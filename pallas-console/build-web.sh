#!/bin/bash

cp -rf ../pallas-plus-web/* ../pallas-console/src/main/resources/static/
cd ../pallas-console-web
yarn build-jar
