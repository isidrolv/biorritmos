require_relative 'lib/server'
require_relative 'lib/browser_launcher'

public_dir = File.expand_path('public', __dir__)
server = Server.new(public_dir: public_dir)
url = "http://127.0.0.1:#{server.port}/"

server_thread = Thread.new { server.start }
sleep 0.2

puts 'Calculadora de Biorritmo'
puts "Escuchando en #{url}"

browser_pid = BrowserLauncher.open(url)

if browser_pid
  begin
    Process.wait(browser_pid)
  rescue Errno::ECHILD, Errno::ESRCH
    nil
  end
  puts 'Ventana cerrada. Saliendo...'
else
  puts 'Se abrió en tu navegador predeterminado. Presiona Ctrl+C aquí para salir.'
  begin
    server_thread.join
  rescue Interrupt
    puts "\nSaliendo..."
  end
end
