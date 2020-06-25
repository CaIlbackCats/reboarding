package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "personal_layout")
public class PersonalLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "image_path")
    private String imagePath = UUID.randomUUID().toString();

    @Lob
    @Column(name = "personal_layout")
    private byte[] personalLayout;

    @OneToOne
    private EmployeeReservation employeeReservation;

    public PersonalLayout(byte[] layout, EmployeeReservation employeeReservation) {
        this.personalLayout = layout;
        this.employeeReservation = employeeReservation;
    }
}
