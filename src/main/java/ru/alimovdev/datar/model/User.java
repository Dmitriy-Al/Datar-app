package ru.alimovdev.datar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@lombok.Setter
@lombok.Getter
@Entity(name = "users")
public class User {

    @Id
    private long id;

    private String tgName;

    @Override
    public String toString() {
        return " < User name: " + tgName + ", id = " + id + "> ";
    }
}