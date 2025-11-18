package ru.alimovdev.datar.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AdministratorRepository extends CrudRepository<Administrator, Long> {

    List<Administrator> findByOwnerId(String ownerId);

}