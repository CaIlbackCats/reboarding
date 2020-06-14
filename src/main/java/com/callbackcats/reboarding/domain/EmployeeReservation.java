package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "employee_reservation")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class EmployeeReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reserved;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "permission_to_office")
    private Boolean permisssionToOffice;

    public EmployeeReservation(Employee employee, Reservation reserved, Boolean permisssionToOffice) {
        this.employee = employee;
        this.reserved = reserved;
        this.permisssionToOffice = permisssionToOffice;
    }
}
