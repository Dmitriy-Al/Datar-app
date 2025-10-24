package ru.alimovdev.datar.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alimovdev.datar.model.Appointment;
import ru.alimovdev.datar.model.AppointmentRepository;
import ru.alimovdev.datar.model.ClientRepository;

import java.util.List;

@RestController //"appointments"
@RequestMapping("/api")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;


    @Autowired
    private ClientRepository clientRepository;

    // Получение всех записей по specialistId
    @GetMapping("/appointments/by-specialist-id/{specialistId}")
    public ResponseEntity<List<Appointment>> getAppointmentsBySpecialistId(@PathVariable String specialistId) {
        try {
            List<Appointment> appointments = appointmentRepository.findBySpecialistId(specialistId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Создание новой записи
    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment savedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.ok(savedAppointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Обновление записи
    @PutMapping("/appointments/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

            appointment.setAppointmentDateTime(appointmentDetails.getAppointmentDateTime());
            appointment.setAppointmentNote(appointmentDetails.getAppointmentNote());

            Appointment updatedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Удаление записи
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Получение записи по ID
    @GetMapping("/appointments/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Получение всех записей для клиента
    @GetMapping("/appointments/by-client-id/{clientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByClientId(@PathVariable Long clientId) {
        try {
            List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}