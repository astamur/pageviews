# Top Pages Finder
This streaming application flow calculates top N pages based on users stream and page-views stream.

### Build and run
Build a project and a docker container:
```
./gradlew build buildDockerImage
```

Start a docker compose environment:
```
docker-compose up -d
```

If you want to change some application properties, you can set them in the `docker-compose.yml` for the container `pageviews`. 

### Check results
Wait for a couple of minutes after start and try to read a `top-pages` topic:
```
docker-compose exec schema-registry kafka-avro-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic top-pages \
    --key-deserializer org.apache.kafka.common.serialization.StringDeserializer \
    --property print.key=true \
    --from-beginning
```

Also you can read `users` topic:
```
docker-compose exec schema-registry kafka-avro-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic users \
    --key-deserializer org.apache.kafka.common.serialization.StringDeserializer \
    --property print.key=true \
    --from-beginning
```

and `pageviews` topic:
```
docker-compose exec schema-registry kafka-avro-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic pageviews \
    --key-deserializer org.apache.kafka.common.serialization.StringDeserializer \
    --property print.key=true \
    --from-beginning
```