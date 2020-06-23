package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.OfficeLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeLayoutRepository extends JpaRepository<OfficeLayout, Long> {
}
