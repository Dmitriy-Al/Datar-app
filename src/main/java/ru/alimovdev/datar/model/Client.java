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

    private long ownerId;

    private String name;

    private String surname;

    private String patronymic;

    @Column(columnDefinition = "varchar(1000000)")
    private String clientNotes; // заметки о клиенте

    @Column(columnDefinition = "varchar(1000000)")
    private String visitHistory; // история посещений

    private String phoneNumber;

    private String birthdate;

    private String confirmAppointment;

    @Override
    public String toString() {
        return " < client name: " + name + ", id = " + id + ">\n";
    }

    public String receiveShortName() {
        return surname + " " +  name.charAt(0) + ". " + patronymic.charAt(0) + ". ";
    }

    public String receiveFullName() {
        return surname + " " +  name + " " + patronymic;
    }

    public String receiveClientInfo() {
        String status = tgId > 0 ? "Статус:  ✓ авторизован" : "Статус:  ✘ не авторизован";
        return receiveFullName() + "\n" + status + "\n" + "Телефон:  " + phoneNumber + "\n" +
                "Дата рождения:  " + birthdate;
    }

    public String receiveVisitHistoryInfo() {
        return "История посещений:\n" + visitHistory;
    }


}