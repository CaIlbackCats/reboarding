package com.callbackcats.reboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OfficeOptionsCreationData {

    private Integer max;

    private Integer capacityValue;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<PointData> closedWorkstations;

    public OfficeOptionsCreationData(Map<String, String> dataTable) {
        this.max = Integer.parseInt(dataTable.get("max").trim());
        this.capacityValue = Integer.parseInt(dataTable.get("capacityValue").trim());
        this.startDate = LocalDate.parse(dataTable.get("startDate"));
        this.endDate = LocalDate.parse(dataTable.get("endDate"));
    }
}
