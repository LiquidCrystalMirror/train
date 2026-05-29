package com.example.ticket.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SeatTypeEnum {
    SECOND_CLASS(0L, "二等座", 1.0),
    FIRST_CLASS(1L, "一等座", 1.2),
    BUSINESS_CLASS(2L, "商务座", 1.5);

    private final Long code;
    private final String description;
    private final Double priceMultiplier;

    SeatTypeEnum(Long code, String description, Double priceMultiplier) {
        this.code = code;
        this.description = description;
        this.priceMultiplier = priceMultiplier;
    }

    private static final Map<Long, SeatTypeEnum> CODE_MAP = new HashMap<>();
    private static final Map<String, SeatTypeEnum> DESC_MAP = new HashMap<>();

    static {
        for (SeatTypeEnum type : values()) {
            CODE_MAP.put(type.code, type);
            DESC_MAP.put(type.description, type);
        }
    }

    public static SeatTypeEnum fromCode(Long code) {
        return CODE_MAP.get(code);
    }

    public static SeatTypeEnum fromDescription(String description) {
        return DESC_MAP.get(description);
    }

    public static String getDescriptionByCode(Long code) {
        SeatTypeEnum type = fromCode(code);
        return type == null ? null : type.getDescription();
    }

    public static Long getCodeByDescription(String description) {
        SeatTypeEnum type = fromDescription(description);
        return type == null ? null : type.getCode();
    }
}