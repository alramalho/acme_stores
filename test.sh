#!/usr/bin/env bash
source set_env.sh

pushd backend
echo -e "Backend Testing"
./gradlew test || exit 1
popd

pushd frontend
echo -e "Frontend Testing"
yarn test || exit 1
popd

echo -e "✅ Great success!"
exit 0