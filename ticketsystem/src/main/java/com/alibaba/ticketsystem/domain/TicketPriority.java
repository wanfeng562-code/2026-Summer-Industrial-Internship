package com.alibaba.ticketsystem.domain;

public enum TicketPriority {
    LOW(72),
    MEDIUM(48),
    HIGH(24),
    URGENT(4);

    private final int defaultSlaHours;

    TicketPriority(int defaultSlaHours) {
        this.defaultSlaHours = defaultSlaHours;
    }

    public int getDefaultSlaHours() {
        return defaultSlaHours;
    }

    public TicketPriority next() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH, URGENT -> URGENT;
        };
    }

    public static TicketPriority from(String value) {
        return value == null || value.isBlank() ? MEDIUM : valueOf(value);
    }
}
