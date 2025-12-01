package ru.alimovdev.datar.model;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.CrudRepository;
import java.util.Collection;
import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Long> {


    List<Client> findByOwnerId(long ownerId);

    List<Client> findByIdIn(Collection<Long> ids);

    List<Client> findByTgId(long tgId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM clients c WHERE owner_Id = :ownerId", nativeQuery = true)
    void deleteAllByOwnerId(@Param("ownerId") long ownerId);

}
