package com.callbackcats.reboarding.controller;

import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
import com.callbackcats.reboarding.service.ReboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@Slf4j
@RequestMapping("/api")
public class ReboardingController {

    private final ReboardingService reboardingService;

    public ReboardingController(ReboardingService reboardingService) {
        this.reboardingService = reboardingService;
    }

    @PostMapping("/register")
    public ResponseEntity<Integer> registerReservation(@RequestBody ReservationCreationData reservation) {
        ResponseEntity<Integer> responseEntity;
        log.info("Reservation is requested by employee id:\t" + reservation.getEmployeeId());
        boolean hasEmployeeAlreadyReservedDay = reboardingService.isEmployeeReservedGivenDay(reservation.getEmployeeId(), reservation.getReservedDate());
        EmployeeReservationData employeeReservation = reboardingService.handleReservationRequest(reservation);
        if (hasEmployeeAlreadyReservedDay) {
            responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (ReservationType.valueOf(employeeReservation.getReservationType()).equals(ReservationType.QUEUED)) {
            responseEntity = new ResponseEntity<>(employeeReservation.getPosition(), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.CREATED);
        }
        return responseEntity;
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Integer> getStatus(@PathVariable String employeeId) {
        ResponseEntity<Integer> responseEntity;
        log.info("Current status is requested by employeeId:\t" + employeeId);
        try {
            Integer currentPosition = reboardingService.getStatus(employeeId);
            responseEntity = new ResponseEntity<>(currentPosition, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @PostMapping("/entry/{employeeId}")
    public ResponseEntity<Boolean> enterToOffice(@PathVariable String employeeId) {
        ResponseEntity<Boolean> responseEntity;
        log.info("Employee by id:\t" + employeeId + " requested to enter to office");

        if (reboardingService.enterEmployee(employeeId)) {
            responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return responseEntity;
    }

    @PostMapping("/exit/{employeeId}")
    public ResponseEntity<Boolean> exitFromOffice(@PathVariable String employeeId) {
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        log.info("Employee by id: " + employeeId + " requested to exit from office");
        Boolean leftEmployee = reboardingService.handleEmployeeExit(employeeId);
        if (leftEmployee) {
            responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
            log.info("Employee by id: " + employeeId + " left the office");
        }
        return responseEntity;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> removeReservation(@RequestBody ReservationCreationData reservation) {
        ResponseEntity<Void> responseEntity;
        log.info("Employee by id:\t" + reservation.getEmployeeId() + " requested to remove reservation for the day:\t" + reservation.getReservedDate());
        try {
            reboardingService.removeReservation(reservation.getEmployeeId(), reservation.getReservedDate());
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.info("Employee by id:\t" + reservation.getEmployeeId() + " do not have reservation for the day:\t" + reservation.getReservedDate());
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @GetMapping(value = "/layout", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getDailyLayout() {
        log.info("Daily layout is requested");

        byte[] layout = reboardingService.getCurrentOfficeLayout();

        return new ResponseEntity<>(layout, HttpStatus.OK);
    }


}
