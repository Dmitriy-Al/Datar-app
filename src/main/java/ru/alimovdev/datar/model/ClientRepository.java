package ru.alimovdev.datar.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Long> {

    List<Client> findByOwnerId(String ownerId);

}
