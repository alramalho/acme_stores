#!/usr/bin/env bash
source set_env.sh

_term() {
  echo "🔥  Fire crash burn  🔥"
  kill -9 $(lsof -i tcp:7000 | awk 'NR > 1 {print $2}')
}
trap _term INT

./gradlew build

pushd build/libs > /dev/null

java -jar backend-1.0-SNAPSHOT.jar

popd >/dev/null