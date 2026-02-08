package com.autoresafety.api.dto.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SourceType {
    MANUAL("manual"),
    STANDARD("standard"),
    REPO("repo"),
    PAPER("paper");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SourceType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SourceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid sourceType: " + value);
    }
}
