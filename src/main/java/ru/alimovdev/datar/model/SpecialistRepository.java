package ru.alimovdev.datar.model;

import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface SpecialistRepository extends CrudRepository<Specialist, Long> {


    @NonNull
    List<Specialist> findAll();

    List<Specialist> findByOwnerId(long ownerId);

    List<Specialist> findByIdIn(Collection<Long> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM administrators_specialists WHERE specialists_id = :specialistId", nativeQuery = true)
    void removeFromAllAdministrators(@Param("specialistId") Long specialistId);

    /*
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM administrators_specialists WHERE owner_id = :ownerId", nativeQuery = true) НЕПРАВИЛЬНЫЙ ЗАПРОС К НЕСУЩЕСТВУЮЩЕМУ ПОЛЮ owner_id
    void removeAllByOwnerId(@Param("ownerId") Long ownerId);
     */

    List<Specialist> findByOwnerIdIn(Collection<Long> ids);
}