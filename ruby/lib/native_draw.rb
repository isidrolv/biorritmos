require 'libui'

module NativeDraw
  UI = LibUI
  DFFI = LibUI::FFI
  FONT_FAMILY = 'Segoe UI'

  module_function

  def hex_to_rgb(hex)
    [hex[1, 2].to_i(16) / 255.0, hex[3, 2].to_i(16) / 255.0, hex[5, 2].to_i(16) / 255.0]
  end

  def solid_brush(hex, alpha = 1.0)
    r, g, b = hex_to_rgb(hex)
    brush = DFFI::DrawBrush.malloc
    brush.to_ptr.free = Fiddle::RUBY_FREE
    brush.Type = LibUI::DrawBrushTypeSolid
    brush.R = r
    brush.G = g
    brush.B = b
    brush.A = alpha
    brush
  end

  def stroke_params(thickness, dashes: [])
    sp = DFFI::DrawStrokeParams.malloc
    sp.to_ptr.free = Fiddle::RUBY_FREE
    sp.Cap = LibUI::DrawLineCapRound
    sp.Join = LibUI::DrawLineJoinRound
    sp.Thickness = thickness
    sp.MiterLimit = 10.0
    if dashes.empty?
      sp.NumDashes = 0
    else
      ptr = Fiddle::Pointer.malloc(8 * dashes.size, Fiddle::RUBY_FREE)
      ptr[0, 8 * dashes.size] = dashes.pack('d*')
      sp.Dashes = ptr
      sp.NumDashes = dashes.size
    end
    sp.DashPhase = 0.0
    sp
  end

  def parse_dash(dash_str)
    return [] if dash_str.nil?

    values = dash_str.split(' ').map(&:to_f)
    return [] if values == [0.0]

    values
  end

  def fill_rect(ctx, x, y, w, h, color_hex, alpha: 1.0)
    path = UI.draw_new_path(LibUI::DrawFillModeWinding)
    UI.draw_path_add_rectangle(path, x, y, w, h)
    UI.draw_path_end(path)
    UI.draw_fill(ctx, path, solid_brush(color_hex, alpha).to_ptr)
    UI.draw_free_path(path)
  end

  def stroke_line(ctx, x1, y1, x2, y2, color_hex, thickness: 1, dashes: [])
    path = UI.draw_new_path(LibUI::DrawFillModeWinding)
    UI.draw_path_new_figure(path, x1, y1)
    UI.draw_path_line_to(path, x2, y2)
    UI.draw_path_end(path)
    UI.draw_stroke(ctx, path, solid_brush(color_hex).to_ptr, stroke_params(thickness, dashes: dashes).to_ptr)
    UI.draw_free_path(path)
  end

  def stroke_polyline(ctx, points, color_hex, thickness: 2, dashes: [])
    return if points.size < 2

    path = UI.draw_new_path(LibUI::DrawFillModeWinding)
    UI.draw_path_new_figure(path, points[0][0], points[0][1])
    points[1..].each { |x, y| UI.draw_path_line_to(path, x, y) }
    UI.draw_path_end(path)
    UI.draw_stroke(ctx, path, solid_brush(color_hex).to_ptr, stroke_params(thickness, dashes: dashes).to_ptr)
    UI.draw_free_path(path)
  end

  def fill_circle(ctx, cx, cy, r, color_hex, alpha: 1.0)
    path = UI.draw_new_path(LibUI::DrawFillModeWinding)
    UI.draw_path_new_figure_with_arc(path, cx, cy, r, 0, Math::PI * 2, 0)
    UI.draw_path_end(path)
    UI.draw_fill(ctx, path, solid_brush(color_hex, alpha).to_ptr)
    UI.draw_free_path(path)
  end

  def stroke_circle(ctx, cx, cy, r, color_hex, thickness: 2)
    path = UI.draw_new_path(LibUI::DrawFillModeWinding)
    UI.draw_path_new_figure_with_arc(path, cx, cy, r, 0, Math::PI * 2, 0)
    UI.draw_path_end(path)
    UI.draw_stroke(ctx, path, solid_brush(color_hex).to_ptr, stroke_params(thickness).to_ptr)
    UI.draw_free_path(path)
  end

  def build_text_layout(text, size:, color_hex: nil, weight: LibUI::TextWeightNormal,
                         italic: LibUI::TextItalicNormal, width: -1, align: LibUI::DrawTextAlignLeft)
    font = DFFI::FontDescriptor.malloc
    font.to_ptr.free = Fiddle::RUBY_FREE
    font.Family = FONT_FAMILY
    font.Size = size
    font.Weight = weight
    font.Italic = italic
    font.Stretch = LibUI::TextStretchNormal

    attr_str = UI.new_attributed_string(text)
    if color_hex
      r, g, b = hex_to_rgb(color_hex)
      UI.attributed_string_set_attribute(attr_str, UI.new_color_attribute(r, g, b, 1.0), 0, text.bytesize)
    end

    params = DFFI::DrawTextLayoutParams.malloc
    params.to_ptr.free = Fiddle::RUBY_FREE
    params.String = attr_str
    params.DefaultFont = font.to_ptr
    params.Width = width
    params.Align = align

    [UI.draw_new_text_layout(params.to_ptr), attr_str]
  end

  def text_extents(text, size:, weight: LibUI::TextWeightNormal)
    layout, attr_str = build_text_layout(text, size: size, weight: weight)
    w_ptr = Fiddle::Pointer.malloc(8, Fiddle::RUBY_FREE)
    h_ptr = Fiddle::Pointer.malloc(8, Fiddle::RUBY_FREE)
    UI.draw_text_layout_extents(layout, w_ptr, h_ptr)
    width = w_ptr[0, 8].unpack1('d')
    height = h_ptr[0, 8].unpack1('d')
    UI.draw_free_text_layout(layout)
    UI.free_attributed_string(attr_str)
    [width, height]
  end

  def draw_text(ctx, text, x, y, size:, color_hex:, weight: LibUI::TextWeightNormal)
    layout, attr_str = build_text_layout(text, size: size, color_hex: color_hex, weight: weight)
    UI.draw_text(ctx, layout, x, y)
    UI.draw_free_text_layout(layout)
    UI.free_attributed_string(attr_str)
  end

  def draw_text_centered(ctx, text, cx, y, size:, color_hex:, weight: LibUI::TextWeightNormal)
    w, = text_extents(text, size: size, weight: weight)
    draw_text(ctx, text, cx - (w / 2.0), y, size: size, color_hex: color_hex, weight: weight)
  end

  def draw_text_right(ctx, text, x_right, y, size:, color_hex:, weight: LibUI::TextWeightNormal)
    w, = text_extents(text, size: size, weight: weight)
    draw_text(ctx, text, x_right - w, y, size: size, color_hex: color_hex, weight: weight)
  end

  # Draws a (possibly multi-line, wrapped) paragraph within `width`, returns the height it used.
  def draw_layout(ctx, text, x, y, width:, align:, size:, color_hex:, weight: LibUI::TextWeightNormal)
    layout, attr_str = build_text_layout(text, size: size, color_hex: color_hex, weight: weight, width: width, align: align)
    w_ptr = Fiddle::Pointer.malloc(8, Fiddle::RUBY_FREE)
    h_ptr = Fiddle::Pointer.malloc(8, Fiddle::RUBY_FREE)
    UI.draw_text_layout_extents(layout, w_ptr, h_ptr)
    height = h_ptr[0, 8].unpack1('d')
    UI.draw_text(ctx, layout, x, y)
    UI.draw_free_text_layout(layout)
    UI.free_attributed_string(attr_str)
    height
  end
end
