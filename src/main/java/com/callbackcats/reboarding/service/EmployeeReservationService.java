package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.*;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.repository.EmployeeReservationRepository;
import com.callbackcats.reboarding.repository.PersonalLayoutRepository;
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
public class EmployeeReservationService {

    private final ReservationRepository reservationRepository;
    private final EmployeeReservationRepository employeeReservationRepository;
    private final OfficeOptionsService officeOptionsService;
    private final EmployeeService employeeService;
    private final PersonalLayoutRepository personalLayoutRepository;
    private final KafkaMessageHandler kafkaMessageHandler;

    public EmployeeReservationService(ReservationRepository reservationRepository, EmployeeReservationRepository employeeReservationRepository, OfficeOptionsService officeOptionsService, EmployeeService employeeService, PersonalLayoutRepository personalLayoutRepository, KafkaMessageHandler kafkaMessageHandler) {
        this.reservationRepository = reservationRepository;
        this.employeeReservationRepository = employeeReservationRepository;
        this.officeOptionsService = officeOptionsService;
        this.employeeService = employeeService;
        this.personalLayoutRepository = personalLayoutRepository;
        this.kafkaMessageHandler = kafkaMessageHandler;
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
        employeeReservation.setWorkStation(null);
        PersonalLayout personalLayout = findPersonalLayoutByEmployeeIdAndDate(employeeId, today);
        personalLayoutRepository.delete(personalLayout);
        employeeReservationRepository.delete(employeeReservation);
        log.info("Employee by id:\t" + employeeId + "'s connection to reservation for the day:\t" + today + " was detached");
    }

    /**
     * <p>Attaches a reservation to an employee
     * </p>
     *
     * @param employee    the employee who requested a reservation
     * @param reservation the reservation to connect to employee
     * @return the created employee's reservations
     */
    EmployeeReservation saveReservationToEmployee(Employee employee, Reservation reservation) {
        EmployeeReservation employeeReservation;
        boolean permissionToOffice = reservation.getReservationType() != ReservationType.QUEUED;
        if (permissionToOffice) {
            WorkStation freeWorkstation = findUsableWorkstation();
            employeeReservation = new EmployeeReservation(employee, reservation, true, freeWorkstation);
        } else {
            employeeReservation = new EmployeeReservation(employee, reservation, false);
        }
        employeeReservationRepository.save(employeeReservation);
        log.info("Employee by id:\t" + employee.getId() + " was saved to reservation for the day:\t" + reservation.getDate());


        return employeeReservation;
    }

    /**
     * <p>Determines the current position in queue
     * </p>
     *
     * @param reservation the reservation to get the data from
     * @return a data containing the current position in queue and the type of reservation
     */
    public EmployeeReservationData getEmployeeReservationData(Reservation reservation) {
        Integer position = (int) findEmployeeReservationsByDate(reservation.getDate())
                .stream()
                .map(EmployeeReservation::getReserved)
                .filter(res -> res.getReservationType().equals(ReservationType.QUEUED))
                .count();
        return new EmployeeReservationData(reservation.getReservationType(), position);
    }

    /**
     * <p>Saves an image in byte array to the given employee's reservation
     * </p>
     *
     * @param employeeReservation the employee's reservations to modify
     * @param layout              the image in byte array to save to employee's reservation
     */
    public void saveEmployeeReservationLayout(EmployeeReservation employeeReservation, byte[] layout) {
        PersonalLayout personalLayout;
        if (employeeReservation.getPersonalLayout() != null) {
            Employee employee = employeeReservation.getEmployee();
            Reservation reservation = employeeReservation.getReserved();
            personalLayout = findPersonalLayoutByEmployeeIdAndDate(employee.getId(), reservation.getDate());
            personalLayoutRepository.delete(personalLayout);
        }
        personalLayout = new PersonalLayout(layout, employeeReservation);
        personalLayoutRepository.save(personalLayout);
        log.info("Personal layout saved to employee reservation id:\t" + employeeReservation.getId());
    }

    /**
     * <p>Finds and returns the path of the image of the employee's reservation
     * </p>
     *
     * @param employeeId      the employee's id
     * @param reservationDate the date of reservation
     * @return path of the image
     * @throws NoSuchElementException when there is no existing layout
     */
    public String getEmployeeReservationLayoutPath(String employeeId, LocalDate reservationDate) {
        PersonalLayout personalLayout = findPersonalLayoutByEmployeeIdAndDate(employeeId, reservationDate);

        log.info("Personal layout found for employee by id:\t" + employeeId + " on\t" + reservationDate);
        return personalLayout.getImagePath();
    }

    /**
     * <p>Finds and returns the image of the given image url in byte array
     * </p>
     *
     * @param path the path of the image
     * @return the found image in byte array
     */
    public byte[] getEmployeeReservationLayout(String path) {
        PersonalLayout personalLayout = personalLayoutRepository.findPersonalLayoutByPath(path).orElseThrow(() -> new NoSuchElementException("There is no such layout for path:\t" + path));
        log.info("Personal layout is found by path:\t" + path);
        return personalLayout.getPersonalLayout();
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
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(today);

        return employeesInOffice.equals(officeOptions.getLimit());
    }

