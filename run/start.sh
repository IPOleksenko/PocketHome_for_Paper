#! /bin/sh

cd "$(dirname "$0")"/.. || exit 0
if ./gradlew build; then
  echo
else
  exit 0
fi
cd "$(dirname "$0")" || exit 0

plugin_wildcard="PocketHome-*[0-9].jar"

version=1.19.3
build=386
file=paper-${version}-${build}.jar
API_URI=https://api.papermc.io/v2/projects/paper/versions/${version}/builds/${build}/downloads/${file}

if [ ! -f $file ]; then
  wget $API_URI
fi

if test -n "$(find plugins/ -maxdepth 1 -name "$plugin_wildcard" -print -quit)"; then
  rm plugins/$plugin_wildcard
fi

if test -n "$(find ../build/libs/ -maxdepth 1 -name "$plugin_wildcard" -print -quit)"; then
  cp "$(find ../build/libs/ -name "${plugin_wildcard}" -print0 | xargs -r -0 ls -1 -t | head -1)" plugins/
fi

java \
  -Xms4G -Xmx4G -Djava.net.preferIPv4Stack=true --add-modules=jdk.incubator.vector -XX:+UseG1GC \
  -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 \
  -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true \
  -jar $file nogui