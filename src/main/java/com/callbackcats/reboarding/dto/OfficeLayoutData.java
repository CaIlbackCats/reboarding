package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.OfficeLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opencv.core.Point;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OfficeLayoutData {

    private LocalDate date;

    private List<WorkstationData> workstations;

    public OfficeLayoutData(OfficeLayout officeLayout) {
        this.date = officeLayout.getDate();
        if (officeLayout.getWorkStations() != null && !officeLayout.getWorkStations().isEmpty())
            this.workstations = officeLayout.getWorkStations()
                    .stream()
                    .map(WorkstationData::new)
                    .collect(Collectors.toList());
    }

    public OfficeLayoutData(List<Point> points, LocalDate date){
        this.date=date;
        this.workstations=points
                .stream()
                .map(WorkstationData::new)
                .collect(Collectors.toList());
    }
}
