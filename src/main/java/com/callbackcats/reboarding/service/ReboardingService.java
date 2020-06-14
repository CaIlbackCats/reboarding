package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.*;
import com.callbackcats.reboarding.repository.CapacityRepository;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import com.callbackcats.reboarding.repository.EmployeeReservationRepository;
import com.callbackcats.reboarding.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReboardingService {

    private ReservationRepository reservationRepository;
    private EmployeeRepository employeeRepository;
    private final CapacityRepository capacityRepository;
    private final EmployeeReservationRepository employeeReservationRepository;

    public ReboardingService(ReservationRepository reservationRepository, EmployeeRepository employeeRepository, CapacityRepository capacityRepository, EmployeeReservationRepository employeeReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.employeeRepository = employeeRepository;
        this.capacityRepository = capacityRepository;
        this.employeeReservationRepository = employeeReservationRepository;
    }

    /**
     * <p>Checks whether the given employee has reservation for the given date.
     * </p>
     *
     * @param employeeId the id of the employee
     * @param date the date to look for employeereservations
     * @return true if there was  reservation,false if there wasn't reservation
     * @throws NoSuchElementException if the employee doesn't exist
     */
    public boolean isEmployeeReservedGivenDay(String employeeId, LocalDate date) {
        Employee employee = findEmployeeById(employeeId);
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
     * </p>
     *
     * @param employeeId the id of the employee
     * @return whether the employee could enter the office
     */
    public Boolean enterEmployee(String employeeId) {
        boolean employeeEntered = false;
        LocalDate today = LocalDate.now();
        EmployeeReservation employeeReservation = findEmployeeReservationByIdAndDate(employeeId, today);
        List<EmployeeReservation> employeeReservations = findEmployeeReservationsByDate(today);
        Capacity capacity = findCapacityByReservationDate(today);
        boolean isOfficeAtLimit = getEmployeesInOffice(employeeReservations).equals(capacity.getLimit());
        if (!isOfficeAtLimit && employeeReservation.getPermisssionToOffice()) {
            Employee employee = employeeReservation.getEmployee();
            setEmployeeInOffice(employee, true);
            employeeEntered = true;
            log.info("Employee by id:\t" + employeeId + " has entered the office");
        }
        return employeeEntered;
    }

    /**
     * <p>Saves capacities for the given time intervals.
     * </p>
     *
     * @param capacityCreationData contains the maximum number of employees, the percentage of the allowed employees to the office, and the interval of the dates it connects to
     * @return the saved capacities
     */
    public List<CapacityData> saveCapacities(List<CapacityCreationData> capacityCreationData) {
        List<Capacity> capacities = capacityCreationData.stream().map(Capacity::new).collect(Collectors.toList());
        capacityRepository.saveAll(capacities);

        return capacities
                .stream()
                .map(CapacityData::new)
                .collect(Collectors.toList());
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
        Employee employee = findEmployeeById(reservationCreationData.getEmployeeId());
        Reservation reservation = findOrCreateReservationByDate(reservationCreationData.getReservedDate());
        saveReservationToEmployee(reservationCreationData.getReservedDate(), employee, reservation);
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
        Employee employee = findEmployeeById(employeeId);
        LocalDate today = LocalDate.now();
        Reservation reservation = findReservationByDateAndType(today, ReservationType.QUEUED);
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
    public void handleEmployeeExit(String employeeId) {
        Employee employee = findEmployeeById(employeeId);
        setEmployeeInOffice(employee, false);

        LocalDate today = LocalDate.now();
        EmployeeReservation employeeReservation = findEmployeeReservationByIdAndDate(employeeId, today);
        employeeReservationRepository.delete(employeeReservation);
        log.info("Employee by id:\t" + employeeId + "'s connection to reservation for the day:\t" + today + " was detached");

        updateEmployeesCanEnterOffice();
    }

    private void updateEmployeesCanEnterOffice() {
        LocalDate today = LocalDate.now();
        List<EmployeeReservation> employeeReservations = findEmployeeReservationsByDate(today);
        Capacity capacity = findCapacityByReservationDate(today);
        Integer employeesInOffice = getEmployeesInOffice(employeeReservations);
        int freeSpace = capacity.getLimit() - employeesInOffice;
        if (freeSpace > 0) {
            Reservation reservation = findReservationByDateAndType(today, ReservationType.QUEUED);
            List<EmployeeReservation> reservedEmployees = reservation.getReservedEmployees();
            for (int i = 0; i < freeSpace && i < reservedEmployees.size(); i++) {
                reservedEmployees.get(i).setPermisssionToOffice(true);
            }
            employeeReservationRepository.saveAll(reservedEmployees);
            log.info("Employees permission to enter was updated based on free spots");
        }

    }

    private void setEmployeeInOffice(Employee employee, boolean inOffice) {
        employee.setInOffice(inOffice);
        employeeRepository.save(employee);
        log.info("Employee by id:\t" + employee.getId() + " in office changed to:\t" + inOffice);
    }

    public EmployeeData findEmployeeDataById(String employeeId) {
        return new EmployeeData(findEmployeeById(employeeId));
    }

    private void saveReservationToEmployee(LocalDate reservedDate, Employee employee, Reservation reservation) {
        EmployeeReservation employeeReservation;
        if (isReservationsAtCapacityLimit(reservation)) {
            reservation = createQueuedReservation(reservedDate);
            employeeReservation = new EmployeeReservation(employee, reservation, false);
        } else {
            employeeReservation = new EmployeeReservation(employee, reservation, true);
        }
        employeeReservationRepository.save(employeeReservation);
        log.info("Employee by id:\t" + employee.getId() + " was saved to reservation for the day:\t" + reservation.getDate());
    }

    private Integer getEmployeesInOffice(List<EmployeeReservation> employeeReservations) {
        log.info("Count current employees in office requested");
        return (int) employeeReservations
                .stream()
                .map(EmployeeReservation::getEmployee)
                .filter(Employee::getInOffice)
                .count();
    }

    private Boolean isReservationsAtCapacityLimit(Reservation reservation) {
        Capacity capacity = reservation.getCapacity();
        return capacity.getLimit() == reservation.getReservedEmployees().size();
    }

    private Reservation createQueuedReservation(LocalDate reservationDate) {
        Capacity capacity = findCapacityByReservationDate(reservationDate);
        Reservation reservation = new Reservation(reservationDate, capacity, ReservationType.QUEUED);
        reservationRepository.save(reservation);
        log.info("A new queued reservation was created for the day:\t" + reservationDate);
        return reservation;
    }

    private Reservation findOrCreateReservationByDate(LocalDate reservationDate) {
        Reservation reservation;
        try {
            reservation = findReservationByDateAndType(reservationDate, ReservationType.RESERVED);
        } catch (NoSuchElementException e) {
            Capacity capacity = findCapacityByReservationDate(reservationDate);
            reservation = new Reservation(reservationDate, capacity, ReservationType.RESERVED);
            reservationRepository.save(reservation);
            log.info("A new reservation was created for the day:\t" + reservationDate);
        }
        return reservation;
    }

    private Employee findEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findEmployeeById(employeeId).orElseThrow(() -> new NoSuchElementException("No employee found by given id:\t" + employeeId));
        log.info("Employee found by given id:\t" + employeeId);
        return employee;
    }

    private Capacity findCapacityByReservationDate(LocalDate reservationDate) {
        Capacity capacity = capacityRepository.findCapacityByReservationDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There is no capacity set for the given date:\t" + reservationDate));
        log.info("Capacity was found for the day:\t" + reservationDate);
        return capacity;
    }

    private Reservation findReservationByDateAndType(LocalDate reservationDate, ReservationType reservationType) {
        Reservation reservation = reservationRepository.findReservationByDateAndType(reservationDate, reservationType)
                .orElseThrow(() -> new NoSuchElementException("There are no " + reservationType + " reservations for the given day:\t" + reservationDate));
        log.info("Reservation was found by given date:\t" + reservationDate + " and type:\t" + reservationType);
        return reservation;
    }

    private EmployeeReservation findEmployeeReservationByIdAndDate(String employeeId, LocalDate date) {
        EmployeeReservation employeeReservation = employeeReservationRepository.findEmployeeReservationByEmployeeIdAndReservationDate(employeeId, date)
                .orElseThrow(() -> new NoSuchElementException("Employee by id: " + employeeId + " has no reservation for the given day: " + date));
        log.info("Employee reservation was found for employee id:\t" + employeeId + " on day:\t" + date);
        return employeeReservation;
    }

    private List<EmployeeReservation> findEmployeeReservationsByDate(LocalDate date) {
        List<EmployeeReservation> employeeReservationsByDate = employeeReservationRepository.findEmployeeReservationsByDate(date);
        log.info("Employee reservations are found for the day:\t" + date);
        return employeeReservationsByDate;
    }

}
