package com.callbackcats.reboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreationData {

    private String employeeId;

    private List<LocalDate> reservedDate;

    public ReservationCreationData(Map<String, String> dataTable) {
        this.employeeId = dataTable.get("id");
        this.reservedDate = new ArrayList<>();
        this.reservedDate.add(LocalDate.parse(dataTable.get("date")));
    }
}
