package com.ssemi.sampleorder.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class JsonObjectStore<T> {
    private final Path file;
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    public JsonObjectStore(Path file, Class<T> type) {
        this.file = file;
        this.type = type;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Optional<T> load() {
        try {
            if (Files.notExists(file)) {
                return Optional.empty();
            }
            String json = Files.readString(file);
            if (json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (IOException exception) {
            throw new IllegalStateException("데이터 파일을 읽을 수 없습니다: " + file, exception);
        }
    }

    public void save(T value) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), value);
        } catch (IOException exception) {
            throw new IllegalStateException("데이터 파일을 저장할 수 없습니다: " + file, exception);
        }
    }
}
