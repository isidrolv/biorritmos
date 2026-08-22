package biorritmo

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.swing.JPanel
import kotlin.math.max

class BiorhythmCanvas(private val colors: () -> ThemeColors) : JPanel() {
    companion object {
        private const val CHART_WIDTH = 760.0
        private const val CHART_HEIGHT = 340.0
        private const val MARGIN_TOP = 16.0
        private const val MARGIN_RIGHT = 16.0
        private const val MARGIN_BOTTOM = 34.0
        private const val MARGIN_LEFT = 44.0
        private const val CHART_Y = 108.0
        private const val LEGEND_Y = 464.0
        private const val LEGEND_COL_WIDTH = 340.0
        private const val LEGEND_COL_GAP = 40.0
        private const val LEGEND_LEFT = 20.0
        private const val LEGEND_TOP = 12.0
        private const val LEGEND_HEADER_H = 22.0
        private const val LEGEND_ROW_H = 26.0
    }

    private var result: BiorhythmResult? = null
    private var selectedDate = LocalDate.now()
    private val visible = Aspects.all.associate { it.key to true }.toMutableMap()
    private val spanishLocale = Locale.of("es", "MX")
    private val dateFormat = DateTimeFormatter.ofPattern("dd MMM", spanishLocale)
    private var offsetX = 0.0

