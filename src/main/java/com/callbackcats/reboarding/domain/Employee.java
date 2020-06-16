package com.callbackcats.reboarding.domain;


import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "employee")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "id", unique = true)
    @NotNull
    private String id;

    @Column(name = "in_office")
    private Boolean inOffice;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeReservation> reservation;

    public Employee(String id, Boolean inOffice) {
        this.id = id;
        this.inOffice = inOffice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
