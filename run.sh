#!/usr/bin/env bash
source set_env.sh

_term() {
  echo "🔥  Fire crash burn  🔥"
  kill -9 $(lsof -i tcp:7000 | awk 'NR > 1 {print $2}')
}

echo "Building backend"
pushd backend
./run.sh &
popd
trap _term INT

echo "Executing frontend"

pushd frontend
./run.sh
popdch