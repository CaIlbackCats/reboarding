package com.callbackcats.reboarding.controller;

import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
import com.callbackcats.reboarding.service.ReboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api")
public class ReboardingController {

    private final ReboardingService reboardingService;

    public ReboardingController(ReboardingService reboardingService) {
        this.reboardingService = reboardingService;
    }

    @PostMapping("/register")
    public ResponseEntity registerReservation(ReservationCreationData reservation) {
        log.info("Reservation is requested by employee id:\t" + reservation.getEmployeeId());
        ResponseEntity responseEntity;
        if (!reboardingService.isEmployeeReservedGivenDay(reservation.getEmployeeId(), reservation.getReservedDate())) {
            EmployeeReservationData employeeReservation = reboardingService.handleReservationRequest(reservation);
            if (ReservationType.valueOf(employeeReservation.getReservationType()).equals(ReservationType.QUEUED)) {
                responseEntity = new ResponseEntity<>(employeeReservation.getPosition(), HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<>(HttpStatus.CREATED);
            }
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return responseEntity;
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Integer> getStatus(@PathVariable String employeeId) {

        log.info("Current status is requested by employeeId:\t" + employeeId);
        Integer currentPosition = reboardingService.getStatus(employeeId);

        return new ResponseEntity<>(currentPosition, HttpStatus.OK);
    }

    @PostMapping("/entry/{employeeId}")
    public ResponseEntity<Boolean> enterToOffice(@PathVariable String employeeId) {

        log.info("Employee by id: " + employeeId + " requested to enter to office");
        Boolean entered = reboardingService.enterEmployee(employeeId);

        return new ResponseEntity<>(entered, HttpStatus.OK);
    }

    @PostMapping("/exit/{employeeId}")
    public ResponseEntity<Void> exitFromOffice(@PathVariable String employeeId) {

        log.info("Employee by id: " + employeeId + " requested to exit from office");
        reboardingService.handleEmployeeExit(employeeId);
        log.info("Employee by id: " + employeeId + "left the office");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
