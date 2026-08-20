require 'rbconfig'
require 'tmpdir'

module BrowserLauncher
  module_function

  def open(url)
    os = RbConfig::CONFIG['host_os']
    if os =~ /mswin|mingw|cygwin/i
      open_windows(url)
    elsif os =~ /darwin/i
      open_mac(url)
    else
      open_linux(url)
    end
  end

  def windows_candidates
    bases = [ENV['ProgramFiles'], ENV['ProgramFiles(x86)']].compact
    bases.flat_map do |base|
      [
        File.join(base, 'Microsoft', 'Edge', 'Application', 'msedge.exe'),
        File.join(base, 'Google', 'Chrome', 'Application', 'chrome.exe'),
      ]
    end
  end

  def open_windows(url)
    path = windows_candidates.find { |candidate| File.exist?(candidate) }
    if path
      profile_dir = File.join(Dir.tmpdir, 'biorritmo-ruby-profile')
      return Process.spawn([path, path], "--app=#{url}", "--user-data-dir=#{profile_dir}", '--window-size=980,1000')
    end

    system('cmd', '/c', 'start', '', url)
    nil
  end

  def open_mac(url)
    chrome = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
    if File.exist?(chrome)
      profile_dir = File.join(Dir.tmpdir, 'biorritmo-ruby-profile')
      return Process.spawn([chrome, chrome], "--app=#{url}", "--user-data-dir=#{profile_dir}")
    end

    system('open', url)
    nil
  end

  def open_linux(url)
    %w[google-chrome chromium chromium-browser].each do |bin|
      path = `which #{bin} 2>/dev/null`.strip
      next if path.empty?

      profile_dir = File.join(Dir.tmpdir, 'biorritmo-ruby-profile')
      return Process.spawn([path, path], "--app=#{url}", "--user-data-dir=#{profile_dir}")
    end

    system('xdg-open', url)
    nil
  end
end
