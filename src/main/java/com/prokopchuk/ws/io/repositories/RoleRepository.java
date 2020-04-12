package com.prokopchuk.ws.io.repositories;

import com.prokopchuk.ws.io.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    RoleEntity findByName(String name);
}
