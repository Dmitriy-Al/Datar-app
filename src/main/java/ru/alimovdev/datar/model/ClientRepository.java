package ru.alimovdev.datar.model;

import org.springframework.data.repository.CrudRepository;
import java.util.Collection;
import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Long> {

    List<Client> findByOwnerId(String ownerId);

    List<Client> findByIdIn(Collection<Long> ids);
}
