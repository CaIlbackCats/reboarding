package com.callbackcats.reboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opencv.core.Point;

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

    public PointData(Point point) {
        this.xPosition = point.x;
        this.yPosition = point.y;
    }
}
