package ru.alimovdev.datar.service;

public enum ConfirmAppointmentStatus {

    UNDEFINED("UNDEFINED"), // статус для неавторизованного пользователя
    CONFIRMED("CONFIRMED"), // статус для подтвердившего визит пользователя
    EXPECTANT("EXPECTANT"); // статус для ожидающего подтверждения визита пользователя

    // ConfirmAppointmentStatus.name()
    private final String statusLabel;

    ConfirmAppointmentStatus(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatusLabel() {
        return statusLabel;
    }


}
