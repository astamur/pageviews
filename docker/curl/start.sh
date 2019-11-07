#! /bin/bash
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' connect:8083)" != "200" ]]
    do sleep 5
done

# Wait while all required are being created
sleep 10

curl -X POST -H "Content-Type: application/json" -d @opt/datagen-users.json http://connect:8083/connectors
curl -X POST -H "Content-Type: application/json" -d @opt/datagen-pageviews.json http://connect:8083/connectors