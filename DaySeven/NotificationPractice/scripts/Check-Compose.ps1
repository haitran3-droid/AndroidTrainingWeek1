param([switch]$TestApk)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskTemp = Join-Path $taskRoot '.build-tmp'
New-Item -ItemType Directory -Path $taskTemp -Force | Out-Null
$taskPreviousTemp = $env:TEMP
$taskPreviousTmp = $env:TMP
$taskPreviousJavaOptions = $env:JAVA_TOOL_OPTIONS
Push-Location $taskRoot
try {
    $env:TEMP = $taskTemp
    $env:TMP = $taskTemp
    $taskJavaTemp = $taskTemp.Replace('\', '/')
    $env:JAVA_TOOL_OPTIONS = "$taskPreviousJavaOptions -Djdk.net.unixdomain.tmpdir=$taskJavaTemp".Trim()
    $taskTasks = @(':app:assembleDebug', ':app:lintDebug')
    if ($TestApk) { $taskTasks += ':app:assembleDebugAndroidTest' }
    & .\gradlew.bat @taskTasks --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally {
    Pop-Location
    $env:TEMP = $taskPreviousTemp
    $env:TMP = $taskPreviousTmp
    $env:JAVA_TOOL_OPTIONS = $taskPreviousJavaOptions
}