xcopy /y /e /s ..\pallas-plus-web ..\pallas-console\src\main\resources\static\
cd ../pallas-console-web
yarn build-jar
