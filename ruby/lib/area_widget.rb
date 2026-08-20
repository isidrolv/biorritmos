require 'libui'

# Thin wrapper around libui's uiArea (a native custom-drawing canvas),
# wiring up the Draw/MouseEvent callback struct via Fiddle.
class AreaWidget
  attr_reader :control

  def initialize(on_draw:, on_mouse_down: nil)
    @callbacks = []

    handler = LibUI::FFI::AreaHandler.malloc
    handler.to_ptr.free = nil

    draw_cb = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) do |_ah, _area, params_ptr|
      params = LibUI::FFI::AreaDrawParams.new(params_ptr)
      on_draw.call(params.Context, params.AreaWidth, params.AreaHeight)
      nil
    end
    @callbacks << draw_cb
    handler.Draw = draw_cb

    mouse_cb = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) do |_ah, _area, event_ptr|
      if on_mouse_down
        event = LibUI::FFI::AreaMouseEvent.new(event_ptr)
        on_mouse_down.call(event.X, event.Y) if event.Down == 1
      end
      nil
    end
    @callbacks << mouse_cb
    handler.MouseEvent = mouse_cb

    noop_mouse_crossed = Fiddle::Closure::BlockCaller.new(0, [1, 1, 1]) { |*_| nil }
    @callbacks << noop_mouse_crossed
    handler.MouseCrossed = noop_mouse_crossed

    noop_drag_broken = Fiddle::Closure::BlockCaller.new(0, [1, 1]) { |*_| nil }
    @callbacks << noop_drag_broken
    handler.DragBroken = noop_drag_broken

    key_cb = Fiddle::Closure::BlockCaller.new(1, [1, 1, 1]) { |*_| 0 }
    @callbacks << key_cb
    handler.KeyEvent = key_cb

    @handler = handler
    # uiNewScrollingArea never fires the Draw callback on this platform, so
    # we always use a plain area and let the caller add it to its box with
    # stretchy=1 (a non-stretchy uiArea always reports a 0x0 size).
    @control = LibUI.new_area(handler.to_ptr)
  end

  def redraw
    LibUI.area_queue_redraw_all(@control)
  end
end
