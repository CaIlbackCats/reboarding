package com.callbackcats.reboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointData {

    private Double xPosition;

    private Double yPosition;

    public PointData(Map<String, String> dataTable) {
        this.xPosition = Double.valueOf(dataTable.get("x"));
        this.yPosition = Double.valueOf(dataTable.get("y"));
    }
}
