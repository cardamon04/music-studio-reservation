param(
  [string]$DbHost = $env:DB_HOST,
  [int]   $Port = $env:DB_PORT,
  [string]$Db   = $env:DB_NAME,
  [string]$User = $env:DB_USER,
  [string]$Password = $env:DB_PASSWORD
)

# psql command check
if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
  Write-Error 'psql command not found. Please add PostgreSQL bin to PATH.'
  exit 1
}

# Connection info check
if ([string]::IsNullOrWhiteSpace($DbHost) -or
    [string]::IsNullOrWhiteSpace($Port) -or
    [string]::IsNullOrWhiteSpace($Db)   -or
    [string]::IsNullOrWhiteSpace($User)) {
  Write-Error 'Connection info missing. Specify DbHost/Port/Db/User or set environment variables DB_HOST/DB_PORT/DB_NAME/DB_USER.'
  exit 1
}

# Password setting
if (-not [string]::IsNullOrWhiteSpace($Password)) {
  $env:PGPASSWORD = $Password
}

# Get .sql files from current directory (CREATE_TABLE folder)
$currentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$files = Get-ChildItem -Path $currentDir -Filter '*.sql' | Sort-Object Name

# Check execution target files
if ($files.Count -eq 0) {
  Write-Error 'No .sql files found in: ' + $currentDir
  exit 1
}

Write-Host '====================================='
Write-Host 'CREATE_TABLE Script Execution Start'
Write-Host '====================================='
Write-Host 'Target: ' + $User + '@' + $DbHost + ':' + $Port + '/' + $Db
Write-Host 'Execution Directory: ' + $currentDir
Write-Host 'File Count: ' + $files.Count
Write-Host '-------------------------------------'

# Execute each .sql file in order
foreach ($f in $files) {
  Write-Host '>> Executing: ' + $f.Name -ForegroundColor Yellow
  & psql --host=$DbHost --port=$Port --username=$User --dbname=$Db --set ON_ERROR_STOP=1 --file $f.FullName
  if ($LASTEXITCODE -ne 0) {
    Write-Error 'Execution failed: ' + $f.Name + '. Stopping execution.'
    if ($env:PGPASSWORD) { Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue }
    exit $LASTEXITCODE
  }
  Write-Host 'Completed: ' + $f.Name -ForegroundColor Green
}

Write-Host '-------------------------------------'
Write-Host 'CREATE_TABLE Script Execution Complete' -ForegroundColor Green
Write-Host '====================================='

# Cleanup password environment variable
if ($env:PGPASSWORD) { Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue }