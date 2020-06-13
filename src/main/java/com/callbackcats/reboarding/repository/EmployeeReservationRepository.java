package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.EmployeeReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmployeeReservationRepository extends JpaRepository<EmployeeReservation, Long> {

    @Query("select er from EmployeeReservation er where er.employee.id= :employeeId and er.reserved.date= :date and er.reserved.reservationType= 'QUEUED'")
    Optional<EmployeeReservation> findQueuedEmployeeReservationByEmployeeIdAndDate(@Param("employeeId") String employeeId, @Param("date") LocalDate date);
}
