package ru.alimovdev.datar.model;

import jakarta.persistence.*;
import java.util.List;

@lombok.Setter
@lombok.Getter
@Entity(name = "administrators")
public class Administrator {

    @Id
    private long id;

    private long tgId;

    private long ownerId;

    private long currentSpecialistId;

    private String name;

    private String subscribeData;

    private String useTime;

    private String surname;

    private String patronymic;

    private String phoneNumber;

    private String ownSendText;

    private String password;

    private String organization;

    private boolean isOwner;

    private int sendTime;

    private int timeZone;

    private String workTimeLength; // рабочий день

    @OneToMany(mappedBy = "own_administrator", fetch = FetchType.EAGER)
    List<Specialist> specialists_owners;

   @ManyToMany(fetch = FetchType.EAGER)
    private List<Specialist> specialists;

    @Override
    public String toString() {
        return "•  " + receiveFullName() + "\n";
    }

    public String receiveShortName() {
        return surname + " " +  name.charAt(0) + ". " + patronymic.charAt(0) + ". ";
    }

    public String receiveFullName() {
        return surname + " " +  name + " " + patronymic;
    }

}

