package com.callbackcats.reboarding.domain;

import com.callbackcats.reboarding.dto.PointData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.opencv.core.Point;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_station", uniqueConstraints = {@UniqueConstraint(columnNames = {"x_Position", "y_Position"})})
public class WorkStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "x_position")
    private Double xPosition;

    @Column(name = "y_position")
    private Double yPosition;

    @OneToMany(mappedBy = "workStation")
    private List<EmployeeReservation> employeeReservations;

    @OneToMany(mappedBy = "workstation")
    private List<OfficeWorkstation> officeWorkstations;

    public WorkStation(Point point) {
        this.xPosition = point.x;
        this.yPosition = point.y;

    }

    public WorkStation(PointData pointData) {
        this.xPosition = pointData.getXPosition();
        this.yPosition = pointData.getYPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkStation that = (WorkStation) o;
        return Objects.equals(xPosition, that.xPosition) &&
                Objects.equals(yPosition, that.yPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xPosition, yPosition);
    }
}
