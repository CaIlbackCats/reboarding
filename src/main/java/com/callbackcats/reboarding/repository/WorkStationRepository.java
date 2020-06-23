package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.WorkStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, Long> {
}
