package com.nntan041299.englishmasterservice.ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AiPromptManager {

    private static final String PROMPT_FILE = "classpath:prompts/ai-promt.properties";

    private final Map<AiPromptKey, String> prompts = new EnumMap<>(AiPromptKey.class);

    public AiPromptManager(ResourceLoader resourceLoader) {
        Properties properties = loadProperties(resourceLoader);
        for (AiPromptKey key : AiPromptKey.values()) {
            String value = properties.getProperty(key.getPropertyKey());
            if (value == null) {
                throw new IllegalStateException("Missing AI prompt for key: " + key.getPropertyKey());
            }
            prompts.put(key, value);
        }
    }

    public String get(AiPromptKey key) {
        return prompts.get(key);
    }

    private Properties loadProperties(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(PROMPT_FILE);
        try (InputStream inputStream = resource.getInputStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load AI prompt file: " + PROMPT_FILE, ex);
        }
    }
}
