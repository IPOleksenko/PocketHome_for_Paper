$MyInvocation.MyCommand.Path | Split-Path | Split-Path | Push-Location
./gradlew build

$MyInvocation.MyCommand.Path | Split-Path | Push-Location

$version = "1.19.3"
$build = 386
$file = "paper-$version-$build.jar"
$API_URI = "https://api.papermc.io/v2/projects/paper/versions/$version/builds/$build/downloads/$file"

if (-not(Test-Path -Path $file)) {
  Invoke-WebRequest -Uri $API_URI -OutFile $file
}

if (Test-Path -Path "./plugins/PocketHome-*[0-9].jar") {
  Remove-Item "./plugins/PocketHome-*[0-9].jar"
}

if (Test-Path -Path "../build/libs/PocketHome-*[0-9].jar") {
  Copy-Item "../build/libs/PocketHome-*[0-9].jar" -Destination "./plugins"
}

java -jar $file nogui
