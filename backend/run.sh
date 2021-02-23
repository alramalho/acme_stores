#!/usr/bin/env bash
source set_env.sh

./gradlew build

pushd build/libs > /dev/null

java -jar backend-1.0-SNAPSHOT.jar
popd >/dev/null