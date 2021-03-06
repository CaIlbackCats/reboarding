package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
import com.callbackcats.reboarding.util.LayoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReboardingService {

    private final EmployeeService employeeService;
    private final EmployeeReservationService employeeReservationService;
    private final OfficeOptionsService officeOptionsService;
    private final LayoutHandler layoutHandler;

    public ReboardingService(EmployeeService employeeService, EmployeeReservationService employeeReservationService, OfficeOptionsService officeOptionsService, LayoutHandler layoutHandler) {
        this.employeeReservationService = employeeReservationService;
        this.employeeService = employeeService;
        this.officeOptionsService = officeOptionsService;
        this.layoutHandler = layoutHandler;
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
        boolean isReserved = false;
        if (employee.getReservation() != null && !employee.getReservation().isEmpty()) {
            isReserved =
                    employee.getReservation()
                            .stream()
                            .map(EmployeeReservation::getReserved)
                            .map(Reservation::getDate)
                            .anyMatch(localDate -> localDate.equals(date));
        }
        return isReserved;
    }

    /**
     * <p>Checks the given employee in if there are enough free spaces in the office and has the permission to enter.
     * employees with vip status are let in under every circumstances
     * </p>
     *
     * @param employeeId the id of the employee
     * @return whether the employee could enter the office
     * @throws NoSuchElementException if the employee doesn't have reservation for the day
     */
    public Boolean enterEmployee(String employeeId) {
        boolean employeeEntered;
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (employee.getVip()) {
            employeeEntered = true;
            employeeService.setEmployeeInOffice(employee, true);
        } else {
            employeeEntered = enterNonVipEmployee(employee);
        }


        return employeeEntered;
    }

    /**
     * <p>Saves an employee's reservation to the given day, with the image of the reserved workstation
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
        EmployeeReservation employeeReservation = employeeReservationService.saveReservationToEmployee(employee, reservation);
        if (reservation.getReservationType().equals(ReservationType.RESERVED)) {
            byte[] personalLayout = layoutHandler.createPersonalLayout(employeeReservation.getWorkStation(), employee.getInOffice());
            employeeReservationService.saveEmployeeReservationLayout(employeeReservation, personalLayout);
        }
        return employeeReservationService.getEmployeeReservationData(employeeReservation.getReserved());
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

        return employees.indexOf(employee) + 1;
    }

    /**
     * <p>Checks the employee out of the office, and sets the permissions of the waiting employees based on free spaces left
     * </p>
     *
     * @param employeeId the id of the employee
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public Boolean handleEmployeeExit(String employeeId) {
        boolean leftEmployee = false;
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (employee.getVip() && employee.getInOffice()) {
            employeeService.setEmployeeInOffice(employee, false);
            leftEmployee = true;
        } else if (employee.getInOffice()) {
            employeeReservationService.removeEmployeeReservationToday(employeeId);
            employeeService.setEmployeeInOffice(employee, false);

            employeeReservationService.updateEmployeesCanEnterOffice();
            leftEmployee = true;
        }
        return leftEmployee;
    }

    /**
     * <p>Finds and Removes the given reservation of the employee
     * </p>
     *
     * @param employeeId   the id of the employee
     * @param reservedDate reservation date
     */
    public void removeReservation(String employeeId, LocalDate reservedDate) {
        EmployeeReservation employeeReservation = employeeReservationService.findEmployeeReservationByIdAndDate(employeeId, reservedDate);
        employeeReservationService.removeEmployeeReservation(employeeReservation);
    }

    /**
     * <p>Creates today's office layout image in byte array based on the employees' reservations
     * </p>
     *
     * @return byte array representing the current status of the office
     * <br>
     * for better visibility the office is colored in gray
     * <br>
     * red - workstation is currently occupied
     * <br>
     * yellow - workstation has a reservation
     * <br>
     * green - workstation is free to reserve
     */
    public byte[] getCurrentOfficeLayout() {
        LocalDate today = LocalDate.now();
        List<EmployeeReservation> employeeReservations = employeeReservationService.findEmployeeReservationsByDate(today);
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(today);
        List<OfficeWorkstation> dailyLayout = officeOptions.getOfficeWorkstations();
        return layoutHandler.createCurrentLayout(employeeReservations, dailyLayout);
    }

    private boolean enterNonVipEmployee(Employee employee) {
        LocalDate today = LocalDate.now();

        boolean employeeEntered = false;
        EmployeeReservation employeeReservation = employeeReservationService.findEmployeeReservationByIdAndDate(employee.getId(), today);
        boolean canEmployeeEnter = employee.getVip() || (!employeeReservationService.isOfficeAtLimitCurrently()
                && employeeReservation.getPermissionToOffice()
                && !employee.getInOffice()
                && isEmployeeReservedGivenDay(employee, today));
        if (canEmployeeEnter) {
            Reservation reservation = employeeReservation.getReserved();
            employeeService.setEmployeeInOffice(employee, true);
            if (reservation.getReservationType().equals(ReservationType.QUEUED)) {
                employeeReservation = employeeReservationService.setQueuedEmployeeWorkstation(employeeReservation);
                employeeReservationService.notifyEmployeeStatus();

            }
            byte[] personalLayout = layoutHandler.createPersonalLayout(employeeReservation.getWorkStation(), employee.getInOffice());
            employeeReservationService.saveEmployeeReservationLayout(employeeReservation, personalLayout);

            employeeEntered = true;
            //   employeeReservationService.removeEmployeeReservationToday(employeeId);
            log.info("Employee by id:\t" + employee.getId() + " has entered the office");
        }
        return employeeEntered;
    }

    private boolean isEmployeeReservedGivenDay(Employee employee, LocalDate date) {
        boolean isReserved = false;
        if (employee.getReservation() != null && !employee.getReservation().isEmpty()) {
            isReserved =
                    employee.getReservation()
                            .stream()
                            .map(EmployeeReservation::getReserved)
                            .map(Reservation::getDate)
                            .anyMatch(localDate -> localDate.equals(date));
        }
        return isReserved;
    }
}