    /**
     * <p>Updates the waiting employees' permission to enter to the office, based on the current number of people in the office
     * </p>
     */
    public void updateEmployeesCanEnterOffice() {
        LocalDate today = LocalDate.now();
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(today);
        int reservedCount = findReservationByDateAndType(today, ReservationType.RESERVED).getReservedEmployees().size();
        int freeSpace = officeOptions.getLimit() - reservedCount;
        if (freeSpace > 0) {
            Reservation reservation = findReservationByDateAndType(today, ReservationType.QUEUED);
            List<EmployeeReservation> reservedEmployees = reservation.getReservedEmployees();
            for (int i = 0; i < freeSpace && i < reservedEmployees.size(); i++) {
                reservedEmployees.get(i).setPermissionToOffice(true);
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
            OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(reservationDate);
            reservation = new Reservation(reservationDate, officeOptions, ReservationType.RESERVED);
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
     * <p>Deletes the connection of the employee and reservation from the database.
     * </p>
     *
     * @param employeeReservation the employee's reservation that will be deleted
     */
    public void removeEmployeeReservation(EmployeeReservation employeeReservation) {
        log.info("Employee reservations are removed with id:\t" + employeeReservation.getId());
        employeeReservationRepository.delete(employeeReservation);
    }

    /**
     * <p>Assigns a workstation to the employee who was waiting in queue
     * </p>
     *
     * @param employeeReservation the employee's reservation to modify
     * @return the modified employee's reservation
     */
    public EmployeeReservation setQueuedEmployeeWorkstation(EmployeeReservation employeeReservation) {
        EmployeeReservation changedEmployeeReservation = updateQueuedEmployeeWorkstation(employeeReservation);
        employeeReservationRepository.save(changedEmployeeReservation);
        employeeReservationRepository.delete(employeeReservation);
        log.info("Workstation attached to employee reservation id:\t" + employeeReservation.getId());

        return changedEmployeeReservation;
    }

    /**
     * <p>Sends a notification to an employee in queue when his/her position has reached the preconfigured place
     * </p>
     */
    public void notifyEmployeeStatus() {
        LocalDate today = LocalDate.now();
        List<EmployeeReservation> employeeReservations = employeeReservationRepository.findEmployeeReservationsByDateAndType(today, ReservationType.QUEUED);
        OfficeOptions officeOption = officeOptionsService.findOfficeOptionsByReservationDate(today);
        Integer employeeNumber = officeOption.getNotifiableEmployeeNumber();
        if (employeeNumber <= employeeReservations.size() - 1) {
            Employee notifyingEmployee = employeeReservations.get(employeeNumber-1).getEmployee();
            kafkaMessageHandler.sendNotification(notifyingEmployee.getId(), employeeNumber);
        }
    }

    private EmployeeReservation updateQueuedEmployeeWorkstation(EmployeeReservation employeeReservation) {
        Employee employee = employeeReservation.getEmployee();
        Reservation reservation = findReservationByDateAndType(LocalDate.now(), ReservationType.RESERVED);
        WorkStation usableWorkstation = findUsableWorkstation();
        EmployeeReservation newReservation = new EmployeeReservation(employee, reservation, true, usableWorkstation);

        log.info("Queued reservation is unset for id:\t" + reservation.getId());
        return newReservation;
    }

    private WorkStation findUsableWorkstation() {
        LocalDate today = LocalDate.now();
        OfficeOptions officeOption = officeOptionsService.findOfficeOptionsByReservationDate(today);
        List<EmployeeReservation> employeeReservations = findEmployeeReservationsByDate(today);
        List<WorkStation> reservedWorkstations = employeeReservations
                .stream()
                .map(EmployeeReservation::getWorkStation)
                .collect(Collectors.toList());
        List<WorkStation> officeWorkstations = officeOption.getOfficeWorkstations()
                .stream()
                .map(OfficeWorkstation::getWorkstation)
                .collect(Collectors.toList());
        WorkStation usableWorkstation = officeWorkstations
                .stream()
                .filter(officeWorkstation -> !reservedWorkstations.contains(officeWorkstation))
                .findFirst().orElseThrow(NoSuchElementException::new);
        log.info("Usable workstation is found");
        return usableWorkstation;
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

    private Boolean isReservationsWithinCapacityLimit(Reservation reservation) {
        OfficeOptions officeOptions = reservation.getOfficeOptions();
        Integer reservationCount = (int) findEmployeeReservationsByDate(reservation.getDate())
                .stream()
                .map(EmployeeReservation::getReserved).filter(res -> res.getReservationType().equals(ReservationType.RESERVED))
                .count();
        return officeOptions.getLimit() > reservationCount && officeOptions.getLimit() != 0;
    }

    private Reservation createQueuedReservation(LocalDate reservationDate) {
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(reservationDate);
        Reservation reservation = new Reservation(reservationDate, officeOptions, ReservationType.QUEUED);
        reservationRepository.save(reservation);
        log.info("A new queued reservation was created for the day:\t" + reservationDate);
        return reservation;
    }

    private PersonalLayout findPersonalLayoutByEmployeeIdAndDate(String employeeId, LocalDate reservationDate) {
        return personalLayoutRepository.findPersonalLayoutByEmployeeIdAndDate(employeeId, reservationDate)
                .orElseThrow(() -> new NoSuchElementException("Employee by id:\t" + employeeId + " has no reservation for the given day:\t" + reservationDate));
    }
}
