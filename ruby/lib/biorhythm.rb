require 'date'
require_relative 'aspects'

module Biorhythm
  RANGE_DAYS = 15
  CHART_WIDTH = 760
  CHART_HEIGHT = 340
  MARGIN = { top: 16, right: 16, bottom: 34, left: 44 }.freeze
  PLOT_WIDTH = CHART_WIDTH - MARGIN[:left] - MARGIN[:right]
  PLOT_HEIGHT = CHART_HEIGHT - MARGIN[:top] - MARGIN[:bottom]
  MONTHS_ES = %w[ene feb mar abr may jun jul ago sep oct nov dic].freeze

  module_function

  def parse_date(str)
    return nil if str.nil? || str.strip.empty?

    y, m, d = str.split('-').map(&:to_i)
    return nil if y.zero? || m.zero? || d.zero?

    Date.new(y, m, d)
  rescue ArgumentError
    nil
  end

  def days_between(from, to)
    (to - from).to_i
  end

  def value_at(days_since_birth, period)
    Math.sin((2 * Math::PI * days_since_birth) / period.to_f) * 100
  end

  def phase_label(value, next_value)
    return 'Crítico' if value.abs < 3

    next_value > value ? 'Ascendente' : 'Descendente'
  end

  def format_short(date)
    "#{date.day.to_s.rjust(2, '0')} #{MONTHS_ES[date.month - 1]}"
  end

  def series(birth_date, selected_date)
    offsets = (-RANGE_DAYS..RANGE_DAYS).to_a
    dates = offsets.map { |offset| selected_date + offset }
    days_since_birth = dates.map { |date| days_between(birth_date, date) }

    x_scale = ->(index) { MARGIN[:left] + (index.to_f / (offsets.length - 1)) * PLOT_WIDTH }
    y_scale = ->(val) { MARGIN[:top] + (1 - (val + 100) / 200.0) * PLOT_HEIGHT }

    lines = Aspects::LIST.map do |aspect|
      values = days_since_birth.map { |days| value_at(days, aspect[:period]) }
      points = values.each_with_index.map { |v, i| [x_scale.call(i), y_scale.call(v)] }
      current_value = values[RANGE_DAYS]
      next_value = value_at(days_since_birth[RANGE_DAYS] + 1, aspect[:period])

      {
        key: aspect[:key],
        label: aspect[:label],
        color: aspect[:color],
        dash: aspect[:dash],
        group: aspect[:group],
        points: points,
        current_value: current_value.round,
        status: phase_label(current_value, next_value),
        marker_x: x_scale.call(RANGE_DAYS),
        marker_y: y_scale.call(current_value),
      }
    end

    date_labels = offsets.each_with_index
                          .select { |offset, _| (offset % 5).zero? }
                          .map { |_offset, index| { x: x_scale.call(index).round(2), label: format_short(dates[index]) } }

    gridlines = [100, 50, 0, -50, -100].map { |val| { value: val, y: y_scale.call(val).round(2), zero: val.zero? } }

    is_today = selected_date == Date.today
    marker_label = is_today ? 'Hoy' : format_short(selected_date)

    {
      gridlines: gridlines,
      center_x: x_scale.call(RANGE_DAYS),
      marker_label: marker_label,
      date_labels: date_labels,
      lines: lines,
      is_today: is_today,
    }
  end
end
