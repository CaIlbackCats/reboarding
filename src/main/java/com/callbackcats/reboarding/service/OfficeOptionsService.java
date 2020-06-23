package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.OfficeOptions;
import com.callbackcats.reboarding.dto.OfficeOptionsCreationData;
import com.callbackcats.reboarding.dto.CapacityData;
import com.callbackcats.reboarding.dto.PointData;
import com.callbackcats.reboarding.repository.OfficeOptionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OfficeOptionsService {

    private final OfficeOptionsRepository officeOptionsRepository;
    private final WorkStationService workStationService;

    public OfficeOptionsService(OfficeOptionsRepository officeOptionsRepository, WorkStationService workStationService) {
        this.officeOptionsRepository = officeOptionsRepository;
        this.workStationService = workStationService;
    }


    /**
     * <p>Saves capacities for the given time intervals.
     * </p>
     *
     * @param officeOptionsCreationData contains the maximum number of employees, the percentage of the allowed employees to the office, and the interval of the dates it connects to
     * @return the saved capacities
     */
    public List<CapacityData> saveCapacities(List<OfficeOptionsCreationData> officeOptionsCreationData) {
        List<OfficeOptions> capacities = officeOptionsCreationData
                .stream()
                .map(this::getOfficeOptions)
                .collect(Collectors.toList());
        officeOptionsRepository.saveAll(capacities);

        return capacities
                .stream()
                .map(CapacityData::new)
                .collect(Collectors.toList());
    }

    private OfficeOptions getOfficeOptions(OfficeOptionsCreationData officeOption) {
        OfficeOptions officeOptions = new OfficeOptions(officeOption);
        List<PointData> closedWorkstations = officeOption.getClosedWorkstations();
        Integer limit = officeOptions.getLimit();
        Integer minDistance = officeOptions.getMinDistance();
        officeOptions.setWorkStations(workStationService.generateLayoutWithRange(closedWorkstations, minDistance, limit));
        return officeOptions;
    }

    OfficeOptions findOfficeOptionsByReservationDate(LocalDate reservationDate) {
        OfficeOptions officeOptions = officeOptionsRepository.findOfficeOptionsByDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There is no capacity set for the given date:\t" + reservationDate));
        log.info("Capacity was found for the day:\t" + reservationDate);
        return officeOptions;
    }
}
