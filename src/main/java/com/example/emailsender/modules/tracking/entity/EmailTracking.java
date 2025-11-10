package com.example.emailsender.modules.tracking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EmailTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String recipient;

    private LocalDateTime sentAt;

    private LocalDateTime openedAt;

    @Column(name = "open_count")
    private int openCount = 0;

    public EmailTracking() {}

    public EmailTracking(String token, String recipient) {
        this.token = token;
        this.recipient = recipient;
        this.sentAt = LocalDateTime.now();
    }

    // getters y setters…


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public int getOpenCount() {
        return openCount;
    }

    public void setOpenCount(int openCount) {
        this.openCount = openCount;
    }
}
