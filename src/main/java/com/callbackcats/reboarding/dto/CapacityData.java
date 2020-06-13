package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.Capacity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CapacityData {

    private Long id;

    private Integer max;

    private Integer limit;

    private LocalDate startDate;

    private LocalDate endDate;

    public CapacityData(Capacity capacity) {
        this.id = capacity.getId();
        this.max = capacity.getMax();
        this.limit = capacity.getLimit();
        this.startDate = capacity.getStartDate();
        this.endDate = capacity.getEndDate();
    }
}
