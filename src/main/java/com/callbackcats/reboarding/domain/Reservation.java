package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Column(name = "reservation_type")
    @Enumerated(EnumType.STRING)
    private ReservationType reservationType;

    @Column(name = "r_date")
    private LocalDate date;

    @OneToMany(mappedBy = "reserved")
    private List<EmployeeReservation> reservedEmployees = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "capacity_id")
    private Capacity capacity;

    public Reservation(LocalDate reservationDate, Capacity capacity, ReservationType reservationType) {
        this.date = reservationDate;
        this.capacity = capacity;
        this.reservationType = reservationType;
    }
}
