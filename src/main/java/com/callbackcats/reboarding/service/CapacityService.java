package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.domain.Capacity;
import com.callbackcats.reboarding.dto.CapacityCreationData;
import com.callbackcats.reboarding.dto.CapacityData;
import com.callbackcats.reboarding.repository.CapacityRepository;
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
public class CapacityService {

    private final CapacityRepository capacityRepository;

    public CapacityService(CapacityRepository capacityRepository) {
        this.capacityRepository = capacityRepository;
    }

    public List<CapacityData> saveCapacities(List<CapacityCreationData> capacityCreationData) {
        List<Capacity> capacities = capacityCreationData.stream().map(Capacity::new).collect(Collectors.toList());
        capacityRepository.saveAll(capacities);

        return capacities
                .stream()
                .map(CapacityData::new)
                .collect(Collectors.toList());
    }

    Capacity findCapacityByReservationDate(LocalDate reservationDate) {
        Capacity capacity = capacityRepository.findCapacityByReservationDate(reservationDate).orElseThrow(() -> new NoSuchElementException("There is no capacity set for the given date:\t" + reservationDate));
        log.info("Capacity was found for the day:\t" + reservationDate);
        return capacity;
    }
}
