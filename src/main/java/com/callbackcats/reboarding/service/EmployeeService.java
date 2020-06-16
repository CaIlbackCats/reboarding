package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.dto.EmployeeData;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import employee.EmployeeImporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private EmployeeImporter employeeImporter = new EmployeeImporter();


    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostConstruct
    public void init() {
        List<Employee> employees = employeeImporter.importEmployees();
        employeeRepository.saveAll(employees);
    }

    public EmployeeData findEmployeeDataById(String employeeId) {
        return new EmployeeData(findEmployeeById(employeeId));
    }

    /**
     * <p>Checks if the employee is currently in the office
     * </p>
     *
     * @param employeeId the id of the employee
     * @return true - if the employee is in the office
     * false - if the employee is not in the office
     */
    Boolean isEmployeeInOffice(String employeeId) {
        Employee employee = findEmployeeById(employeeId);
        return employee.getInOffice();
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