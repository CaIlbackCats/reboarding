package com.callbackcats.reboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreationData {

    private String employeeId;

    private LocalDate reservedDate;

    public ReservationCreationData(Map<String, String> dataTable) {
        this.employeeId = dataTable.get("id");
        this.reservedDate = LocalDate.parse(dataTable.get("date"));
    }
}
