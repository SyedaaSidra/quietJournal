package com.quietjournal.util;

public class SupabaseProperties {
    private final String url;
    private final String key;
    private final String bucket;

    public SupabaseProperties(String url, String key, String bucket) {
        this.url = url;
        this.key = key;
        this.bucket = bucket;
    }

    public String getUrl() { return url; }
    public String getKey() { return key; }
    public String getBucket() { return bucket; }
}

