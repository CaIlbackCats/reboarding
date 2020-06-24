package com.callbackcats.reboarding.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "office_workstation")
public class OfficeWorkstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "office_options_id")
    private OfficeOptions officeOptions;

    @ManyToOne
    @JoinColumn(name = "workstation_id")
    private WorkStation workstation;

    public OfficeWorkstation(OfficeOptions officeOptions, WorkStation workStation) {
        this.officeOptions = officeOptions;
        this.workstation = workStation;
    }
}
