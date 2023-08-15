$version = "0.1.0"
$name = "window-tiler-$version-SNAPSHOT"
$latestVersionUrl = "https://github.com/marad/tillr/releases/download/latest/$name.zip"

function Kill-And-Get-Cmd {
    
    [CmdletBinding()]
    param()

    $tillr = get-process -Name "java" -ErrorAction SilentlyContinue | where-object {$_.CommandLine -like "*window-tiler*"}
    $tillrPid = ($tillr).Id
    $tillrCmd = ($tillr).CommandLine

    if  ($tillrPid -like "") {
        Write-verbose -Message "App was not running"
    } else {
        Write-Verbose -Message "App is running, shutting it down"
        taskkill /F /PID $tillrPid | Out-Null
    }

    return $tillrCmd
}


$tillrCmd = Kill-And-Get-Cmd -Verbose

Push-Location $PSScriptRoot
if (Test-Path -Path tmp) {
    Remove-Item -Recurse -Force tmp
}
mkdir tmp
Invoke-WebRequest -Uri $latestVersionUrl -OutFile "tmp/dist.zip"
Expand-Archive -Path tmp/dist.zip  -DestinationPath "tmp/"

# Verify that download contained what it should
if (Test-Path -Path "tmp/$name/bin/window-tiler.bat") {
    Write-Output "Updating bin..."
    Remove-Item -Recurse -Force bin -ErrorAction SilentlyContinue
    Copy-Item -Recurse -Path "tmp/$name/bin" -Destination "."
}

if (Test-Path -Path "tmp/$name/lib") {
    Write-Output "Updating lib..."
    Remove-Item -Recurse -Force lib -ErrorAction SilentlyContinue
    Copy-Item -Recurse -Path "tmp/$name/lib" -Destination "."
}
Remove-Item -Recurse -Force tmp
Pop-Location

# It would be cool to start Tillr again but I have no idea how...
# Maybe someday!

#$job = start-job { iex "& $tillrCmd" }
#Receive-Job -job $job
#& "cmd.exe /c 'start $($tillrCmd)'"