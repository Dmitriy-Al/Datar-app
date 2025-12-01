package ru.alimovdev.datar.model;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentRepository extends CrudRepository <Appointment, Long> {

    List<Appointment> findBySpecialistId(long specialistId);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByOwnerId(long ownerId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM appointments WHERE owner_Id = :ownerId", nativeQuery = true)
    void removeAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT * FROM appointments WHERE owner_Id = :ownerId AND wait_near_appointment = true", nativeQuery = true)
    List<Appointment> findByAwaitAppointment(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT * FROM appointments WHERE appointment_date_time LIKE :date || ' - %' AND time_zone = :timeZone", nativeQuery = true)
    List<Appointment> findAppointmentsByDateTime(@Param("date") String appointmentDate, @Param("timeZone") int timeZone);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM appointments WHERE appointment_date_time LIKE :date || ' - %' AND time_zone = :timeZone", nativeQuery = true)
    void removeAllByDateTime(@Param("date") String appointmentDate, @Param("timeZone") int timeZone);

    @Query(value = "SELECT * FROM appointments WHERE appointment_date_time LIKE :date || ' - %' AND send_time = :sendTime AND time_zone = :timeZone AND client_tg_id > :defaultTgId", nativeQuery = true)
    List<Appointment> findAppointmentByDateTimeZone(@Param("date") String appointmentDate, @Param("sendTime") int sendTime, @Param("timeZone") int timeZone, @Param("defaultTgId") long defaultTgId);



    @Query(value = "SELECT * FROM appointments WHERE appointment_date_time LIKE :date || ' - %' AND send_time = :sendTime AND client_tg_id > :defaultTgId", nativeQuery = true)
    List<Appointment> findAppointmentByDateTime(@Param("date") String appointmentDate, @Param("sendTime") int sendTime, @Param("defaultTgId") long defaultTgId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM appointments WHERE appointment_date_time LIKE :date || ' - %'", nativeQuery = true)
    void removeAllByDate(@Param("date") String appointmentDate);


    @Query(value = "SELECT * FROM appointments WHERE appointment_date_time LIKE :date || ' - %' AND client_tg_id > :defaultTgId", nativeQuery = true)
    List<Appointment> findAppointmentByDate(@Param("date") String appointmentDate, @Param("defaultTgId") long defaultTgId);
}
