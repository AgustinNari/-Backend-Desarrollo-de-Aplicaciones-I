package com.example.quickbid.quickbid.storage;

public record StoredFile(String storagePath, String checksum, long sizeBytes) {
}
