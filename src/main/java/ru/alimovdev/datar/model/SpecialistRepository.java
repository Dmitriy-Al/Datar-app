package ru.alimovdev.datar.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpecialistRepository extends CrudRepository<Specialist, Long> {

    List<Specialist> findByOwnerId(String ownerId);

}