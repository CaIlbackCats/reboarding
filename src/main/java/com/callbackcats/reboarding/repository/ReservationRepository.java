package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.Reservation;
import com.callbackcats.reboarding.domain.ReservationType;
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

    @Query("select r from Reservation r where r.date= :reservationDate and r.reservationType= :reservationType")
    Optional<Reservation> findReservationByDateAndType(@Param("reservationDate") LocalDate reservationDate, @Param("reservationType") ReservationType reservationType);

}
