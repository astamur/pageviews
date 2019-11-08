package dev.astamur.streaming.pageviews;

import dev.astamur.streaming.pageviews.config.PageviewsProperties;
import dev.astamur.streaming.pageviews.utils.PriorityQueueSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProcessingStream implements DisposableBean, InitializingBean {
    private static final String USERS_STORE = "users-store";
    private static final String AGGREGATED_PAGE_VIEWS_STORE = "aggregated-pageviews-store";

    private static final String GENDER_FIELD = "gender";
    private static final String USER_ID_FIELD = "user_id";
    private static final String PAGE_ID_FIELD = "page_id";
    private static final String VIEW_TIME = "view_time";
    private static final String TOTAL_VIEW_TIME = "total_view_time";
    private static final String USERS = "users";
    private static final String UNIQUE_USERS = "unique_users";
    private static final String KEY_FORMAT = "%s:%s";

    private final Properties streamsConfig;
    private final PageviewsProperties properties;
    private final Serde<String> keySerde;
    private final Serde<GenericRecord> valueSerde;
    private final Serde<Windowed<String>> windowedStringSerde;
    private final DateTimeFormatter formatter;

    private KafkaStreams streams;

    public ProcessingStream(Properties streamsConfig,
                            PageviewsProperties properties,
                            Serde<String> keySerde,
                            Serde<GenericRecord> valueSerde) {
        this.streamsConfig = streamsConfig;
        this.properties = properties;
        this.keySerde = keySerde;
        this.valueSerde = valueSerde;

        this.windowedStringSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class);
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        final StreamsBuilder builder = new StreamsBuilder();

        // Read input topics
        final GlobalKTable<String, GenericRecord> usersTable = builder.globalTable(properties.getUsersTopic(),
                Materialized.<String, GenericRecord, KeyValueStore<Bytes, byte[]>>as(USERS_STORE)
                        .withKeySerde(keySerde)
                        .withValueSerde(valueSerde));
        final KStream<String, GenericRecord> pageViewsStream = builder.stream(properties.getPageviewsTopic());


        final KStream<String, GenericRecord> usersPageViews = joinUsersAndPageViews(pageViewsStream, usersTable);
        final KTable<Windowed<String>, GenericRecord> pageViewsStats = aggregateByGenderAndPageId(usersPageViews);
        final KStream<String, GenericRecord> topPagesStream = getTopPagesByGender(pageViewsStats, properties.getTopSize());
        topPagesStream.to(properties.getTopPagesTopic());

        streams = new KafkaStreams(builder.build(), streamsConfig);

        streams.cleanUp();
        streams.start();
    }

    @Override
    public void destroy() {
        this.streams.close();
    }

    private KStream<String, GenericRecord> joinUsersAndPageViews(KStream<String, GenericRecord> pageViewsStream,
                                                                 GlobalKTable<String, GenericRecord> usersTable) throws IOException {
        final Schema schema = loadSchema(properties.getUsersPageviewsSchemaLocation());

        return pageViewsStream
                .join(usersTable,
                        (pageViewId, pageView) -> pageView.get(USER_ID_FIELD) != null
                                ? pageView.get(USER_ID_FIELD).toString()
                                : "",
                        (pageView, user) -> {
                            final GenericRecord record = new GenericData.Record(schema);
                            record.put(GENDER_FIELD, user.get(GENDER_FIELD));
                            record.put(PAGE_ID_FIELD, pageView.get(PAGE_ID_FIELD));
                            record.put(VIEW_TIME, pageView.get(VIEW_TIME));
                            record.put(USER_ID_FIELD, user.get(USER_ID_FIELD));
                            return record;
                        });
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    private KTable<Windowed<String>, GenericRecord> aggregateByGenderAndPageId(
            KStream<String, GenericRecord> usersPageViews) throws IOException {

        final Schema aggRecordSchema = loadSchema(properties.getAggregatedPageviewSchemaLocation());

        return usersPageViews
                .groupBy((userId, pageView) -> String.format(KEY_FORMAT,
                        pageView.get(GENDER_FIELD), pageView.get(PAGE_ID_FIELD)))
                .windowedBy(TimeWindows
                        .of(Duration.ofSeconds(properties.getWindowSizeInSeconds()))
                        .advanceBy(Duration.ofSeconds(properties.getAdvanceSizeInSeconds())))
                .aggregate(() -> {
                            final GenericRecord record = new GenericData.Record(aggRecordSchema);
                            record.put(GENDER_FIELD, "");
                            record.put(PAGE_ID_FIELD, 0L);
                            record.put(TOTAL_VIEW_TIME, 0L);
                            record.put(USERS, new HashMap<>());
                            return record;
                        },
                        (key, pageView, agg) -> {
                            agg.put(GENDER_FIELD, pageView.get(GENDER_FIELD));
                            agg.put(PAGE_ID_FIELD, pageView.get(PAGE_ID_FIELD));
                            agg.put(TOTAL_VIEW_TIME, (Long) agg.get(TOTAL_VIEW_TIME) + (Long) pageView.get(VIEW_TIME));
                            ((Map<String, Boolean>) agg.get(USERS)).put(pageView.get(USER_ID_FIELD).toString(), true);
                            return agg;
                        },
                        Materialized.as(AGGREGATED_PAGE_VIEWS_STORE)
                );
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    private KStream<String, GenericRecord> getTopPagesByGender(KTable<Windowed<String>,
            GenericRecord> pageViewsStats, int top) throws IOException {

        final Schema topPageSchema = loadSchema(properties.getTopPageSchemaLocation());

        final Comparator<GenericRecord> comparator =
                (o1, o2) -> (int) ((Long) o2.get(TOTAL_VIEW_TIME) - (Long) o1.get(TOTAL_VIEW_TIME));

        return pageViewsStats
                .groupBy(
                        (windowedKey, aggPageView) -> {
                            final Windowed<String> windowedGenderKey = new Windowed<>(
                                    windowedKey.key().split(":")[0], windowedKey.window());

                            final GenericRecord topPage = new GenericData.Record(topPageSchema);
                            topPage.put(GENDER_FIELD, aggPageView.get(GENDER_FIELD));
                            topPage.put(PAGE_ID_FIELD, aggPageView.get(PAGE_ID_FIELD));
                            topPage.put(TOTAL_VIEW_TIME, aggPageView.get(TOTAL_VIEW_TIME));
                            topPage.put(UNIQUE_USERS, (long) ((Map<String, Boolean>) aggPageView.get(USERS)).size());

                            return new KeyValue<>(windowedGenderKey, topPage);
                        },
                        Grouped.with(windowedStringSerde, valueSerde)
                ).aggregate(
                        () -> new PriorityQueue<>(comparator),
                        (windowedGenderKey, aggPageView, queue) -> {
                            queue.add(aggPageView);
                            return queue;
                        },
                        (windowedGenderKey, aggPageView, queue) -> {
                            queue.remove(aggPageView);
                            return queue;
                        },
                        Materialized.with(windowedStringSerde, new PriorityQueueSerde<>(comparator, valueSerde))
                )
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(properties.getSuppressTimeoutInSeconds()),
                        Suppressed.BufferConfig.unbounded()))
                .toStream()
                .flatMap((key, queue) -> {
                    if (queue == null) {
                        return Collections.emptyList();
                    }

                    return IntStream.range(0, top)
                            .mapToObj(i -> queue.poll())
                            .filter(Objects::nonNull)
                            .map(topPage -> new KeyValue<>(String.format(KEY_FORMAT,
                                    formatter.format(key.window().startTime()), key.key()),
                                    topPage))
                            .collect(Collectors.toList());
                });
    }

    private static Schema loadSchema(String location) throws IOException {
        return new Schema.Parser().parse(ProcessingStream.class.getClassLoader().getResourceAsStream(location));
    }
}