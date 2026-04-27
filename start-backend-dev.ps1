$env:SPRING_PROFILES_ACTIVE='dev'

$backendDir = Join-Path $PSScriptRoot 'backend'
$envFile = Join-Path $backendDir '.env'
$envExample = Join-Path $backendDir '.env.example'

if (-not (Test-Path $envFile) -and (Test-Path $envExample)) {
    Copy-Item $envExample $envFile
}

$envSource = if (Test-Path $envFile) { $envFile } else { $envExample }

if (Test-Path $envSource) {
    Get-Content $envSource | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            return
        }

        $parts = $line.Split('=', 2)
        if ($parts.Length -eq 2) {
            [System.Environment]::SetEnvironmentVariable($parts[0], $parts[1])
        }
    }
}

Set-Location $backendDir
& (Join-Path $PSScriptRoot 'tools\apache-maven-3.9.9\bin\mvn.cmd') spring-boot:run
