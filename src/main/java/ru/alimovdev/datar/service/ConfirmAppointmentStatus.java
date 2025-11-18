package ru.alimovdev.datar.service;

public enum ConfirmAppointmentStatus {

    UNDEFINED("UNDEFINED"),
    CONFIRMED("CONFIRMED"),
    EXPECTANT("EXPECTANT"),
    UNCERTAIN("UNCERTAIN");

    // Аналог .name()
    private final String statusLabel;

    ConfirmAppointmentStatus(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatus() {
        return statusLabel;
    }


}
