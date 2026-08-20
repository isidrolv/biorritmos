require 'socket'
require 'json'
require 'uri'
require_relative 'biorhythm'

class Server
  def initialize(public_dir:, host: '127.0.0.1', port: 0)
    @public_dir = public_dir
    @tcp_server = TCPServer.new(host, port)
  end

  def port
    @tcp_server.addr[1]
  end

  def start
    loop do
      client = @tcp_server.accept
      Thread.new(client) { |c| handle_client(c) }
    end
  end

  private

  def handle_client(client)
    request_line = client.gets
    return if request_line.nil?

    method, full_path = request_line.split(' ')
    consume_headers(client)

    path, query = (full_path || '/').split('?', 2)
    params = query ? URI.decode_www_form(query).to_h : {}

    dispatch(client, method, path, params)
  rescue StandardError => e
    warn "[server] #{e.class}: #{e.message}"
  ensure
    begin
      client.close
    rescue StandardError
      nil
    end
  end

  def consume_headers(client)
    while (line = client.gets)
      break if line == "\r\n" || line == "\n"
    end
  end

  def dispatch(client, method, path, params)
    return respond(client, 405, 'text/plain', 'Method Not Allowed') unless method == 'GET'

    case path
    when '/'
      serve_file(client, 'index.html', 'text/html; charset=utf-8')
    when '/app.css'
      serve_file(client, 'app.css', 'text/css; charset=utf-8')
    when '/app.js'
      serve_file(client, 'app.js', 'application/javascript; charset=utf-8')
    when '/api/biorhythm'
      serve_api(client, params)
    else
      respond(client, 404, 'text/plain; charset=utf-8', 'No encontrado')
    end
  end

  def serve_file(client, filename, content_type)
    file_path = File.join(@public_dir, filename)
    return respond(client, 404, 'text/plain; charset=utf-8', 'No encontrado') unless File.exist?(file_path)

    respond(client, 200, content_type, File.read(file_path, encoding: 'UTF-8'))
  end

  def serve_api(client, params)
    birth = Biorhythm.parse_date(params['birth'])
    selected = Biorhythm.parse_date(params['selected']) || Date.today

    return respond(client, 400, 'application/json; charset=utf-8', { error: 'birth_required' }.to_json) unless birth

    data = Biorhythm.series(birth, selected)
    respond(client, 200, 'application/json; charset=utf-8', data.to_json)
  end

  def respond(client, status, content_type, body)
    reason = { 200 => 'OK', 400 => 'Bad Request', 404 => 'Not Found', 405 => 'Method Not Allowed' }.fetch(status, 'OK')
    bytes = body.to_s.dup.force_encoding('UTF-8').b

    client.write "HTTP/1.1 #{status} #{reason}\r\n"
    client.write "Content-Type: #{content_type}\r\n"
    client.write "Content-Length: #{bytes.bytesize}\r\n"
    client.write "Connection: close\r\n"
    client.write "\r\n"
    client.write bytes
  end
end
