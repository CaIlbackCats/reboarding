package com.callbackcats.reboarding.service;

import com.callbackcats.reboarding.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class EntryService {

    private ReservationRepository reservationRepository;

    public EntryService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean checkUserId(String currentUserId){
        return reservationRepository.findByUserId(currentUserId) != null;
    }
}
