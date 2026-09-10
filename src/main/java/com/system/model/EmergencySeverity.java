package com.system.model;

public enum EmergencySeverity {
    CRITICAL(4), HIGH(3), MODERATE(2), NORMAL(1);

    private final int rank;

    EmergencySeverity(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}

