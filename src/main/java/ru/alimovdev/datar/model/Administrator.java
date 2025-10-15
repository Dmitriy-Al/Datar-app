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

    @Column(columnDefinition = "varchar(1000000)")
    private String specialistIdList;

    private String name;

    private String surname;

    private String patronymic;

    private String phoneNumber;

    private String password;

    @Override
    public String toString() {
        return " < Administrator name: " + name + ", id = " + id + "> ";
    }

}