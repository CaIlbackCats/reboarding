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
     * <p>This is a simple description of the method. . .
     * <a href="http://www.supermanisthegreatest.com">Superman!</a>
     * </p>
     *
     * @param currentEmployeeId the amount of incoming damage
     * @return the amount of health hero has after attack
     * @see <a href="http://www.link_to_jira/HERO-402">HERO-402</a>
     * @since 1.0
     */
    public boolean isEmployeeReservedGivenDay(String currentEmployeeId, LocalDate date) {
        Employee employee = findEmployeeById(currentEmployeeId);
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

    public List<CapacityData> saveCapacities(List<CapacityCreationData> capacityCreationData) {
        List<Capacity> capacities = capacityCreationData.stream().map(Capacity::new).collect(Collectors.toList());
        capacityRepository.saveAll(capacities);

        return capacities
                .stream()
                .map(CapacityData::new)
                .collect(Collectors.toList());
    }

    public EmployeeReservationData handleReservationRequest(ReservationCreationData reservationCreationData) {
        Employee employee = findEmployeeById(reservationCreationData.getEmployeeId());
        Reservation reservation = findOrCreateReservationByDate(reservationCreationData.getReservedDate());
        saveReservationToEmployee(reservationCreationData.getReservedDate(), employee, reservation);
        Integer position = reservation.getReservedEmployees().size() + 1;
        return new EmployeeReservationData(reservation.getReservationType(), position);
    }

    public Integer getStatus(String currentEmployeeId) {
        Employee employee = findEmployeeById(currentEmployeeId);
        LocalDate today = LocalDate.now();
        Reservation reservation = findReservationByDateAndType(today, ReservationType.QUEUED);
        List<Employee> employees = reservation.getReservedEmployees()
                .stream()
                .map(EmployeeReservation::getEmployee)
                .collect(Collectors.toList());

        return employees.indexOf(employee) + 1;
    }

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
