[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateNotNullOrEmpty()]
    [string[]]$Url,

    [Parameter()]
    [string]$OutputDirectory = (Join-Path $HOME 'Downloads')
)

$ErrorActionPreference = 'Stop'

$ytDlp = Get-Command 'yt-dlp' -ErrorAction SilentlyContinue
if (-not $ytDlp) {
    throw "No encuentro yt-dlp. Instálalo y asegúrate de que esté en PATH: https://github.com/yt-dlp/yt-dlp#installation"
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$outputTemplate = Join-Path $OutputDirectory '%(title)s [%(id)s].%(ext)s'

& $ytDlp.Source --no-playlist --merge-output-format mp4 --output $outputTemplate @Url
if ($LASTEXITCODE -ne 0) {
    throw "yt-dlp terminó con código de error $LASTEXITCODE."
}
