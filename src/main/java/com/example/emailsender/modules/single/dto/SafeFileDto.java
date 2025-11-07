package com.example.emailsender.modules.single.dto;

public class SafeFileDto {
    private String name;
    private String contentType;
    private byte[] bytes;

    public SafeFileDto(String name, String contentType, byte[] bytes) {
        this.name = name;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
