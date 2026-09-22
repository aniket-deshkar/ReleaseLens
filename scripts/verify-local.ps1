$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Maven is required' }
mvn -pl backend -DskipTests=false test
npm.cmd --prefix (Join-Path $root 'frontend') run typecheck
Write-Host 'Offline verification completed: backend tests and frontend typecheck passed.'
