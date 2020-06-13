package com.callbackcats.reboarding.dto;

import com.callbackcats.reboarding.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeData {

    private String id;

    private Boolean inOffice;

    public EmployeeData(Employee employee) {
        this.id = employee.getId();
        this.inOffice = employee.getInOffice();
    }
}
