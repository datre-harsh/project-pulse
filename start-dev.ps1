$projectRoot = $PSScriptRoot
$backendScript = Join-Path $projectRoot 'start-backend-dev.ps1'
$frontendScript = Join-Path $projectRoot 'start-frontend.ps1'
$logDir = Join-Path $projectRoot '.codex-test-logs'
$backendLog = Join-Path $logDir 'backend-dev.log'
$backendEnv = Join-Path $projectRoot 'backend\.env'

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$mongoUri = $null
if (Test-Path $backendEnv) {
    $mongoUriLine = Get-Content $backendEnv | Where-Object {
        $_.Trim().StartsWith('MONGODB_URI=')
    } | Select-Object -First 1

    if ($mongoUriLine) {
        $mongoUri = $mongoUriLine.Split('=', 2)[1].Trim()
    }
}

if (-not $mongoUri -or $mongoUri.StartsWith('mongodb://localhost') -or $mongoUri.StartsWith('mongodb://127.0.0.1')) {
    docker compose up -d
} else {
    Write-Host 'Using remote MongoDB from backend/.env; skipping local Docker MongoDB.' -ForegroundColor Cyan
}

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
