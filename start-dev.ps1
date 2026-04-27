$projectRoot = $PSScriptRoot
$backendScript = Join-Path $projectRoot 'start-backend-dev.ps1'
$frontendScript = Join-Path $projectRoot 'start-frontend.ps1'
$logDir = Join-Path $projectRoot '.codex-test-logs'
$backendLog = Join-Path $logDir 'backend-dev.log'

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

docker compose up -d

if (Test-Path $backendLog) {
    Remove-Item $backendLog -Force
}

$backendJob = Start-Job -Name 'project-pulse-backend' -ScriptBlock {
    param($scriptPath, $logPath)

    & $scriptPath *>&1 | Tee-Object -FilePath $logPath -Append
} -ArgumentList $backendScript, $backendLog

try {
    Write-Host 'Starting backend in the background...' -ForegroundColor Cyan

    $deadline = (Get-Date).AddSeconds(90)
    $backendReady = $false

    while ((Get-Date) -lt $deadline) {
        if ($backendJob.State -in @('Failed', 'Stopped', 'Completed')) {
            break
        }

        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/rubrics' -UseBasicParsing -TimeoutSec 3
            if ($response.StatusCode -ge 200) {
                $backendReady = $true
                break
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    if (-not $backendReady) {
        Write-Host 'Backend did not become ready. Recent log output:' -ForegroundColor Red
        if (Test-Path $backendLog) {
            Get-Content $backendLog -Tail 60
        }
        throw 'Backend startup failed.'
    }

    Write-Host 'Backend is running on http://localhost:8080' -ForegroundColor Green
    Write-Host 'Frontend will run in this Windsurf terminal on http://localhost:5173' -ForegroundColor Green
    Write-Host 'Press Ctrl+C here when you are done. The background backend job will be stopped.' -ForegroundColor Yellow

    & $frontendScript
} finally {
    if ($backendJob) {
        Stop-Job -Job $backendJob -ErrorAction SilentlyContinue | Out-Null
        Remove-Job -Job $backendJob -Force -ErrorAction SilentlyContinue | Out-Null
    }
}
