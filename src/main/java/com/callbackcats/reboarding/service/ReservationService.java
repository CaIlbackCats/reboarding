package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.domain.EmployeeReservation;
import com.callbackcats.reboarding.domain.Reservation;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import com.callbackcats.reboarding.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ReservationService {

    private ReservationRepository reservationRepository;
    private EmployeeRepository employeeRepository;

    public ReservationService(ReservationRepository reservationRepository, EmployeeRepository employeeRepository) {
        this.reservationRepository = reservationRepository;
        this.employeeRepository = employeeRepository;
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
        Optional<Employee> maybeEmployee = employeeRepository.findEmployeeById(currentEmployeeId);
        boolean isReservationDateToday = false;
        if (maybeEmployee.isPresent()) {
            Employee currentEmployee = maybeEmployee.get();
            if (currentEmployee.getReservation() != null && !currentEmployee.getReservation().isEmpty()) {
                isReservationDateToday =
                        currentEmployee.getReservation()
                                .stream()
                                .map(EmployeeReservation::getReservation)
                                .map(Reservation::getDate)
                                .anyMatch(localDate -> localDate.equals(LocalDate.now()));
            }
        }
        return isReservationDateToday;
    }

}
