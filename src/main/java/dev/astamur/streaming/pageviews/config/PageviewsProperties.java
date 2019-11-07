package dev.astamur.streaming.pageviews.config;

public class PageviewsProperties {
    private String applicationName = "pageviews";
    private String bootstrapServers = "localhost:9093";
    private String schemaRegistryUrl = "http://localhost:8081";

    private String usersTopic = "users";
    private String pageviewsTopic = "pageviews";
    private String topPagesTopic = "top-pages";

    private String usersPageviewsSchemaLocation = "avro/users_pageviews.avro";

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

    public String getTopPagesTopic() {
        return topPagesTopic;
    }

    public PageviewsProperties setTopPagesTopic(String topPagesTopic) {
        this.topPagesTopic = topPagesTopic;
        return this;
    }
}