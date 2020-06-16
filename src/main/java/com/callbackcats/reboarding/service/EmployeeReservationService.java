package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.repository.EmployeeReservationRepository;
import com.callbackcats.reboarding.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@Slf4j
public class EmployeeReservationService {

    private final ReservationRepository reservationRepository;
    private final EmployeeReservationRepository employeeReservationRepository;
    private final CapacityService capacityService;
    private final EmployeeService employeeService;

    public EmployeeReservationService(ReservationRepository reservationRepository, EmployeeReservationRepository employeeReservationRepository, CapacityService capacityService, EmployeeService employeeService) {
        this.reservationRepository = reservationRepository;
        this.employeeReservationRepository = employeeReservationRepository;
        this.capacityService = capacityService;
        this.employeeService = employeeService;
    }

    /**
     * <p>Removes the reservation of the given employee for today.
     * </p>
     *
     * @param employeeId the id of the employee
     */
    public void removeEmployeeReservationToday(String employeeId) {
        LocalDate today = LocalDate.now();
        EmployeeReservation employeeReservation = findEmployeeReservationByIdAndDate(employeeId, today);
        employeeReservationRepository.delete(employeeReservation);
        log.info("Employee by id:\t" + employeeId + "'s connection to reservation for the day:\t" + today + " was detached");
    }

    /**
     * <p>Attaches a reservation to an employee
     * </p>
     *
     * @param employee    the employee who requested a reservation
     * @param reservation the reservation to connect to employee
     */
    EmployeeReservationData saveReservationToEmployee(Employee employee, Reservation reservation) {
        EmployeeReservation employeeReservation;
        boolean permissionToOffice = reservation.getReservationType() != ReservationType.QUEUED;
        employeeReservation = new EmployeeReservation(employee, reservation, permissionToOffice);
        employeeReservationRepository.save(employeeReservation);
        log.info("Employee by id:\t" + employee.getId() + " was saved to reservation for the day:\t" + reservation.getDate());


        Integer position = (int) findEmployeeReservationsByDate(reservation.getDate())
                .stream()
                .map(EmployeeReservation::getReserved)
                .filter(res -> res.getReservationType().equals(ReservationType.QUEUED))
                .count();

        return new EmployeeReservationData(reservation.getReservationType(), position);
    }


    /**
     * <p>Decides whether the limit of office is reached for today
     * </p>
     *
     * @return true - if the number of employees in the office has reached the capacity limit for the day,
     * false- if the number of employee in the office has not reached yet the limit for the day
     */
    public Boolean isOfficeAtLimitCurrently() {
        LocalDate today = LocalDate.now();
        Integer employeesInOffice = employeeService.getNumberOfEmployeesInOffice();
        Capacity capacity = capacityService.findCapacityByReservationDate(today);

        return employeesInOffice.equals(capacity.getLimit());
    }

