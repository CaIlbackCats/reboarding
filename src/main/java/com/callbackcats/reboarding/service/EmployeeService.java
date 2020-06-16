package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }



    Employee findEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findEmployeeById(employeeId).orElseThrow(() -> new NoSuchElementException("No employee found by given id:\t" + employeeId));
        log.info("Employee found by given id:\t" + employeeId);
        return employee;
    }

    void setEmployeeInOffice(Employee employee, boolean inOffice) {
        employee.setInOffice(inOffice);
        employeeRepository.save(employee);
        log.info("Employee by id:\t" + employee.getId() + " in office changed to:\t" + inOffice);
    }
}
