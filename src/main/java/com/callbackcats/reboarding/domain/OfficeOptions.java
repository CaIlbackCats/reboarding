package com.callbackcats.reboarding.domain;

import com.callbackcats.reboarding.dto.CapacityCreationData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "office_capacity")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class OfficeOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "max")
    private Integer max;

    @Column(name = "capacity_limit")
    private Integer limit;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "officeOptions")
    private List<Reservation> reservation;

    @Column(name = "employee_min_distance")
    private Integer minDistance;

    public OfficeOptions(CapacityCreationData capacityCreationData) {
        this.max = capacityCreationData.getMax();
        this.limit = (max * capacityCreationData.getCapacityValue()) / 100;
        this.startDate = capacityCreationData.getStartDate();
        this.endDate = capacityCreationData.getEndDate();
    }
}
