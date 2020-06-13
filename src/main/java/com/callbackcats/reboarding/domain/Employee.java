package com.callbackcats.reboarding.domain;


import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "employee")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "id")
    @NotNull
    private String id;

    @Column(name = "in_office")
    private Boolean inOffice;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeReservation> reservation;
}
