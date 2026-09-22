$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Maven 3.6.3+ is required. Install Maven or use the Maven wrapper.' }
Start-Process -FilePath 'mvn' -ArgumentList '-pl','backend','spring-boot:run' -WorkingDirectory $root
Start-Process -FilePath 'npm.cmd' -ArgumentList '--prefix','frontend','run','dev' -WorkingDirectory $root
Write-Host 'ReleaseLens is starting at http://localhost:3000 (backend http://127.0.0.1:8080)'
