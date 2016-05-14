package com.yarixlab;

public enum PostStatus {
    PUBLISHED("PUBLISHED"), REVIEW("REVIEW"), DRAFT("DRAFT"), ALL("ALL");

    private String status;

    PostStatus(String status) {
        this.status = status;
    }
}
