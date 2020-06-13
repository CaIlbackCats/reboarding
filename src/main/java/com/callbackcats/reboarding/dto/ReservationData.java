package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationData {

    private String reservationType;

    private LocalDate date;

    public ReservationData(Reservation reservation) {
        this.date = reservation.getDate();
        this.reservationType = String.valueOf(reservation.getReservationType());
    }
}
