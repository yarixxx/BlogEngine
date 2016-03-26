package com.yarixlab;

public enum TemplateFields {
    BLOG_TITLE("title"),
    POST("post"),
    POSTS("posts"),
    PAGE("page"),
    ALL_TAGS("allTags"),
    TOP_TAGS("topTags"),
    LATEST_POSTS("announce"),
    ARCHIVE("archive")
    ;

    private final String param;

    TemplateFields(final String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return param;
    }
}
