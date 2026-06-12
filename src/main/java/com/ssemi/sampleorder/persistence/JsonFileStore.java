package com.ssemi.sampleorder.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonFileStore<T> {
    private final Path file;
    private final Class<T[]> arrayType;
    private final ObjectMapper objectMapper;

    public JsonFileStore(Path file, Class<T[]> arrayType) {
        this.file = file;
        this.arrayType = arrayType;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<T> load() {
        try {
            if (Files.notExists(file)) {
                return new ArrayList<>();
            }
            String json = Files.readString(file);
            if (json.isBlank()) {
                return new ArrayList<>();
            }
            T[] values = objectMapper.readValue(json, arrayType);
            return new ArrayList<>(Arrays.asList(values));
        } catch (IOException exception) {
            throw new IllegalStateException("데이터 파일을 읽을 수 없습니다: " + file, exception);
        }
    }

    public void saveAll(List<T> values) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), values);
        } catch (IOException exception) {
            throw new IllegalStateException("데이터 파일을 저장할 수 없습니다: " + file, exception);
        }
    }
}
