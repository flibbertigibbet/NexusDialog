#!/usr/bin/env bash

echo 'assembling now...'
./real_gradlew assemble --continue

sleep 1
echo 'now going to run real task...'
./real_gradlew "$@" --continue

echo 'all done!'
