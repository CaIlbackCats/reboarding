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

    public OfficeOptionsService(OfficeOptionsRepository officeOptionsRepository) {
        this.officeOptionsRepository = officeOptionsRepository;
    }


    /**
     * <p>Saves capacities for the given time intervals.
     * </p>
     *
     * @param officeOptionsCreationData contains the maximum number of employees, the percentage of the allowed employees to the office, and the interval of the dates it connects to
     * @return the saved capacities
     */
    public OfficeOptions saveOfficeOption(OfficeOptionsCreationData officeOptionsCreationData) {

        OfficeOptions officeOptions = new OfficeOptions(officeOptionsCreationData);
        officeOptionsRepository.save(officeOptions);

        log.info("Office options saved");
        return officeOptions;
    }

    public List<CapacityData> saveOfficeOptions(List<OfficeOptionsCreationData> officeOptionsCreationData) {
        List<OfficeOptions> officeOptions = officeOptionsCreationData.stream().map(OfficeOptions::new).collect(Collectors.toList());

        officeOptionsRepository.saveAll(officeOptions);

        return officeOptions
                .stream()
                .map(CapacityData::new)
                .collect(Collectors.toList());
    }

    OfficeOptions findOfficeOptionsByReservationDate(LocalDate reservationDate) {
        OfficeOptions officeOptions = officeOptionsRepository.findOfficeOptionsByDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There is no capacity set for the given date:\t" + reservationDate));
        log.info("Capacity was found for the day:\t" + reservationDate);
        return officeOptions;
    }
}
