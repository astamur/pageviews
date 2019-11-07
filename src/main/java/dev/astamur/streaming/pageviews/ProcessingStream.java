package dev.astamur.streaming.pageviews;

import dev.astamur.streaming.pageviews.config.PageviewsProperties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProcessingStream implements DisposableBean, InitializingBean {
    private final Properties streamsConfig;
    private final PageviewsProperties properties;
    private KafkaStreams streams;

    public ProcessingStream(Properties streamsConfig,
                            PageviewsProperties properties) {
        this.streamsConfig = streamsConfig;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        final StreamsBuilder builder = new StreamsBuilder();

        // Input sources
        final KTable<String, GenericRecord> userTable = builder.table(properties.getUsersTopic());
        final KStream<String, GenericRecord> pageViewsStream = builder.stream(properties.getPageviewsTopic());

        // Parse joined message schema
        final InputStream schemaStream = ProcessingStream.class.getClassLoader()
                .getResourceAsStream(properties.getUsersPageviewsSchemaLocation());
        final Schema schema = new Schema.Parser().parse(schemaStream);

        pageViewsStream
                .selectKey((k, v) -> v.get("user_id") != null ? v.get("user_id").toString() : "")
                .leftJoin(userTable,
                        (user, pageView) -> {
                            final GenericRecord clone = new GenericData.Record(schema);
                            clone.put("gender", user.get("gender"));
                            clone.put("page_id", pageView.get("page_id"));
                            clone.put("view_time", pageView.get("view_time"));
                            clone.put("user_id", user.get("user_id"));
                            return clone;
                        })
                .to(properties.getTopPagesTopic());

        streams = new KafkaStreams(builder.build(), streamsConfig);

        streams.cleanUp();
        streams.start();
    }

    @Override
    public void destroy() {
        this.streams.close();
    }
}