    init {
        preferredSize = Dimension(800, 614)
        isOpaque = true
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(event: MouseEvent) = toggleLegendAt(event.x.toDouble(), event.y.toDouble())
        })
    }

    fun updateData(birthDate: LocalDate, date: LocalDate) {
        selectedDate = date
        result = Biorhythm.calculate(birthDate, date)
        repaint()
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val g = graphics.create() as Graphics2D
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            val theme = colors()
            g.color = theme.background
            g.fillRect(0, 0, width, height)
            offsetX = max((width - CHART_WIDTH) / 2.0, 0.0)
            drawHeader(g, theme)
            drawChart(g, theme)
            drawLegend(g, theme)
        } finally {
            g.dispose()
        }
    }

    private fun drawHeader(g: Graphics2D, theme: ThemeColors) {
        drawCentered(g, "Calculadora de biorritmo", offsetX + CHART_WIDTH / 2, 29.0, 22f, Font.PLAIN, theme.heading)
        drawCentered(g, "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual,", offsetX + CHART_WIDTH / 2, 58.0, 12f, Font.PLAIN, theme.muted)
        drawCentered(g, "junto con los aspectos complementarios: espiritual, conciencia, intuición y estética.", offsetX + CHART_WIDTH / 2, 76.0, 12f, Font.PLAIN, theme.muted)
    }

    private fun drawChart(g: Graphics2D, theme: ThemeColors) {
        val data = result ?: return
        val plotWidth = CHART_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
        val plotHeight = CHART_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM
        val left = offsetX + MARGIN_LEFT
        val right = offsetX + CHART_WIDTH - MARGIN_RIGHT
        val top = CHART_Y + MARGIN_TOP
        val bottom = CHART_Y + CHART_HEIGHT - MARGIN_BOTTOM
        fun x(index: Int) = left + index.toDouble() / (data.dates.size - 1) * plotWidth
        fun y(value: Double) = top + (1.0 - (value + 100.0) / 200.0) * plotHeight

        for (value in listOf(100, 50, 0, -50, -100)) {
            val yy = y(value.toDouble())
            g.color = if (value == 0) theme.zeroGridline else theme.gridline
            g.stroke = BasicStroke(1f)
            g.drawLine(left.toInt(), yy.toInt(), right.toInt(), yy.toInt())
            drawRight(g, value.toString(), left - 8, yy + 4, 11f, Font.PLAIN, theme.axisLabel)
        }

        val centerX = x(Biorhythm.RANGE_DAYS)
        g.color = theme.markerLine
        g.stroke = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(3f, 3f), 0f)
        g.drawLine(centerX.toInt(), top.toInt(), centerX.toInt(), bottom.toInt())
        val marker = if (selectedDate == LocalDate.now()) "Hoy" else formatDate(selectedDate)
        drawCentered(g, marker, centerX, top - 6, 12f, Font.BOLD, theme.label)

        data.dates.forEachIndexed { index, date ->
            if ((index - Biorhythm.RANGE_DAYS) % 5 == 0) {
                drawCentered(g, formatDate(date), x(index), bottom + 18, 11f, Font.PLAIN, theme.axisLabel)
            }
        }

        data.cycles.filter { visible[it.aspect.key] == true }.forEach { cycle ->
            val path = Path2D.Double()
            cycle.values.forEachIndexed { index, value ->
                if (index == 0) path.moveTo(x(index), y(value)) else path.lineTo(x(index), y(value))
            }
            g.color = cycle.aspect.color
            g.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, cycle.aspect.dash, 0f)
            g.draw(path)
            val markerY = y(cycle.values[Biorhythm.RANGE_DAYS])
            g.fill(Ellipse2D.Double(centerX - 5, markerY - 5, 10.0, 10.0))
            g.color = Color.WHITE
            g.stroke = BasicStroke(2f)
            g.draw(Ellipse2D.Double(centerX - 5, markerY - 5, 10.0, 10.0))
        }
    }

    private fun drawLegend(g: Graphics2D, theme: ThemeColors) {
        val data = result ?: return
        val groups = listOf(
            Triple(LEGEND_LEFT, "Aspectos básicos", Aspects.all.filter { it.group == AspectGroup.BASIC }),
            Triple(LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP, "Aspectos complementarios", Aspects.all.filter { it.group == AspectGroup.COMPLEMENTARY }),
        )
        val values = data.cycles.associateBy { it.aspect.key }
        groups.forEach { (columnX, title, aspects) ->
            drawLeft(g, title, offsetX + columnX, LEGEND_Y + LEGEND_TOP + 14, 14f, Font.BOLD, theme.label)
            aspects.forEachIndexed { index, aspect ->
                val rowTop = LEGEND_Y + LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                val centerY = rowTop + LEGEND_ROW_H / 2
                val enabled = visible[aspect.key] == true
                g.color = aspect.color
                if (enabled) g.fill(Ellipse2D.Double(offsetX + columnX + 2, centerY - 6, 12.0, 12.0))
                else {
                    g.stroke = BasicStroke(2f)
                    g.draw(Ellipse2D.Double(offsetX + columnX + 2, centerY - 6, 12.0, 12.0))
                }
                val line = values.getValue(aspect.key)
                val textColor = if (enabled) theme.heading else theme.legendDim
                drawLeft(g, aspect.label, offsetX + columnX + 24, rowTop + 17, 13f, Font.PLAIN, textColor)
                drawRight(g, "${line.currentValue}%", offsetX + columnX + LEGEND_COL_WIDTH - 90, rowTop + 17, 13f, Font.PLAIN, textColor)
                drawRight(g, line.status, offsetX + columnX + LEGEND_COL_WIDTH, rowTop + 17, 12f, Font.PLAIN, theme.axisLabel)
            }
        }
    }

    private fun toggleLegendAt(x: Double, y: Double) {
        val localX = x - offsetX
        val localY = y - LEGEND_Y
        val columns = listOf(
            LEGEND_LEFT to Aspects.all.filter { it.group == AspectGroup.BASIC },
            (LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP) to Aspects.all.filter { it.group == AspectGroup.COMPLEMENTARY },
        )
        for ((columnX, aspects) in columns) {
            if (localX !in columnX..(columnX + LEGEND_COL_WIDTH)) continue
            aspects.forEachIndexed { index, aspect ->
                val rowY = LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                if (localY in rowY..(rowY + LEGEND_ROW_H)) {
                    visible[aspect.key] = visible[aspect.key] != true
                    repaint()
                    return
                }
            }
        }
    }

    private fun formatDate(date: LocalDate) = date.format(dateFormat).replaceFirstChar { it.lowercase(spanishLocale) }
    private fun font(size: Float, style: Int) = Font("Segoe UI", style, size.toInt())

    private fun drawLeft(g: Graphics2D, text: String, x: Double, baseline: Double, size: Float, style: Int, color: Color) {
        g.font = font(size, style); g.color = color; g.drawString(text, x.toFloat(), baseline.toFloat())
    }

    private fun drawCentered(g: Graphics2D, text: String, centerX: Double, baseline: Double, size: Float, style: Int, color: Color) {
        g.font = font(size, style); g.color = color
        g.drawString(text, (centerX - g.fontMetrics.stringWidth(text) / 2.0).toFloat(), baseline.toFloat())
    }

    private fun drawRight(g: Graphics2D, text: String, right: Double, baseline: Double, size: Float, style: Int, color: Color) {
        g.font = font(size, style); g.color = color
        g.drawString(text, (right - g.fontMetrics.stringWidth(text)).toFloat(), baseline.toFloat())
    }
}
