$MyInvocation.MyCommand.Path | Split-Path | Push-Location

$version = "1.19.3"
$build = 386
$file = "paper-$version-$build.jar"
$API_URI = "https://api.papermc.io/v2/projects/paper/versions/$version/builds/$build/downloads/$file"

if (-not(Test-Path -Path $file)) {
  Invoke-WebRequest -Uri $API_URI -OutFile $file
}

if (Test-Path -Path "./plugins/PocketHome-*.jar") {
  Remove-Item "./plugins/PocketHome-*.jar"
}

if (Test-Path -Path "../build/libs/PocketHome-*.jar") {
  Copy-Item "../build/libs/PocketHome-*.jar" -Destination "./plugins"
}
else {
  Write-Host "Run './gradlew build' before launching a server"
  Exit
}

java `
  -Xms4G -Xmx4G -Djava.net.preferIPv4Stack=true --add-modules=jdk.incubator.vector -XX:+UseG1GC `
  -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC `
  -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M `
  -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 `
  -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 `
  -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 `
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true `
  -jar $file nogui
