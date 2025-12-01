package ru.alimovdev.datar.model;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdministratorRepository extends CrudRepository<Administrator, Long> {



    List<Administrator> findByOwnerId(long ownerId);

    List<Administrator> findByCurrentSpecialistId(long specialistId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO administrators_specialists (administrators_id, specialists_id) " +
            "SELECT :administratorId, id FROM specialists", nativeQuery = true)
    void addAdministratorToAllSpecialists(@Param("administratorId") Long administratorId); // работает

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM administrators_specialists WHERE administrators_id = :administratorId", nativeQuery = true)
    void removeFromAllAdministrators(@Param("administratorId") Long administratorId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM administrators_specialists WHERE administrators_id = :ownerId", nativeQuery = true)
    void removeAllByOwnerId(@Param("ownerId") Long ownerId);

}

