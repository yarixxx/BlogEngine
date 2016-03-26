package com.yarixlab;

public enum ContextParameters {
    DATA_BASE("mongoDatabase"),
    COLLECTION("mongoCollection"),
    TEMPLATES_PATH("templatePath"),
    TEMPLATE("template"),
    BLOG_TITLE("blogTitle")
    ;

    private final String param;

    ContextParameters(final String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return param;
    }
}
