package ru.alimovdev.datar.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppointmentRepository extends CrudRepository <Appointment, Long> {
    List<Appointment> findBySpecialistId(String specialistId);
    List<Appointment> findByClientId(Long clientId);
}
