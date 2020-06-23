package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.OfficeOptions;
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

    public CapacityData(OfficeOptions officeOptions) {
        this.id = officeOptions.getId();
        this.max = officeOptions.getMax();
        this.limit = officeOptions.getLimit();
        this.startDate = officeOptions.getStartDate();
        this.endDate = officeOptions.getEndDate();
    }
}
