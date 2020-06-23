package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.OfficeOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CapacityRepository extends JpaRepository<OfficeOptions, Long> {

    @Query("select c from OfficeOptions c where c.startDate<= :reservationDate and c.endDate>= :reservationDate")
    Optional<OfficeOptions> findCapacityByReservationDate(@Param("reservationDate") LocalDate date);
}
