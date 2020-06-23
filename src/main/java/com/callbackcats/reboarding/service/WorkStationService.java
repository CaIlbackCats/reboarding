package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.OfficeLayout;
import com.callbackcats.reboarding.domain.OfficeOptions;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.dto.OfficeLayoutData;
import com.callbackcats.reboarding.dto.PointData;
import com.callbackcats.reboarding.repository.OfficeLayoutRepository;
import com.callbackcats.reboarding.repository.WorkStationRepository;
import com.callbackcats.reboarding.util.InvalidLayoutException;
import com.callbackcats.reboarding.util.TemplateMatcher;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WorkStationService {

    private static final Point ORIGO = new Point(0, 0);

    private final WorkStationRepository workStationRepository;
    private final OfficeLayoutRepository officeLayoutRepository;
    private final OfficeOptionsService officeOptionsService;

    public WorkStationService(WorkStationRepository workStationRepository, OfficeLayoutRepository officeLayoutRepository, OfficeOptionsService officeOptionsService) {
        this.workStationRepository = workStationRepository;
        this.officeLayoutRepository = officeLayoutRepository;
        this.officeOptionsService = officeOptionsService;
    }

    @PostConstruct
    public void init() {
        List<Point> workstationPositions = TemplateMatcher.getWorkstationPosition();
        List<WorkStation> workStations = workstationPositions
                .stream()
                .map(WorkStation::new)
                .collect(Collectors.toList());
        workStationRepository.saveAll(workStations);
    }

    public OfficeLayoutData generateLayoutWithRange(List<PointData> disabledWorkstations, LocalDate date) {
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(date);
        List<WorkStation> availableWorkstations = getAvailableWorkstations(disabledWorkstations);
        int range = officeOptions.getMinDistance();
        List<WorkStation> layout = new ArrayList<>();
        Integer size = officeOptions.getLimit();

        while (layout.size() < size && !availableWorkstations.isEmpty()) {
            WorkStation closestWorkstation = findClosestWorkstation(availableWorkstations);
            layout.add(closestWorkstation);
            availableWorkstations = removeCoordsInRange(closestWorkstation, availableWorkstations, range);
        }
        if (availableWorkstations.isEmpty()) {
            throw new InvalidLayoutException("Invalid range and place combination");
        }
        OfficeLayout officeLayout = new OfficeLayout(date, layout);
        officeLayoutRepository.save(officeLayout);
        TemplateMatcher.drawMap(layout);
        return new OfficeLayoutData(officeLayout);
    }

    private List<WorkStation> getAvailableWorkstations(List<PointData> closedWorkstations) {
        List<WorkStation> workstations = workStationRepository.findAll();
        List<WorkStation> closedWorkstationList = closedWorkstations.stream().map(WorkStation::new).collect(Collectors.toList());
        return workstations
                .stream()
                .filter(workStation -> !closedWorkstationList.contains(workStation))
                .collect(Collectors.toList());
    }

    private List<WorkStation> removeCoordsInRange(WorkStation closestWorkstation, List<WorkStation> availableWorkstations, int range) {
        return availableWorkstations.stream().filter(workStation -> isWorkstationOutOfRange(closestWorkstation, workStation, range)).collect(Collectors.toList());
    }

    private boolean isWorkstationOutOfRange(WorkStation closestWorkstation, WorkStation currentWorkstation, int range) {
        Point closestPoint = new Point(closestWorkstation.getXPosition(), closestWorkstation.getYPosition());
        return calculateDistance(closestPoint, currentWorkstation) > range;
    }


    private WorkStation findClosestWorkstation(List<WorkStation> availableWorkstations) {

        List<Integer> workstationDistances = availableWorkstations
                .stream()
                .map(workStation -> calculateDistance(ORIGO, workStation))
                .collect(Collectors.toList());
        int minDistance = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < workstationDistances.size(); i++) {
            if (workstationDistances.get(i) != 0 && workstationDistances.get(i) < minDistance) {
                minDistance = workstationDistances.get(i);
                minIndex = i;
            }
        }
        return availableWorkstations.get(minIndex);
    }

    private int calculateDistance(Point target, WorkStation currentWorkstation) {
        double xDistance = currentWorkstation.getXPosition() - target.x;
        double yDistance = currentWorkstation.getYPosition() - target.y;
        return (int) Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
    }
}
