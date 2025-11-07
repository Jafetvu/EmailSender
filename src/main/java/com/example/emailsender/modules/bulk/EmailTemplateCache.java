package com.example.emailsender.modules.bulk;



import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailTemplateCache {
    private final Map<String, BulkEmailService.ProcessedTemplate> cache = new ConcurrentHashMap<>();

    public BulkEmailService.ProcessedTemplate get(String key) {
        return cache.get(key);
    }

    public void put(String key, BulkEmailService.ProcessedTemplate template) {
        cache.put(key, template);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}