package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r from Reservation r where r.date= :reservationDate")
    Optional<Reservation> findReservationByDate(@Param("reservationDate") LocalDate reservationDate);

}
