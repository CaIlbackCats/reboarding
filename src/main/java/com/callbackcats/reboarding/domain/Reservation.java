package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "reservation")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "r_date")
    private LocalDate date;

    @OneToMany(mappedBy = "reservation")
    private List<EmployeeReservation> reservedEmployees;

    @OneToMany(mappedBy = "reservation")
    private List<EmployeeReservation> queuedEmployees;

    @OneToOne
    @JoinColumn(name = "capacity_id")
    private Capacity capacity;
}