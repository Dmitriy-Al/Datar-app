package ru.alimovdev.datar.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alimovdev.datar.model.Administrator;
import ru.alimovdev.datar.model.AdministratorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//  Создание REST контроллера
@RestController
@RequestMapping("/api")
public class AdministratorController {
    @Autowired
    private AdministratorRepository adminRepository;

    // Обработка OPTIONS запросов для CORS (TODO добавил)
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/administrators")
    public List<Administrator> getAllAdministrators() {
        Iterable<Administrator> administrators = adminRepository.findAll();
        List<Administrator> administratorList = new ArrayList<>();
        administrators.forEach(administratorList::add); // TODO remake
        return administratorList;
    }

    // Добавить нового пользователя
    @PostMapping("/administrators")
    public Administrator addClient(@RequestBody Administrator administrator) {
        return adminRepository.save(administrator);
    }

    // Удалить пользователя
    @DeleteMapping("/administrators/{id}")
    public void deleteAdministrator(@PathVariable long id) {
        adminRepository.deleteById(id);
    }

    /*       */
    // Добавьте OPTIONS метод для CORS preflight
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        System.out.println("***************************   public ResponseEntity<?> options() {}   ***************************");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/administrators/{id}") // TODO добавил
    public ResponseEntity<Administrator> getAdministratorById(@PathVariable long id) {
        Optional<Administrator> administrator = adminRepository.findById(id);

        if (administrator.isPresent()) {
            return ResponseEntity.ok(administrator.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}