    /**
     * <p>Updates the waiting employees' permission to enter to the office, based on the current number of people in the office
     * </p>
     */
    public void updateEmployeesCanEnterOffice() {
        LocalDate today = LocalDate.now();
        List<EmployeeReservation> employeeReservations = findEmployeeReservationsByDate(today);
        Capacity capacity = capacityService.findCapacityByReservationDate(today);
        Integer employeesInOffice = employeeService.getNumberOfEmployeesInOffice();
        //  Integer employeesInOffice = getEmployeesInOffice(employeeReservations);
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

    /**
     * <p>Tries to find an already existing reservation for the given day, or creates a 'Reserved' one if there is none,
     * or a 'Queued' one if the capacity limit has already been reached
     * </p>
     *
     * @param reservationDate the date to reserve
     * @return a new or an already existing reservation where there's still free room
     */
    Reservation findOrCreateReservationByDate(LocalDate reservationDate) {
        Reservation reservation;
        try {
            reservation = findReservationByDateAndType(reservationDate, ReservationType.RESERVED);
        } catch (NoSuchElementException e) {
            Capacity capacity = capacityService.findCapacityByReservationDate(reservationDate);
            reservation = new Reservation(reservationDate, capacity, ReservationType.RESERVED);
            reservationRepository.save(reservation);
            log.info("A new reservation was created for the day:\t" + reservationDate);
        }
        if (!isReservationsWithinCapacityLimit(reservation)) {
            reservation = findOrCreateQueuedReservation(reservationDate);
        }
        return reservation;
    }

    /**
     * <p>Finds a reservation for the current day by the given reservation type
     * </p>
     *
     * @param reservationDate the date to reserve
     * @param reservationType the type of reservation to look for
     * @return the found Reservation
     * @throws NoSuchElementException if there has been no reservations for the given day by the given type
     */
    Reservation findReservationByDateAndType(LocalDate reservationDate, ReservationType reservationType) {
        Reservation reservation = reservationRepository.findReservationByDateAndType(reservationDate, reservationType)
                .orElseThrow(() -> new NoSuchElementException("There are no " + reservationType + " reservations for the given day:\t" + reservationDate));
        log.info("Reservation was found by given date:\t" + reservationDate + " and type:\t" + reservationType);
        return reservation;
    }

    /**
     * <p>Finds a the employee's reservation on the given day
     * </p>
     *
     * @param employeeId      the id of the employee
     * @param reservationDate the date of reservation
     * @return the found employee reservation
     * @throws NoSuchElementException if the employee didn't have a reservation for the given day
     */
    EmployeeReservation findEmployeeReservationByIdAndDate(String employeeId, LocalDate reservationDate) {
        EmployeeReservation employeeReservation = employeeReservationRepository.findEmployeeReservationByEmployeeIdAndReservationDate(employeeId, reservationDate)
                .orElseThrow(() -> new NoSuchElementException("Employee by id:\t" + employeeId + " has no reservation for the given day:\t" + reservationDate));
        log.info("Employee reservation was found for employee id:\t" + employeeId + " on day:\t" + reservationDate);
        return employeeReservation;
    }

    /**
     * <p>Finds all employee reservations for the given day
     * </p>
     *
     * @param reservationDate the date of reservation
     * @return the list of found employee reservations
     */
    List<EmployeeReservation> findEmployeeReservationsByDate(LocalDate reservationDate) {
        List<EmployeeReservation> employeeReservationsByDate = employeeReservationRepository.findEmployeeReservationsByDate(reservationDate);
        log.info("Employee reservations are found for the day:\t" + reservationDate);
        return employeeReservationsByDate;
    }

    /**
     *
     */
    //TODO javadoc
    public void removeEmployeeReservation(EmployeeReservation employeeReservation) {
        log.info("Employee reservations are removed with id:\t" + employeeReservation.getId());
        employeeReservationRepository.delete(employeeReservation);
    }

    private Reservation findOrCreateQueuedReservation(LocalDate reservationDate) {
        Reservation queuedReservation;
        try {
            queuedReservation = findReservationByDateAndType(reservationDate, ReservationType.QUEUED);
        } catch (NoSuchElementException e) {
            queuedReservation = createQueuedReservation(reservationDate);
            reservationRepository.save(queuedReservation);
        }
        return queuedReservation;
    }


    private Integer getEmployeesInOffice(List<EmployeeReservation> employeeReservations) {
        int employeesInOffice = (int) employeeReservations
                .stream()
                .map(EmployeeReservation::getEmployee)
                .filter(Employee::getInOffice)
                .count();
        log.info("Employees in office:\t" + employeesInOffice);
        return employeesInOffice;
    }

    private Boolean isReservationsWithinCapacityLimit(Reservation reservation) {
        Capacity capacity = reservation.getCapacity();
        Integer reservationCount = (int) findEmployeeReservationsByDate(reservation.getDate())
                .stream()
                .map(EmployeeReservation::getReserved).filter(res -> res.getReservationType().equals(ReservationType.RESERVED))
                .count();
        return capacity.getLimit() >= reservationCount && capacity.getLimit() != 0;
    }

    private Reservation createQueuedReservation(LocalDate reservationDate) {
        Capacity capacity = capacityService.findCapacityByReservationDate(reservationDate);
        Reservation reservation = new Reservation(reservationDate, capacity, ReservationType.QUEUED);
        reservationRepository.save(reservation);
        log.info("A new queued reservation was created for the day:\t" + reservationDate);
        return reservation;
    }
}
