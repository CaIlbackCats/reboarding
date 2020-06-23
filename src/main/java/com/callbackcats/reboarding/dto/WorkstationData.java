package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.WorkStation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opencv.core.Point;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkstationData {

    private Double xPosition;

    private Double yPosition;

    public WorkstationData(WorkStation workStation) {
        this.xPosition = workStation.getXPosition();
        this.yPosition = workStation.getYPosition();
    }

    public WorkstationData(Point point) {
        this.xPosition = point.x;
        this.yPosition = point.y;
    }
}
