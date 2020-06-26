package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.OfficeOptions;
import com.callbackcats.reboarding.domain.OfficeWorkstation;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.dto.OfficeOptionsCreationData;
import com.callbackcats.reboarding.dto.PointData;
import com.callbackcats.reboarding.repository.OfficeWorkstationRepository;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OfficeWorkstationService {

    private final OfficeOptionsService officeOptionsService;
    private final WorkStationService workStationService;
    private final OfficeWorkstationRepository officeWorkstationRepository;

    public OfficeWorkstationService(OfficeOptionsService officeOptionsService, WorkStationService workStationService, OfficeWorkstationRepository officeWorkstationRepository) {
        this.officeOptionsService = officeOptionsService;
        this.workStationService = workStationService;
        this.officeWorkstationRepository = officeWorkstationRepository;
    }

    public void saveOfficeWorkstations(List<OfficeOptionsCreationData> officeOptionsCreationDataList) {
        List<OfficeWorkstation> officeWorkstations = officeOptionsCreationDataList
                .stream()
                .map(this::createOfficeWorkstations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        officeWorkstationRepository.saveAll(officeWorkstations);
    }

    private List<OfficeWorkstation> createOfficeWorkstations(OfficeOptionsCreationData officeOptionsCreationData) {
        OfficeOptions officeOptions = officeOptionsService.saveOfficeOption(officeOptionsCreationData);
        List<WorkStation> workStations = workStationService.generateLayoutWithRange(officeOptionsCreationData.getClosedWorkstations(), officeOptions.getMinDistance(), officeOptions.getLimit());
        List<OfficeWorkstation> officeWorkstations = workStations
                .stream()
                .map(workStation -> new OfficeWorkstation(officeOptions, workStation))
                .collect(Collectors.toList());
        officeWorkstationRepository.saveAll(officeWorkstations);
        return officeWorkstations;
    }

    public void saveOfficeWorkstation(List<Point> closedPoints, LocalDate date) {
        List<PointData> closedPointData = closedPoints.stream().map(PointData::new).collect(Collectors.toList());
        OfficeOptions officeOptions = officeOptionsService.findOfficeOptionsByReservationDate(date);
        List<WorkStation> workStations = workStationService.generateLayoutWithRange(closedPointData, officeOptions.getMinDistance(), officeOptions.getLimit());
        List<OfficeWorkstation> officeWorkstations = workStations
                .stream()
                .map(workStation -> new OfficeWorkstation(officeOptions, workStation))
                .collect(Collectors.toList());
        officeWorkstationRepository.saveAll(officeWorkstations);
    }


}
