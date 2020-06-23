package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.OfficeOptions;
import com.callbackcats.reboarding.domain.OfficeLayout;
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

//    public OfficeLayoutData createDailyLayout(List<PointData> closedWorkstations, LocalDate date) {
//        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(date);
//        List<WorkStation> availableWorkstations = getAvailableWorkstations(closedWorkstations);
//        List<WorkStation> dailyWorkstations = new ArrayList<>();
//        int i = 0;
//        while (dailyWorkstations.size() < officeOptions.getLimit() && i < availableWorkstations.size()) {
//
//            if (dailyWorkstations.isEmpty()) {
//                dailyWorkstations.add(availableWorkstations.get(i));
//                availableWorkstations.remove(availableWorkstations.get(i));
//            } else {
//                WorkStation workstationToAdd = getRangeCheckedWorkstation(availableWorkstations, availableWorkstations.get(i), officeOptions.getMinDistance());
//                dailyWorkstations.add(workstationToAdd);
//                availableWorkstations.remove(workstationToAdd);
//            }
//            i++;
//        }
//        OfficeLayout officeLayout = new OfficeLayout(date, dailyWorkstations);
//        officeLayoutRepository.save(officeLayout);
//        officeLayout.getWorkStations().forEach(workStation -> log.info(workStation.getXPosition() + "\t" + workStation.getYPosition()));
//        TemplateMatcher.drawMap(officeLayout.getWorkStations());
//        return new OfficeLayoutData(officeLayout);
//    }
//
//    private WorkStation getRangeCheckedWorkstation(List<WorkStation> availableWorkstation, WorkStation currentWorkstation, Integer distanceLimit) {
//        int i = 0;
//        while (i < availableWorkstation.size() && distance(currentWorkstation, availableWorkstation.get(i)) <= distanceLimit) {
//            i++;
//        }
//        return availableWorkstation.get(i);
//    }
//
//    private Double distance(WorkStation currentWorkstation, WorkStation comparingWorkstation) {
//        double xDistance = comparingWorkstation.getXPosition() - currentWorkstation.getXPosition();
//        double yDistance = comparingWorkstation.getYPosition() - currentWorkstation.getYPosition();
//        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
//    }
//
//    private List<WorkStation> getAvailableWorkstations(List<PointData> closedWorkstations) {
//        List<WorkStation> workstations = workStationRepository.findAll();
//        List<WorkStation> closedWorkstationList = closedWorkstations.stream().map(WorkStation::new).collect(Collectors.toList());
//        return workstations
//                .stream()
//                .filter(workStation -> !closedWorkstationList.contains(workStation))
//                .collect(Collectors.toList());
//    }


    public OfficeLayoutData generateLayoutWithRange(List<PointData> disabledWorkstations, LocalDate date) {
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(date);
        List<WorkStation> availableWorkstations = getAvailableWorkstations(disabledWorkstations);
        List<Point> coords = availableWorkstations.stream().map(workStation -> new Point(workStation.getXPosition(), workStation.getYPosition())).collect(Collectors.toList());
        int range = officeOptions.getMinDistance();
        List<Point> layout = new ArrayList<>();
        Integer size = officeOptions.getLimit();

        Point target = new Point(0, 0);
        while (layout.size() < size && !coords.isEmpty()) {
            Point closestCoord = getClosestCoord(target, coords);
            layout.add(closestCoord);
            coords = removeCoordsInRange(closestCoord, coords, range);
            target = layout.get(layout.size() - 1);
        }
        if (coords.isEmpty()) {
            throw new InvalidLayoutException("Invalid range and place combination");
        }
        TemplateMatcher.drawMap(layout);
        return new OfficeLayoutData(layout, date);
    }

    private List<WorkStation> getAvailableWorkstations(List<PointData> closedWorkstations) {
        List<WorkStation> workstations = workStationRepository.findAll();
        List<WorkStation> closedWorkstationList = closedWorkstations.stream().map(WorkStation::new).collect(Collectors.toList());
        return workstations
                .stream()
                .filter(workStation -> !closedWorkstationList.contains(workStation))
                .collect(Collectors.toList());
    }

    private List<Point> removeCoordsInRange(Point target, List<Point> coords, int range) {
        return coords.stream().filter(coord -> isOutOfTargetRange(target, coord, range)).collect(Collectors.toList());
    }

    private boolean isOutOfTargetRange(Point target, Point coord, int range) {
        return calculateDistance(target, coord) > range;
    }


    private Point getClosestCoord(Point target, List<Point> coords) {
        List<Integer> ranges = coords.stream()
                .map(coord -> calculateDistance(target, coord))
                .collect(Collectors.toList());
        int min = 1000;
        int minIndex = 0;
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i) != 0 && ranges.get(i) < min) {
                min = ranges.get(i);
                minIndex = i;
            }
        }
        return coords.get(minIndex);
    }

    private int calculateDistance(Point target, Point coord) {
        return (int) Math.sqrt(Math.pow(coord.x - target.x, 2) + Math.pow(coord.y - target.y, 2));
    }
}
