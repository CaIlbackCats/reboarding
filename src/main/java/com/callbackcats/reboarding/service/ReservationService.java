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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReservationService {

    private ReservationRepository reservationRepository;
    private EmployeeRepository employeeRepository;
    private final CapacityRepository capacityRepository;
    private final EmployeeReservationRepository employeeReservationRepository;

    public ReservationService(ReservationRepository reservationRepository, EmployeeRepository employeeRepository, CapacityRepository capacityRepository, EmployeeReservationRepository employeeReservationRepository) {
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
    public boolean isEmployeeIdReservedToday(String currentEmployeeId) {
        Employee employee = findEmployeeById(currentEmployeeId);
        boolean isReservationDateToday = false;
        if (employee.getReservation() != null && !employee.getReservation().isEmpty()) {
            isReservationDateToday =
                    employee.getReservation()
                            .stream()
                            .map(EmployeeReservation::getReserved)
                            .map(Reservation::getDate)
                            .anyMatch(localDate -> localDate.equals(LocalDate.now()));
        }
        return isReservationDateToday;
    }

    public void enterEmployee(String employeeId) {
        Employee employee = findEmployeeById(employeeId);

    }

    public List<CapacityData> saveCapacities(List<CapacityCreationData> capacityCreationData) {
        List<Capacity> capacities = capacityCreationData.stream().map(Capacity::new).collect(Collectors.toList());
        capacityRepository.saveAll(capacities);

        return capacities.stream().map(CapacityData::new).collect(Collectors.toList());
    }

    public EmployeeReservationData handleReservationRequest(ReservationCreationData reservationCreationData) {
        Employee employee = findEmployeeById(reservationCreationData.getEmployeeId());
        Reservation reservation = findOrCreateReservationByDate(reservationCreationData.getReservedDate());
        saveReservationToEmployee(reservationCreationData.getReservedDate(), employee, reservation);
        Integer position = reservation.getReservedEmployees().size() + 1;
        return new EmployeeReservationData(reservation.getReservationType(), position);
    }

    private void saveReservationToEmployee(LocalDate reservedDate, Employee employee, Reservation reservation) {
        EmployeeReservation employeeReservation;
        if (isOfficeOverCrowded(reservation)) {
            reservation = createQueuedReservation(reservedDate);
            employeeReservation = new EmployeeReservation(employee, reservation, false);
        } else {
            employeeReservation = new EmployeeReservation(employee, reservation, true);
        }
        employeeReservationRepository.save(employeeReservation);
    }

    public Integer findPosition(String currentEmployeeId) {
        Employee employee = findEmployeeById(currentEmployeeId);
        LocalDate today = LocalDate.now();
        Reservation reservation = findReservationByDateAndType(today, ReservationType.QUEUED);
        List<Employee> employees = reservation.getReservedEmployees().stream().map(EmployeeReservation::getEmployee).collect(Collectors.toList());

        return employees.indexOf(employee) + 1;
    }

    public EmployeeData findEmployeeDataById(String employeeId) {
        return new EmployeeData(findEmployeeById(employeeId));
    }

    private Boolean isOfficeOverCrowded(Reservation reservation) {
        Capacity capacity = reservation.getCapacity();
        return capacity.getLimit() == reservation.getReservedEmployees().size();
    }

    private Reservation createQueuedReservation(LocalDate reservationDate) {
        Capacity capacity = findCapacityByReservationDate(reservationDate);
        Reservation reservation = new Reservation(reservationDate, capacity, ReservationType.QUEUED);
        reservationRepository.save(reservation);
        return reservation;
    }

    private Reservation findOrCreateReservationByDate(LocalDate reservationDate) {
        Reservation reservation;
        try {
            reservation = findReservationByDate(reservationDate);
        } catch (NoSuchElementException e) {
            Capacity capacity = findCapacityByReservationDate(reservationDate);
            reservation = new Reservation(reservationDate, capacity, ReservationType.RESERVED);
            reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Employee findEmployeeById(String employeeId) {
        return employeeRepository.findEmployeeById(employeeId).orElseThrow(() -> new NoSuchElementException("No employee found by given id:\t" + employeeId));
    }

    private Capacity findCapacityByReservationDate(LocalDate reservationDate) {
        return capacityRepository.findCapacityByReservationDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There is no capacity set for the given date:\t" + reservationDate));
    }

    private Reservation findReservationByDate(LocalDate reservationDate) {
        return reservationRepository.findReservationByDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There has been no reservation yet for the given day"));
    }

    private Reservation findReservationByDateAndType(LocalDate reservationDate, ReservationType reservationType) {
        return reservationRepository.findReservationByDateAndType(reservationDate, reservationType)
                .orElseThrow(() -> new NoSuchElementException("There are no " + reservationType + " reservations for the given day:\t" + reservationDate));
    }

}
