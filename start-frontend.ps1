$frontendDir = Join-Path $PSScriptRoot 'frontend'
$envFile = Join-Path $frontendDir '.env'
$envExample = Join-Path $frontendDir '.env.example'
$nodeModules = Join-Path $frontendDir 'node_modules'

if (-not (Test-Path $envFile) -and (Test-Path $envExample)) {
    Copy-Item $envExample $envFile
}

Set-Location $frontendDir

if (-not (Test-Path $nodeModules)) {
    npm install
}

npm run dev
