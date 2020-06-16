package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.*;
import employee.EmployeeImporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReboardingService {

    private final EmployeeService employeeService;
    private final EmployeeReservationService employeeReservationService;

    public ReboardingService(EmployeeService employeeService, EmployeeReservationService employeeReservationService) {
        this.employeeReservationService = employeeReservationService;
        this.employeeService = employeeService;
    }


    /**
     * <p>Checks whether the given employee has reservation for the given date.
     * </p>
     *
     * @param employeeId the id of the employee
     * @param date       the date to look for employeereservations
     * @return true if there was  reservation,false if there wasn't reservation
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public boolean isEmployeeReservedGivenDay(String employeeId, LocalDate date) {
        Employee employee = employeeService.findEmployeeById(employeeId);

        if (employee.getReservation() == null || employee.getReservation().isEmpty()) {
            return false;
        }

        return employee.getReservation()
            .stream()
            .map(EmployeeReservation::getReserved)
            .map(Reservation::getDate)
            .anyMatch(localDate -> localDate.equals(date));
    }

    /**
     * <p>Checks the given employee in if there are enough free spaces in the office and has the permission to enter.
     * </p>
     *
     * @param employeeId the id of the employee
     * @return whether the employee could enter the office
     */
    public Boolean enterEmployee(String employeeId) {
        LocalDate today = LocalDate.now();
        EmployeeReservation employeeReservation = employeeReservationService.findEmployeeReservationByIdAndDate(employeeId, today);
        List<EmployeeReservation> employeeReservations = employeeReservationService.findEmployeeReservationsByDate(today);
        boolean canEmployeeEnter = !employeeReservationService.isOfficeAtLimitCurrently(employeeReservations)
                && employeeReservation.getPermisssionToOffice()
                && !employeeService.isEmployeeInOffice(employeeId);

        if (!canEmployeeEnter) {
            return false;
        }

        Employee employee = employeeReservation.getEmployee();
        employeeService.setEmployeeInOffice(employee, true);
        log.info("Employee by id:\t" + employeeId + " has entered the office");
        return true;
    }


    /**
     * <p>Saves an employee's reservation to the given day.
     * Creates reservation if there has been no for that day
     * </p>
     *
     * @param reservationCreationData contains id of the employee and the date of the reservation
     * @return whether the employee could enter the office or not
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public EmployeeReservationData handleReservationRequest(ReservationCreationData reservationCreationData) {
        Employee employee = employeeService.findEmployeeById(reservationCreationData.getEmployeeId());
        Reservation reservation = employeeReservationService.findOrCreateReservationByDate(reservationCreationData.getReservedDate());
        employeeReservationService.saveReservationToEmployee(employee, reservation);
        Integer position = reservation.getReservedEmployees().size() + 1;

        return new EmployeeReservationData(reservation.getReservationType(), position);
    }

    /**
     * <p>Checks the current position in the queue of the employee
     * </p>
     *
     * @param employeeId the id of the employee
     * @return the current position of the employee in the queue
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public Integer getStatus(String employeeId) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        LocalDate today = LocalDate.now();
        Reservation reservation = employeeReservationService.findReservationByDateAndType(today, ReservationType.QUEUED);
        List<Employee> employees = reservation.getReservedEmployees()
                .stream()
                .map(EmployeeReservation::getEmployee)
                .collect(Collectors.toList());
        try {
            return employees.indexOf(employee) + 1;
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Employee doesn't exist");
        }
    }

    /**
     * <p>Checks the employee out of the office, and sets the permissions of the waiting employees based on free spaces left
     * </p>
     *
     * @param employeeId the id of the employee
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public void handleEmployeeExit(String employeeId) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        employeeService.setEmployeeInOffice(employee, false);

        employeeReservationService.removeEmployeeReservationToday(employeeId);

        employeeReservationService.updateEmployeesCanEnterOffice();
    }


}
