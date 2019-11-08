package dev.astamur.streaming.pageviews;

import dev.astamur.streaming.pageviews.config.PageviewsProperties;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @ConfigurationProperties("pageviews")
    public PageviewsProperties pageviewsProperties() {
        return new PageviewsProperties();
    }

    @Bean
    public Serde<String> keySerde() {
        return Serdes.String();
    }

    @Bean
    public Serde<GenericRecord> valueSerde(PageviewsProperties properties) {
        GenericAvroSerde genericAvroSerde = new GenericAvroSerde();
        genericAvroSerde.configure(schemaRegistryConfig(properties.getSchemaRegistryUrl()), false);
        return genericAvroSerde;
    }


    @Bean
    public Properties streamsConfig(PageviewsProperties properties) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, properties.getApplicationName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, properties.getReplicationFactor());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getSchemaRegistryUrl());
        return props;
    }

    @Bean
    public ProcessingStream processingStream(Properties streamsConfig,
                                             PageviewsProperties properties,
                                             Serde<String> keySerde,
                                             Serde<GenericRecord> valueSerde) {
        return new ProcessingStream(streamsConfig, properties, keySerde, valueSerde);
    }

    private Map<String, Object> schemaRegistryConfig(String schemaRegistryUrl) {
        return new HashMap<String, Object>() {{
            put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
            put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        }};
    }
}