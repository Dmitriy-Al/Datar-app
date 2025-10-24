package ru.alimovdev.datar.restcontroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alimovdev.datar.model.Client;
import ru.alimovdev.datar.model.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//  Создание REST контроллера
@RestController
@RequestMapping("/api")
public class ClientController {
    @Autowired
    private ClientRepository clientRepository;

    // Обработка OPTIONS запросов для CORS (TODO добавил)
    // to {OPTIONS [/api/**]}: There is already 'administratorController' bean method
    @RequestMapping(value = "/****", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/clients")
    public List<Client> getAllClients() {
        Iterable<Client> clients = clientRepository.findAll();
        List<Client> clientList = new ArrayList<>();
        clients.forEach(clientList::add); // TODO remake
        return clientList;
    }

    // Добавить нового пользователя
    @PostMapping("/clients")
    public Client addClient(@RequestBody Client client) {
        return clientRepository.save(client);
    }

    // Удалить пользователя
    @DeleteMapping("/clients/{id}")
    public void deleteClient(@PathVariable long id) {
        clientRepository.deleteById(id);
    }

    @GetMapping("/clients/{id}") // TODO добавил
    public ResponseEntity<Client> getClientById(@PathVariable long id) {
        Optional<Client> client = clientRepository.findById(id);

        if (client.isPresent()) {
            return ResponseEntity.ok(client.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Поиск специалистов по specialistId (не по ID!)// TODO добавил для поиска элементов по specialistId
    @GetMapping("/clients/by-specialist-id/{specialistId}")
    public List<Client> getClientsBySpecialistId(@PathVariable String specialistId) {
        return clientRepository.findByOwnerId(specialistId);
    }


    @PatchMapping("/clients/{id}")
    public ResponseEntity<Client> partialUpdateClient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        try {
            Optional<Client> clientOptional = clientRepository.findById(id);

            if (clientOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Client client = clientOptional.get();

            // Обновляем только те поля, которые пришли в запросе
            updates.forEach((key, value) -> {
                switch (key) {
                    // case "name" -> client.setName((String) value); // Возможна реализация в будущем
                    // case "surname" -> client.setSurname((String) value); // Возможна реализация в будущем
                    // case "patronymic" -> client.setPatronymic((String) value); // Возможна реализация в будущем
                    // case "phoneNumber" -> client.setPhoneNumber((String) value); // Возможна реализация в будущем
                    // case "clientNotes" -> client.setClientNotes((String) value); // Возможна реализация в будущем
                    //  case "appointmentDateTime" -> client.setAppointmentDateTime((String) value);
                    //  case "appointmentNote" -> client.setAppointmentNote((String) value);
                }
            });

            Client updatedClient = clientRepository.save(client);
            return ResponseEntity.ok(updatedClient);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/clients/by-owner-id/{ownerId}")// TODO добавил 21.10.2025
    public ResponseEntity<List<Client>> getClientsByOwnerId(@PathVariable String ownerId) {
        try {
            List<Client> clients = clientRepository.findByOwnerId(ownerId);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}