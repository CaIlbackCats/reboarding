package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.EmployeeReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeReservationRepository extends JpaRepository<EmployeeReservation, Long> {

    @Query("select er from EmployeeReservation er where er.employee.id= :employeeId and er.reserved.date= :date")
    Optional<EmployeeReservation> findEmployeeReservationByEmployeeIdAndReservationDate(@Param("employeeId") String employeeId, @Param("date") LocalDate date);

    @Query("select er from EmployeeReservation er where er.reserved.date= :date")
    List<EmployeeReservation> findEmployeeReservationsByDate(@Param("date") LocalDate date);

}
