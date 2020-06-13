package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReservationData {


    private String reservationType;

    private Integer position;

    public EmployeeReservationData(ReservationType reservationType, Integer position) {
        this.reservationType = String.valueOf(reservationType);
        this.position = position;
    }
}
