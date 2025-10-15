package ru.alimovdev.datar.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alimovdev.datar.model.User;
import ru.alimovdev.datar.model.UserRepository;

import java.util.ArrayList;
import java.util.List;

//  Создание REST контроллера
@RestController
@RequestMapping("/api")
//  @CrossOrigin(origins = {"allowedOriginPatterns", "https://yourusername.github.io"}) TODO is it need?
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Обработка OPTIONS запросов для CORS (TODO добавил)
    //  @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    @RequestMapping(value = "/*", method = RequestMethod.OPTIONS)//(TODO добавил)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {

        Iterable <User> users = userRepository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add); // TODO remake
        return userList;
    }

    // Добавить нового пользователя User
    @PostMapping("/users")
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);
    }


    // Удалить пользователя
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable long id) {
        userRepository.deleteById(id);
    }

/*

    // Добавьте OPTIONS метод для CORS preflight
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        System.out.println("***************************   public ResponseEntity<?> options() {}   ***************************");
        return ResponseEntity.ok().build();
    }
*/
}