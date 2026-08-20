require 'win32/registry'

module Theme
  LIGHT = {
    bg: '#ffffff',
    text_h: '#1a1a1a',
    muted: '#555555',
    label: '#333333',
    gridline: '#e5e4e7',
    gridline_zero: '#b8b6bd',
    marker_line: '#999999',
    axis_label: '#777777',
    legend_dim: '#aaaaaa',
  }.freeze

  DARK = {
    bg: '#16171d',
    text_h: '#f3f4f6',
    muted: '#9ca3af',
    label: '#d1d5db',
    gridline: '#2e303a',
    gridline_zero: '#4b4d59',
    marker_line: '#6b6d78',
    axis_label: '#9ca3af',
    legend_dim: '#6b6d78',
  }.freeze

  module_function

  def system_is_dark?
    Win32::Registry::HKEY_CURRENT_USER.open('Software\Microsoft\Windows\CurrentVersion\Themes\Personalize') do |reg|
      reg['AppsUseLightTheme'].to_i.zero?
    end
  rescue StandardError
    false
  end

  def colors(mode)
    case mode
    when :dark then DARK
    when :light then LIGHT
    else system_is_dark? ? DARK : LIGHT
    end
  end
end
