package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.OfficeWorkstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeWorkstationRepository extends JpaRepository<OfficeWorkstation,Long> {
}
