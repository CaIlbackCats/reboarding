package com.callbackcats.reboarding.domain;


import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
