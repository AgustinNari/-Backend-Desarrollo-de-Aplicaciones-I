package com.example.quickbid.quickbid.storage;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.files")
public record StorageProperties(Path storagePath) {
}
