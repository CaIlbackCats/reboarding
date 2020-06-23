package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.OfficeOptions;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.dto.PointData;
import com.callbackcats.reboarding.repository.WorkStationRepository;
import com.callbackcats.reboarding.util.TemplateMatcher;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WorkStationService {

    private final WorkStationRepository workStationRepository;
    private final OfficeOptionsService officeOptionsService;

    public WorkStationService(WorkStationRepository workStationRepository, OfficeOptionsService officeOptionsService) {
        this.workStationRepository = workStationRepository;
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

    public void createDailyLayout(List<PointData> closedWorkstations, LocalDate date) {
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(date);
        List<WorkStation> availableWorkstations = getAvailableWorkstations(closedWorkstations);


    }

    private List<WorkStation> getAvailableWorkstations(List<PointData> closedWorkstations) {
        List<WorkStation> workstations = workStationRepository.findAll();
        List<WorkStation> closedWorkstationList = closedWorkstations.stream().map(WorkStation::new).collect(Collectors.toList());
        return workstations
                .stream()
                .filter(closedWorkstationList::contains)
                .collect(Collectors.toList());
    }
}
