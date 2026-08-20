(function () {
  const pad2 = (n) => (n < 10 ? `0${n}` : `${n}`)
  const toInputValue = (date) => `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
  const fromInputValue = (value) => {
    if (!value) return null
    const [y, m, d] = value.split('-').map(Number)
    if (!y || !m || !d) return null
    return new Date(y, m - 1, d)
  }
  const addDays = (date, amount) => {
    const result = new Date(date)
    result.setDate(result.getDate() + amount)
    return result
  }

  const todayStr = toInputValue(new Date())

  const birthInput = document.getElementById('birth-date')
  const selectedInput = document.getElementById('selected-date')
  const prevBtn = document.getElementById('prev-day')
  const todayBtn = document.getElementById('today-btn')
  const nextBtn = document.getElementById('next-day')
  const results = document.getElementById('results')
  const themeSelect = document.getElementById('theme-select')

  birthInput.max = todayStr
  selectedInput.value = todayStr

  const visible = {}

  function applyTheme(theme) {
    if (theme === 'system') {
      document.documentElement.removeAttribute('data-theme')
    } else {
      document.documentElement.setAttribute('data-theme', theme)
    }
    localStorage.setItem('biorritmo-theme', theme)
  }

  const savedTheme = localStorage.getItem('biorritmo-theme') || 'system'
  themeSelect.value = savedTheme
  applyTheme(savedTheme)
  themeSelect.addEventListener('change', () => applyTheme(themeSelect.value))

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, (c) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]))
  }

  function buildChart(data) {
    const { chart, gridlines, centerX, markerLabel, dateLabels, lines } = data

    const gridSvg = gridlines.map((g) => `
      <g>
        <line x1="${chart.margin.left}" x2="${chart.width - chart.margin.right}" y1="${g.y}" y2="${g.y}" class="${g.zero ? 'gridline gridline-zero' : 'gridline'}"/>
        <text x="${chart.margin.left - 8}" y="${g.y}" class="axis-label" text-anchor="end" dy="0.32em">${g.value}</text>
      </g>`).join('')

    const dateLabelsSvg = dateLabels.map((d) => `
      <text x="${d.x}" y="${chart.height - chart.margin.bottom + 18}" class="axis-label" text-anchor="middle">${escapeHtml(d.label)}</text>`).join('')

    const linesSvg = lines.map((line) => `
      <g data-key="${line.key}">
        <path d="${line.path}" fill="none" stroke="${line.color}" stroke-width="2" stroke-dasharray="${line.dash}" stroke-linejoin="round" stroke-linecap="round"/>
        <circle cx="${line.markerX}" cy="${line.markerY}" r="5" fill="${line.color}" stroke="#ffffff" stroke-width="2">
          <title>${escapeHtml(line.label)}: ${line.currentValue}%</title>
        </circle>
      </g>`).join('')

    return `
      <svg class="chart" viewBox="0 0 ${chart.width} ${chart.height}" role="img" aria-label="Gráfico de biorritmo con ciclos físico, emocional, intelectual, espiritual, conciencia, intuición y estética">
        ${gridSvg}
        <line x1="${centerX}" x2="${centerX}" y1="${chart.margin.top}" y2="${chart.height - chart.margin.bottom}" class="marker-line"/>
        <text x="${centerX}" y="${chart.margin.top - 4}" class="marker-label" text-anchor="middle">${escapeHtml(markerLabel)}</text>
        ${dateLabelsSvg}
        <g id="lines-layer">${linesSvg}</g>
      </svg>`
  }

  function legendItem(line) {
    const isVisible = visible[line.key] !== false
    return `
      <li>
        <button type="button" class="legend-item" data-key="${line.key}" aria-pressed="${isVisible}">
          <span class="swatch" style="background:${isVisible ? line.color : 'transparent'}; border-color:${line.color}"></span>
          <span class="legend-text">${escapeHtml(line.label)}</span>
          <span class="legend-value">${line.currentValue}%</span>
          <span class="legend-status">${escapeHtml(line.status)}</span>
        </button>
      </li>`
  }

  function buildLegend(lines) {
    const groups = [['basico', 'Aspectos básicos'], ['complementario', 'Aspectos complementarios']]
    return `<div class="legend">${groups.map(([key, title]) => {
      const groupLines = lines.filter((l) => l.group === key)
      return `
        <div class="legend-group">
          <h2>${title}</h2>
          <ul>${groupLines.map(legendItem).join('')}</ul>
        </div>`
    }).join('')}</div>`
  }

  function wireLegend(lines) {
    results.querySelectorAll('.legend-item').forEach((btn) => {
      btn.addEventListener('click', () => {
        const key = btn.dataset.key
        visible[key] = !(visible[key] !== false)
        const isVisible = visible[key]
        btn.setAttribute('aria-pressed', String(isVisible))
        const line = lines.find((l) => l.key === key)
        btn.querySelector('.swatch').style.background = isVisible ? line.color : 'transparent'
        const group = results.querySelector(`g[data-key="${key}"]`)
        if (group) group.style.display = isVisible ? '' : 'none'
      })
    })
  }

  function showEmptyState() {
    results.innerHTML = '<p class="empty-state">Ingresa tu fecha de nacimiento para ver tu gráfico de biorritmo.</p>'
  }

  function render() {
    const birthStr = birthInput.value
    const selectedStr = selectedInput.value || todayStr
    todayBtn.disabled = selectedStr === todayStr

    if (!birthStr) {
      showEmptyState()
      return
    }

    const url = `/api/biorhythm?birth=${encodeURIComponent(birthStr)}&selected=${encodeURIComponent(selectedStr)}`
    fetch(url)
      .then((response) => response.json())
      .then((data) => {
        if (data.error) {
          showEmptyState()
          return
        }
        results.innerHTML = buildChart(data) + buildLegend(data.lines)
        wireLegend(data.lines)
      })
      .catch(() => showEmptyState())
  }

  birthInput.addEventListener('change', render)
  selectedInput.addEventListener('change', render)
  prevBtn.addEventListener('click', () => {
    selectedInput.value = toInputValue(addDays(fromInputValue(selectedInput.value) || new Date(), -1))
    render()
  })
  nextBtn.addEventListener('click', () => {
    selectedInput.value = toInputValue(addDays(fromInputValue(selectedInput.value) || new Date(), 1))
    render()
  })
  todayBtn.addEventListener('click', () => {
    selectedInput.value = todayStr
    render()
  })

  render()
})()
