[CmdletBinding()]
param(
    [string]$Database = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'elder_ai_assistant' }),
    [string]$HostName = $(if ($env:DB_HOST) { $env:DB_HOST } else { '127.0.0.1' }),
    [int]$Port = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3306 }),
    [string]$User = $(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'root' }),
    [string]$Password = $env:DB_PASSWORD,
    [string]$OutputDirectory = '',
    [ValidateRange(1, 3650)]
    [int]$RetentionDays = 30
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($Password)) {
    throw 'DB_PASSWORD is required.'
}

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
    $OutputDirectory = Join-Path $scriptDirectory '..\sql\backups'
}

$dumpCommand = Get-Command 'mysqldump.exe' -ErrorAction SilentlyContinue
if (-not $dumpCommand) {
    $dumpCommand = Get-Command 'mysqldump' -ErrorAction SilentlyContinue
}
if (-not $dumpCommand) {
    throw 'mysqldump was not found. Add the MySQL bin directory to PATH.'
}

$outputRoot = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$safeDatabase = $Database -replace '[^A-Za-z0-9_-]', '_'
$backupPath = Join-Path $outputRoot ("{0}_{1}.sql" -f $safeDatabase, $timestamp)
$previousMysqlPassword = $env:MYSQL_PWD

try {
    $env:MYSQL_PWD = $Password
    $arguments = @(
        '--single-transaction',
        '--routines',
        '--events',
        '--triggers',
        '--hex-blob',
        '--default-character-set=utf8mb4',
        '--set-gtid-purged=OFF',
        '-h', $HostName,
        '-P', $Port,
        '-u', $User,
        $Database
    )

    if ($HostName -notmatch '^[A-Za-z0-9._-]+$' -or
        $User -notmatch '^[A-Za-z0-9_$-]+$' -or
        $Database -notmatch '^[A-Za-z0-9_$-]+$') {
        throw 'Database connection parameters contain unsupported characters.'
    }

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $dumpCommand.Source
    $startInfo.Arguments = ($arguments -join ' ')
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $null = $process.Start()
    $outputStream = [System.IO.File]::Create($backupPath)
    try {
        $copyTask = $process.StandardOutput.BaseStream.CopyToAsync($outputStream)
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $null = $copyTask.GetAwaiter().GetResult()
        $errorText = $errorTask.GetAwaiter().GetResult().Trim()
        $exitCode = $process.ExitCode
    }
    finally {
        $outputStream.Dispose()
        $process.Dispose()
    }

    if ($exitCode -ne 0) {
        if ([string]::IsNullOrWhiteSpace($errorText)) { $errorText = 'Unknown error' }
        throw "Database backup failed: $errorText"
    }

    $backupFile = Get-Item -LiteralPath $backupPath
    if ($backupFile.Length -lt 1024) {
        throw 'The backup file is unexpectedly small.'
    }
    if (-not (Select-String -LiteralPath $backupPath -Pattern 'CREATE TABLE' -Quiet)) {
        throw 'Backup verification failed: CREATE TABLE was not found.'
    }
    if (-not (Select-String -LiteralPath $backupPath -Pattern 'INSERT INTO' -Quiet)) {
        throw 'Backup verification failed: INSERT INTO was not found.'
    }

    $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $backupPath).Hash
    Write-Output ("Backup succeeded: {0}" -f $backupFile.FullName)
    Write-Output ("Size: {0} bytes" -f $backupFile.Length)
    Write-Output ("SHA256: {0}" -f $hash)

    $cutoff = (Get-Date).AddDays(-$RetentionDays)
    Get-ChildItem -LiteralPath $outputRoot -File -Filter ($safeDatabase + '_*.sql') |
        Where-Object { $_.LastWriteTime -lt $cutoff } |
        ForEach-Object {
            $candidate = [System.IO.Path]::GetFullPath($_.FullName)
            if ([System.IO.Path]::GetDirectoryName($candidate) -eq $outputRoot) {
                Remove-Item -LiteralPath $candidate -Force
            }
        }
}
finally {
    if ($null -eq $previousMysqlPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
    else {
        $env:MYSQL_PWD = $previousMysqlPassword
    }
}
