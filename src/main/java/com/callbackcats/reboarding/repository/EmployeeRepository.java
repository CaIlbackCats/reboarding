package com.callbackcats.reboarding.repository;


import com.callbackcats.reboarding.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("SELECT e FROM Employee e WHERE e.id= :id")
    Optional<Employee> findEmployeeById(@Param("id") String employeeId);
}
