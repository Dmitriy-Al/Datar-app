package ru.alimovdev.datar.model;

import jakarta.persistence.*;

@lombok.Setter
@lombok.Getter
@Entity(name = "appointments")
public class Appointment implements Comparable<Appointment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String specialistId;

    private String ownerId;

    private long clientId;

    private String appointmentDateTime; // время предстоящего приема TODO

    @Column(columnDefinition = "varchar(1000000)")
    private String appointmentNote; // заметка о том, что предстоит сделать в рамках предстоящего приема

    @Override
    public String toString(){
        return " <Appointment" + ": specialistId = " + specialistId  + "; clientId = " + clientId  + "; DateTime = " + appointmentDateTime + ">\n";
    }

    @Override
    public int compareTo(Appointment o) {
        return (int) (Long.parseLong(this.appointmentDateTime.replaceAll("\\.", "").replaceAll(":","").
                replace(" - ","").replace("/","")) - Long.parseLong(o.getAppointmentDateTime().
                replaceAll("\\.", "").replaceAll(":","").replace(" - ","").replace("/","")));
    }

}
