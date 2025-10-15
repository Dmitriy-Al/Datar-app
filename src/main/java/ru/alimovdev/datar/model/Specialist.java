package ru.alimovdev.datar.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@lombok.Setter
@lombok.Getter
@Entity(name = "specialists")
public class Specialist {

    @Id
    private long id;

    private String specialistId; // идентификационный номер специалиста

    private String profession;

    private String name;

    private String surname;

    private String patronymic;

    private String receptionSchedule;

    private String administratorIdList;

    private String phoneNumber;

    private String password;

    private boolean isAdminPermission; // дополнительные полномочия для администратора

    private String workTimeLength; // рабочий день

    private String clientAppointmentRange; // возможность самостоятельной записи для клиента

    private int timeZone; // временная зона

    private int messageForClientTime; // время рассылки сообщений

    @Override
    public String toString() {
        return " < Specialist name: " + name + ", id = " + id + "> ";
    }
}