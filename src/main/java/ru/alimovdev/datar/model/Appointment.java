package ru.alimovdev.datar.model;

import jakarta.persistence.*;

@lombok.Setter
@lombok.Getter
@Entity(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String specialistId;

    private String ownerId;

    private long clientId;

    private String appointmentDateTime; // время предстоящего приема TODO

    @Column(columnDefinition = "varchar(1000000)")
    private String appointmentNote; // заметка о том, что предстоит сделать в рамках предстоящего приема


}
