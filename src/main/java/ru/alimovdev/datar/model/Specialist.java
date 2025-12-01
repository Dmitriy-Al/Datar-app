package ru.alimovdev.datar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@lombok.Setter
@lombok.Getter
@Entity(name = "specialists")
public class Specialist {

    @Id
    private long id;

    private long tgId;

    private long ownerId;

    private String profession;

    private String name;

    private String surname;

    private String patronymic;

    private String subscribeData;

    private String useTime;

    private String ownSendText;

    private String receptionSchedule;

    private String phoneNumber;

    private String password;

    private boolean isOwner;

    private boolean isAdminPermission; // дополнительные полномочия для администратора

    private String workTimeLength; // рабочий день

    private String clientAppointmentRange; // возможность самостоятельной записи для клиента

    private int sendTime;

    private int timeZone; // временная зона


    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Administrator own_administrator;

     @ManyToMany(mappedBy = "specialists", fetch = FetchType.EAGER)
     @JsonIgnore
     private List<Administrator> administrators;

    /*
@JsonIgnore
Ошибка возникает из-за циклических ссылок в объектах, которые вы пытаетесь сериализовать в JSON.
Рассмотрим ваши сущности:
Administrator имеет два списка Specialist.
Specialist имеет ссылку на Administrator (поле own_administrator) и список Administrator (поле administrators).
При сериализации Administrator:
Он сериализует specialists_owners (каждый Specialist в этом списке имеет ссылку на Administrator через own_administrator).
Этот Administrator снова сериализует specialists_owners и так далее, пока не будет достигнута максимальная глубина.
Аналогично для specialists (хотя там связь @ManyToMany).
Как исправить?
Используйте аннотации Jackson для управления сериализацией и разрыва циклических ссылок. @JsonIgnore
     */

    @Override
    public String toString() {
        return " < Specialist name: " + name + ", id = " + id + "> ";
    }

    public String receiveShortName() {
        return surname + " " +  name.charAt(0) + ". " + patronymic.charAt(0) + ". ";
    }

    public String receiveFullName() {
        return surname + " " +  name + " " + patronymic;
    }


}


    /*
        @OneToMany (mappedBy = "specialists")
    private Administrator administrator;


    @ManyToMany(mappedBy = "specialists")
    private List<Administrator> administrators = new ArrayList<>();

    @OneToMany(mappedBy = "specialist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "specialist")
    private List<Client> clients = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "administrator_id")
    private Administrator administrator;

     */