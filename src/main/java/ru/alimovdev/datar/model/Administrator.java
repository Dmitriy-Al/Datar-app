package ru.alimovdev.datar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@lombok.Setter
@lombok.Getter
@Entity(name = "administrators")
public class Administrator {

    @Id
    private long id;

    private String ownerId;

    @Column(columnDefinition = "varchar(1000000)")
    private String specialistIdList;

    private String currentSpecialistId;

    private String name;

    private String surname;

    private String patronymic;

    private String phoneNumber;

    private String password;

    private String organization;

    private boolean isOwner;

    private String workTimeLength; // рабочий день

    @Override
    public String toString() {
        return " < Administrator name: " + name + ", id = " + id + "> ";
    }

    public String receiveShortName() {
        return surname + " " +  name.charAt(0) + ". " + patronymic.charAt(0) + ". ";
    }

    public String receiveFullName() {
        return surname + " " +  name + " " + patronymic;
    }

}