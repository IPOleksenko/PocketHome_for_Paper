#! /bin/sh

cd "$(dirname "$0")"/.. || exit 0
if ./gradlew build; then
  echo
else
  exit 0
fi
cd "$(dirname "$0")" || exit 0

version=1.19.3
api=https://api.papermc.io/v2/
plugin_version="$(git --no-pager describe --always)"

if [ ! -f "server.jar" ]; then
	latest_build="$(curl -sX GET "$api"/projects/paper/versions/"$version"/builds -H 'accept: application/json' | jq '.builds [-1].build')"
	download_url="$api"/projects/paper/versions/"$version"/builds/"$latest_build"/downloads/paper-"$version"-"$latest_build".jar
  if ! wget "$download_url" -O server.jar; then
  	exit 0
	fi
fi


rm plugins/PocketHome-*
cp "../build/libs/PocketHome-$plugin_version.jar" plugins/

java \
  -Xms4G -Xmx4G -Djava.net.preferIPv4Stack=true --add-modules=jdk.incubator.vector -XX:+UseG1GC \
  -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 \
  -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true \
  -jar server.jar nogui
