//go:build windows

package theme

import "golang.org/x/sys/windows/registry"

// systemIsDark replica Theme.system_is_dark? de Ruby: lee
// HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize.
func systemIsDark() bool {
	k, err := registry.OpenKey(registry.CURRENT_USER,
		`Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`, registry.QUERY_VALUE)
	if err != nil {
		return false
	}
	defer k.Close()

	v, _, err := k.GetIntegerValue("AppsUseLightTheme")
	if err != nil {
		return false
	}
	return v == 0
}
