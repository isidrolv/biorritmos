require 'date'
require_relative 'lib/aspects'
require_relative 'lib/biorhythm'
require_relative 'lib/theme'
require_relative 'lib/native_draw'
require_relative 'lib/area_widget'

UI = LibUI
D = NativeDraw

class BiorritmoApp
  CHART_WIDTH = Biorhythm::CHART_WIDTH.to_f
  CHART_HEIGHT = Biorhythm::CHART_HEIGHT.to_f
  MARGIN = Biorhythm::MARGIN
  HEADER_HEIGHT = 92

  LEGEND_WIDTH = CHART_WIDTH
  LEGEND_COL_WIDTH = 340.0
  LEGEND_COL_GAP = 40.0
  LEGEND_LEFT = (LEGEND_WIDTH - ((LEGEND_COL_WIDTH * 2) + LEGEND_COL_GAP)) / 2.0
  LEGEND_TOP = 12.0
  LEGEND_HEADER_H = 22.0
  LEGEND_ROW_H = 26.0
  LEGEND_HEIGHT = (LEGEND_TOP + LEGEND_HEADER_H + (4 * LEGEND_ROW_H) + 12).to_i

  CANVAS_WIDTH = CHART_WIDTH
  CANVAS_GAP = 16.0
  CHART_Y = HEADER_HEIGHT + CANVAS_GAP
  LEGEND_Y = CHART_Y + CHART_HEIGHT + CANVAS_GAP
  CANVAS_HEIGHT = LEGEND_Y + LEGEND_HEIGHT

  BASICO = Aspects::LIST.select { |a| a[:group] == 'basico' }
  COMPLEMENTARIO = Aspects::LIST.select { |a| a[:group] == 'complementario' }
  LEGEND_GROUPS = [
    [LEGEND_LEFT, :basico, 'Aspectos básicos', BASICO],
    [LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP, :complementario, 'Aspectos complementarios', COMPLEMENTARIO],
  ].freeze

  def initialize
    @theme_mode = :system
    @visible = Aspects::LIST.each_with_object({}) { |a, h| h[a[:key]] = true }
    @birth_date = Date.new(Date.today.year - 25, Date.today.month, [Date.today.day, 28].min)
    @selected_date = Date.today
    @series = nil

    build_window
    UI.date_time_picker_on_changed(@birth_picker) { on_birth_changed }
    UI.date_time_picker_on_changed(@selected_picker) { on_selected_changed }
    recalc
  end

  def run
    UI.control_show(@window)
    UI.main
  end

  private

  def colors
    Theme.colors(@theme_mode)
  end

  # -- date <-> struct tm -------------------------------------------------

  def date_to_tm(date)
    tm = LibUI::FFI::TM.malloc
    tm.to_ptr.free = Fiddle::RUBY_FREE
    tm.tm_year = date.year - 1900
    tm.tm_mon = date.month - 1
    tm.tm_mday = date.day
    tm.tm_hour = 0
    tm.tm_min = 0
    tm.tm_sec = 0
    tm
  end

  def tm_to_date(tm)
    Date.new(tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday)
  end

  def read_picker_date(picker)
    tm = LibUI::FFI::TM.malloc
    tm.to_ptr.free = Fiddle::RUBY_FREE
    UI.date_time_picker_time(picker, tm.to_ptr)
    tm_to_date(tm)
  end

  def set_picker_date(picker, date)
    UI.date_time_picker_set_time(picker, date_to_tm(date).to_ptr)
  end

  # -- window construction --------------------------------------------------

  def build_window
    @window = UI.new_window('Calculadora de biorritmo', 900, 840, 0)
    UI.window_set_margined(@window, 1)
    UI.window_on_closing(@window) do
      UI.quit
      1
    end

    root = UI.new_vertical_box
    UI.box_set_padded(root, 1)

    append_topbar(root)
    append_controls(root)
    append_canvas(root)

    UI.window_set_child(@window, root)
  end

  def append_topbar(root)
    bar = UI.new_horizontal_box
    UI.box_set_padded(bar, 1)
    UI.box_append(bar, UI.new_label(''), 1)
    UI.box_append(bar, UI.new_label('Tema:'), 0)

    @theme_combo = UI.new_combobox
    %w[Sistema Claro Oscuro].each { |item| UI.combobox_append(@theme_combo, item) }
    UI.combobox_set_selected(@theme_combo, 0)
    UI.combobox_on_selected(@theme_combo) do
      idx = UI.combobox_selected(@theme_combo)
      @theme_mode = %i[system light dark][idx]
      redraw_all
    end
    UI.box_append(bar, @theme_combo, 0)
    UI.box_append(root, bar, 0)
  end

  def append_controls(root)
    outer = UI.new_horizontal_box
    UI.box_append(outer, UI.new_label(''), 1)

    inner = UI.new_horizontal_box
    UI.box_set_padded(inner, 1)

    birth_col = UI.new_vertical_box
    UI.box_set_padded(birth_col, 1)
    UI.box_append(birth_col, UI.new_label('Fecha de nacimiento'), 0)
    @birth_picker = UI.new_date_picker
    set_picker_date(@birth_picker, @birth_date)
    UI.box_append(birth_col, @birth_picker, 0)
    UI.box_append(inner, birth_col, 0)

    selected_col = UI.new_vertical_box
    UI.box_set_padded(selected_col, 1)
    UI.box_append(selected_col, UI.new_label('Fecha a analizar'), 0)
    @selected_picker = UI.new_date_picker
    set_picker_date(@selected_picker, @selected_date)
    UI.box_append(selected_col, @selected_picker, 0)
    UI.box_append(inner, selected_col, 0)

    nav_col = UI.new_vertical_box
    UI.box_set_padded(nav_col, 1)
    UI.box_append(nav_col, UI.new_label(''), 0)
    nav = UI.new_horizontal_box
    UI.box_set_padded(nav, 1)

    prev_btn = UI.new_button('< Anterior')
    UI.button_on_clicked(prev_btn) { shift_selected(-1) }
    UI.box_append(nav, prev_btn, 0)

    @today_btn = UI.new_button('Hoy')
    UI.button_on_clicked(@today_btn) { set_selected(Date.today) }
    UI.box_append(nav, @today_btn, 0)

    next_btn = UI.new_button('Siguiente >')
    UI.button_on_clicked(next_btn) { shift_selected(1) }
    UI.box_append(nav, next_btn, 0)

    UI.box_append(nav_col, nav, 0)
    UI.box_append(inner, nav_col, 0)

    UI.box_append(outer, inner, 0)
    UI.box_append(outer, UI.new_label(''), 1)
    UI.box_append(root, outer, 0)
  end

  def append_canvas(root)
    @canvas_offset_x = 0.0
    @canvas_area = AreaWidget.new(on_draw: method(:draw_canvas), on_mouse_down: method(:canvas_mouse_down))
    UI.box_append(root, @canvas_area.control, 1)
  end

  # -- event handlers ---------------------------------------------------

  def on_birth_changed
    @birth_date = read_picker_date(@birth_picker)
    recalc
  end

  def on_selected_changed
    @selected_date = read_picker_date(@selected_picker)
    recalc
  end

  def shift_selected(amount)
    set_selected(@selected_date + amount)
  end

  def set_selected(date)
    @selected_date = date
    set_picker_date(@selected_picker, date)
    recalc
  end

  def canvas_mouse_down(x, y)
    lx = x - @canvas_offset_x
    ly = y - LEGEND_Y

    LEGEND_GROUPS.each do |col_x, _group_key, _title, aspects|
      next unless lx >= col_x && lx <= col_x + LEGEND_COL_WIDTH

      aspects.each_with_index do |aspect, i|
        row_y = LEGEND_TOP + LEGEND_HEADER_H + (i * LEGEND_ROW_H)
        next unless ly >= row_y && ly <= row_y + LEGEND_ROW_H

        @visible[aspect[:key]] = !@visible[aspect[:key]]
        @canvas_area.redraw
        return
      end
    end
  end

  def recalc
    @series = Biorhythm.series(@birth_date, @selected_date)
    if @selected_date == Date.today
      UI.control_disable(@today_btn)
    else
      UI.control_enable(@today_btn)
    end
    redraw_all
  end

  def redraw_all
    @canvas_area.redraw
  end

  # -- drawing ------------------------------------------------------------

  def draw_canvas(ctx, width, height)
    c = colors
    D.fill_rect(ctx, 0, 0, width, height, c[:bg])

    @canvas_offset_x = [(width - CANVAS_WIDTH) / 2.0, 0].max
    ox = @canvas_offset_x

    draw_header(ctx, ox, 0)
    draw_chart(ctx, ox, CHART_Y)
    draw_legend(ctx, ox, LEGEND_Y)
  end

  def draw_header(ctx, ox, oy)
    c = colors

    title_h = D.draw_layout(ctx, 'Calculadora de biorritmo', ox, oy + 6, width: CHART_WIDTH,
                                                               align: LibUI::DrawTextAlignCenter, size: 22,
                                                               color_hex: c[:text_h], weight: LibUI::TextWeightMedium)

    subtitle = "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, " \
               'junto con los aspectos complementarios: espiritual, conciencia, intuición y estética.'
    D.draw_layout(ctx, subtitle, ox, oy + 6 + title_h + 6, width: CHART_WIDTH, align: LibUI::DrawTextAlignCenter,
                                                            size: 12, color_hex: c[:muted])
  end

  def draw_chart(ctx, ox, oy)
    c = colors
    return unless @series

    left = ox + MARGIN[:left]
    right_edge = ox + CHART_WIDTH - MARGIN[:right]
    bottom = oy + CHART_HEIGHT - MARGIN[:bottom]
    top = oy + MARGIN[:top]

    @series[:gridlines].each do |g|
      color = g[:zero] ? c[:gridline_zero] : c[:gridline]
      D.stroke_line(ctx, left, oy + g[:y], right_edge, oy + g[:y], color, thickness: 1)
      D.draw_text_right(ctx, g[:value].to_s, left - 8, oy + g[:y] - 6, size: 11, color_hex: c[:axis_label])
    end

    cx = ox + @series[:center_x]
    D.stroke_line(ctx, cx, top, cx, bottom, c[:marker_line], thickness: 1, dashes: [3, 3])
    D.draw_text_centered(ctx, @series[:marker_label], cx, top - 18, size: 12, color_hex: c[:label],
                                                        weight: LibUI::TextWeightBold)

    @series[:date_labels].each do |d|
      D.draw_text_centered(ctx, d[:label], ox + d[:x], bottom + 6, size: 11, color_hex: c[:axis_label])
    end

    @series[:lines].each do |line|
      next unless @visible[line[:key]]

      dashes = D.parse_dash(line[:dash])
      points = line[:points].map { |x, y| [ox + x, oy + y] }
      marker_x = ox + line[:marker_x]
      marker_y = oy + line[:marker_y]
      D.stroke_polyline(ctx, points, line[:color], thickness: 2, dashes: dashes)
      D.fill_circle(ctx, marker_x, marker_y, 5, line[:color])
      D.stroke_circle(ctx, marker_x, marker_y, 5, '#ffffff', thickness: 2)
    end
  end

  def draw_legend(ctx, ox, oy)
    c = colors
    return unless @series

    values_by_key = @series[:lines].each_with_object({}) { |l, h| h[l[:key]] = l }

    LEGEND_GROUPS.each do |col_x, _group_key, title, aspects|
      D.draw_text(ctx, title, ox + col_x, oy + LEGEND_TOP, size: 14, color_hex: c[:label],
                                                             weight: LibUI::TextWeightBold)

      aspects.each_with_index do |aspect, i|
        row_y = oy + LEGEND_TOP + LEGEND_HEADER_H + (i * LEGEND_ROW_H)
        draw_legend_row(ctx, ox + col_x, row_y, aspect, values_by_key[aspect[:key]], c)
      end
    end
  end

  def draw_legend_row(ctx, col_x, row_y, aspect, line, c)
    visible = @visible[aspect[:key]]
    cy = row_y + (LEGEND_ROW_H / 2.0)

    if visible
      D.fill_circle(ctx, col_x + 8, cy, 6, aspect[:color])
    else
      D.stroke_circle(ctx, col_x + 8, cy, 6, aspect[:color], thickness: 2)
    end

    text_color = visible ? c[:text_h] : c[:legend_dim]
    D.draw_text(ctx, aspect[:label], col_x + 24, row_y + 4, size: 13, color_hex: text_color)
    D.draw_text_right(ctx, "#{line[:current_value]}%", col_x + LEGEND_COL_WIDTH - 90, row_y + 4, size: 13,
                                                         color_hex: text_color)
    D.draw_text_right(ctx, line[:status], col_x + LEGEND_COL_WIDTH, row_y + 4, size: 12, color_hex: c[:axis_label])
  end
end

UI.init
BiorritmoApp.new.run
