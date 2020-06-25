package com.callbackcats.reboarding.repository;

import com.callbackcats.reboarding.domain.PersonalLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PersonalLayoutRepository extends JpaRepository<PersonalLayout, Long> {

    @Query("select p from PersonalLayout p where p.employeeReservation.employee.id= :employeeId and p.employeeReservation.reserved.date= :date")
    Optional<PersonalLayout> findPersonalLayoutByEmployeeIdAndDate(@Param("employeeId") String employeeId, @Param("date") LocalDate date);

    @Query("select p from PersonalLayout p where p.imagePath= :path")
    Optional<PersonalLayout> findPersonalLayoutByPath(@Param("path") String path);
}
