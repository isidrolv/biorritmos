require 'libui'
UI = LibUI
FFI = LibUI::FFI

UI.init

def solid_brush(hex, alpha = 1.0)
  r = hex[1, 2].to_i(16) / 255.0
  g = hex[3, 2].to_i(16) / 255.0
  b = hex[5, 2].to_i(16) / 255.0
  brush = FFI::DrawBrush.malloc
  brush.to_ptr.free = Fiddle::RUBY_FREE
  brush.Type = LibUI::DrawBrushTypeSolid
  brush.R = r
  brush.G = g
  brush.B = b
  brush.A = alpha
  brush
end

def stroke_params(thickness, dashes = [])
  sp = FFI::DrawStrokeParams.malloc
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

handler = FFI::AreaHandler.malloc
handler.to_ptr.free = nil

blockcallers = []

draw_cb = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) do |_ah, _area, params_ptr|
  params = FFI::AreaDrawParams.new(params_ptr)
  ctx = params.Context

  bg_path = UI.draw_new_path(LibUI::DrawFillModeWinding)
  UI.draw_path_add_rectangle(bg_path, 0, 0, params.AreaWidth, params.AreaHeight)
  UI.draw_path_end(bg_path)
  UI.draw_fill(ctx, bg_path, solid_brush('#16171d').to_ptr)
  UI.draw_free_path(bg_path)

  line_path = UI.draw_new_path(LibUI::DrawFillModeWinding)
  UI.draw_path_new_figure(line_path, 10, 10)
  UI.draw_path_line_to(line_path, 300, 10)
  UI.draw_path_end(line_path)
  UI.draw_stroke(ctx, line_path, solid_brush('#1656c9').to_ptr, stroke_params(2, []).to_ptr)
  UI.draw_free_path(line_path)

  dash_path = UI.draw_new_path(LibUI::DrawFillModeWinding)
  UI.draw_path_new_figure(dash_path, 10, 30)
  UI.draw_path_line_to(dash_path, 300, 30)
  UI.draw_path_end(dash_path)
  UI.draw_stroke(ctx, dash_path, solid_brush('#d97706').to_ptr, stroke_params(2, [9, 3, 2, 3]).to_ptr)
  UI.draw_free_path(dash_path)

  circle_path = UI.draw_new_path(LibUI::DrawFillModeWinding)
  UI.draw_path_new_figure_with_arc(circle_path, 50, 60, 8, 0, Math::PI * 2, 0)
  UI.draw_path_end(circle_path)
  UI.draw_fill(ctx, circle_path, solid_brush('#a3195b').to_ptr)
  UI.draw_free_path(circle_path)

  font = FFI::FontDescriptor.malloc
  font.to_ptr.free = Fiddle::RUBY_FREE
  font.Family = 'Segoe UI'
  font.Size = 16.0
  font.Weight = LibUI::TextWeightNormal
  font.Italic = LibUI::TextItalicNormal
  font.Stretch = LibUI::TextStretchNormal

  attr_str = UI.new_attributed_string('Prueba de texto nativo')
  layout_params = FFI::DrawTextLayoutParams.malloc
  layout_params.to_ptr.free = Fiddle::RUBY_FREE
  layout_params.String = attr_str
  layout_params.DefaultFont = font.to_ptr
  layout_params.Width = 300.0
  layout_params.Align = LibUI::DrawTextAlignLeft
  layout = UI.draw_new_text_layout(layout_params.to_ptr)
  UI.draw_text(ctx, layout, 10, 80)
  UI.draw_free_text_layout(layout)
  UI.free_attributed_string(attr_str)
  nil
end
blockcallers << draw_cb
handler.Draw = draw_cb
handler.MouseEvent = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) { |*_| nil }.tap { |c| blockcallers << c }
handler.MouseCrossed = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) { |*_| nil }.tap { |c| blockcallers << c }
handler.DragBroken = Fiddle::Closure::BlockCaller.new(0, [1, 1]) { |*_| nil }.tap { |c| blockcallers << c }
handler.KeyEvent = Fiddle::Closure::BlockCaller.new(1, [1, 1, 1]) { |*_| 0 }.tap { |c| blockcallers << c }

area = UI.new_area(handler.to_ptr)

w = UI.new_window('Prueba Area', 400, 200, 0)
box = UI.new_vertical_box
UI.box_append(box, area, 1)
UI.window_set_child(w, box)
UI.control_show(w)

UI.timer(8000) do
  puts 'ok, cerrando'
  STDOUT.flush
  UI.quit
  0
end

UI.main
puts 'main returned sin crash'
STDOUT.flush
