package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r from Reservation r where r.userId= :userId")
    Reservation findByUserId(@Param("userId") String userId);
}
