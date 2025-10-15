package ru.alimovdev.datar.model;

import jakarta.persistence.*;

@lombok.Setter
@lombok.Getter
@Entity(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long tgId;

    private String specialistId;
    private String name;

    private String surname;

    private String patronymic;

    @Column(columnDefinition = "varchar(1000000)")
    private String clientNotes; // заметки о клиенте

    private String appointmentDateTime; // время предстоящего приема

    @Column(columnDefinition = "varchar(1000000)")
    private String appointmentNote; // заметка о том, что предстоит сделать в рамках предстоящего приема

    private boolean waitNearAppointment; // отметка о том, что клиент готов перезаписаться на ближайшее свободное время приема (если true)

    @Column(columnDefinition = "varchar(1000000)")
    private String visitHistory; // история посещений

    private String phoneNumber;

    private String birthdate;

    private String visitDuration;

    private String confirmAppointment;

    @Override
    public String toString() {
        return " < client name: " + name + ", id = " + id + ">\n";
    }

}