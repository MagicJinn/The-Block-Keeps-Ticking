# Runs deterministic TBKT selftests on Fabric and NeoForge via gradlew runServer.
# Expects log line: TBKT_SELFTEST_RESULT=PASS
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Invoke-Selftest([string]$Loader) {
    Write-Host "=== TBKT selftest: $Loader ===" -ForegroundColor Cyan
    $log = Join-Path $root "build/selftest-$Loader.log"
    New-Item -ItemType Directory -Force -Path (Split-Path $log) | Out-Null

    & .\gradlew.bat ":${Loader}:runSelftest" --console=plain *>&1 | Tee-Object -FilePath $log
    $exit = $LASTEXITCODE

    $content = Get-Content $log -Raw
    if ($content -notmatch 'TBKT_SELFTEST_RESULT=PASS') {
        Write-Host "FAIL: $Loader missing TBKT_SELFTEST_RESULT=PASS (exit=$exit)" -ForegroundColor Red
        if ($content -match 'TBKT_SELFTEST_RESULT=FAIL[^\r\n]*') {
            Write-Host $Matches[0] -ForegroundColor Red
        }
        exit 1
    }
    Write-Host "PASS: $Loader" -ForegroundColor Green
}

Invoke-Selftest 'fabric'
Invoke-Selftest 'neoforge'
Write-Host "All TBKT selftests passed." -ForegroundColor Green
