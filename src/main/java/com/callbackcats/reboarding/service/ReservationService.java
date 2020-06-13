package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.CapacityCreationData;
import com.callbackcats.reboarding.dto.CapacityData;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
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

    public List<CapacityData> saveCapacities(List<CapacityCreationData> capacityCreationData) {
        List<Capacity> capacities = capacityCreationData.stream().map(Capacity::new).collect(Collectors.toList());
        capacityRepository.saveAll(capacities);

        return capacities.stream().map(CapacityData::new).collect(Collectors.toList());
    }

    public EmployeeReservationData saveReservation(ReservationCreationData reservationCreationData) {
        Employee employee = findEmployeeById(reservationCreationData.getEmployeeId());
        List<Reservation> reservations = new ArrayList<>();
        reservationCreationData.getReservedDate()
                .stream()
                .map(this::findOrCreateReservationByDate)
                .forEach(reservation -> {
                    if (isOfficeOverCrowded(reservation)) {
                        reservation = createQueuedReservation(reservation.getDate());
                    }
                    employeeReservationRepository.save(new EmployeeReservation(employee, reservation));
                    reservations.add(reservation);
                });

        return new EmployeeReservationData(reservations, employee);
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

}
