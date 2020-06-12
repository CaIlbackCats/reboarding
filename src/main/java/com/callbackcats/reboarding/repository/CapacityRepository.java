package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.Capacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapacityRepository extends JpaRepository<Capacity, Long> {


}
