package ru.alimovdev.datar.restcontroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alimovdev.datar.model.Specialist;
import ru.alimovdev.datar.model.SpecialistRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//  Создание REST контроллера
@RestController
@RequestMapping("/api")
public class SpecialistController {
    @Autowired
    private SpecialistRepository specialistRepository;

    // Обработка OPTIONS запросов для CORS (TODO добавил)
    @RequestMapping(value = "/*****", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/specialists")
    public List<Specialist> getAllSpecialist() {
        Iterable<Specialist> specialists = specialistRepository.findAll();
        List<Specialist> specialistList = new ArrayList<>();
        specialists.forEach(specialistList::add); // TODO remake
        return specialistList;
    }

    // Добавить нового пользователя
    @PostMapping("/specialists")
    public Specialist addCSpecialist(@RequestBody Specialist specialist) {
        return specialistRepository.save(specialist);
    }

    // Удалить пользователя
    @DeleteMapping("/specialists/{id}")

    public void deleteASpecialist(@PathVariable long id) {
        specialistRepository.deleteById(id);
    }

    @GetMapping("/specialists/{id}") // TODO добавил
    public ResponseEntity<Specialist> getSpecialistById(@PathVariable long id) {
        Optional<Specialist> specialist = specialistRepository.findById(id);

        if (specialist.isPresent()) {
            return ResponseEntity.ok(specialist.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/specialists/by-ids") // TODO добавил для поиска элементов по списку id
    public List<Specialist> getSpecialistsByIds(@RequestBody List<Long> ids) {
        Iterable<Specialist> specialists = specialistRepository.findAllById(ids);
        List<Specialist> specialistList = new ArrayList<>();
        specialists.forEach(specialistList::add);
        return specialistList;
    }

}