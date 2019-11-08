package dev.astamur.streaming.pageviews.config;

public class PageviewsProperties {
    private String applicationName = "pageviews";
    private String bootstrapServers = "localhost:9093";
    private String schemaRegistryUrl = "http://localhost:8081";

    private String usersTopic = "users";
    private String pageviewsTopic = "pageviews";
    private String topPagesTopic = "top-pages";

    private String usersPageviewsSchemaLocation = "avro/user_pageview.avro";
    private String aggregatedPageviewSchemaLocation = "avro/aggregated_pageview.avro";
    private String topPageSchemaLocation = "avro/top_page.avro";

    private int topSize = 10;
    private int windowSizeInSeconds = 60;
    private int advanceSizeInSeconds = 10;
    private int suppressTimeoutInSeconds = 60;

    private int replicationFactor = 1;
    private int threads = 1;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUsersTopic() {
        return usersTopic;
    }

    public PageviewsProperties setUsersTopic(String usersTopic) {
        this.usersTopic = usersTopic;
        return this;
    }

    public String getPageviewsTopic() {
        return pageviewsTopic;
    }

    public PageviewsProperties setPageviewsTopic(String pageviewsTopic) {
        this.pageviewsTopic = pageviewsTopic;
        return this;
    }

    public String getUsersPageviewsSchemaLocation() {
        return usersPageviewsSchemaLocation;
    }

    public PageviewsProperties setUsersPageviewsSchemaLocation(String usersPageviewsSchemaLocation) {
        this.usersPageviewsSchemaLocation = usersPageviewsSchemaLocation;
        return this;
    }

    public String getAggregatedPageviewSchemaLocation() {
        return aggregatedPageviewSchemaLocation;
    }

    public PageviewsProperties setAggregatedPageviewSchemaLocation(String aggregatedPageviewSchemaLocation) {
        this.aggregatedPageviewSchemaLocation = aggregatedPageviewSchemaLocation;
        return this;
    }

    public String getTopPageSchemaLocation() {
        return topPageSchemaLocation;
    }

    public PageviewsProperties setTopPageSchemaLocation(String topPageSchemaLocation) {
        this.topPageSchemaLocation = topPageSchemaLocation;
        return this;
    }

    public int getTopSize() {
        return topSize;
    }

    public PageviewsProperties setTopSize(int topSize) {
        this.topSize = topSize;
        return this;
    }

    public int getWindowSizeInSeconds() {
        return windowSizeInSeconds;
    }

    public PageviewsProperties setWindowSizeInSeconds(int windowSizeInSeconds) {
        this.windowSizeInSeconds = windowSizeInSeconds;
        return this;
    }

    public int getAdvanceSizeInSeconds() {
        return advanceSizeInSeconds;
    }

    public PageviewsProperties setAdvanceSizeInSeconds(int advanceSizeInSeconds) {
        this.advanceSizeInSeconds = advanceSizeInSeconds;
        return this;
    }

    public int getSuppressTimeoutInSeconds() {
        return suppressTimeoutInSeconds;
    }

    public PageviewsProperties setSuppressTimeoutInSeconds(int suppressTimeoutInSeconds) {
        this.suppressTimeoutInSeconds = suppressTimeoutInSeconds;
        return this;
    }

    public String getTopPagesTopic() {
        return topPagesTopic;
    }

    public PageviewsProperties setTopPagesTopic(String topPagesTopic) {
        this.topPagesTopic = topPagesTopic;
        return this;
    }
}