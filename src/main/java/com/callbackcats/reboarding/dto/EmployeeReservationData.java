package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.domain.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReservationData {

    private Long id;

    private List<ReservationData> reservations;

    private EmployeeData employeeData;


    public EmployeeReservationData(List<Reservation> reservations, Employee employee) {
        this.reservations = reservations.stream().map(ReservationData::new).collect(Collectors.toList());
        this.employeeData = new EmployeeData(employee);
    }
}
