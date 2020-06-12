package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Column(name = "date")
    private LocalDateTime date;

    @OneToMany(mappedBy = "reservation")
    private List<Employee> reservedEmployees;

    @OneToMany(mappedBy = "reservation")
    private List<Employee> queuedEmployees;

    @OneToOne
    @JoinColumn(name = "capacity_id")
    private Capacity capacity;
}
