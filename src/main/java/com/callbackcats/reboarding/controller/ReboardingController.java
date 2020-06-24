package com.callbackcats.reboarding.controller;

import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
import com.callbackcats.reboarding.service.ReboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity registerReservation(@RequestBody ReservationCreationData reservation) {
        log.info("Reservation is requested by employee id:\t" + reservation.getEmployeeId());

        boolean hasEmployeeAlreadyReservedDay = reboardingService.isEmployeeReservedGivenDay(reservation.getEmployeeId(), reservation.getReservedDate());
        if (hasEmployeeAlreadyReservedDay) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        EmployeeReservationData employeeReservation = reboardingService.handleReservationRequest(reservation);

        if (ReservationType.valueOf(employeeReservation.getReservationType()).equals(ReservationType.QUEUED)) {
            return new ResponseEntity<>(employeeReservation.getPosition(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Integer> getStatus(@PathVariable String employeeId) {

        log.info("Current status is requested by employeeId:\t" + employeeId);
        try {
            Integer currentPosition = reboardingService.getStatus(employeeId);
            return new ResponseEntity<>(currentPosition, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/entry/{employeeId}")
    public ResponseEntity<Boolean> enterToOffice(@PathVariable String employeeId) {
        log.info("Employee by id:\t" + employeeId + " requested to enter to office");

        if (reboardingService.enterEmployee(employeeId)) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/exit/{employeeId}")
    public ResponseEntity<Boolean> exitFromOffice(@PathVariable String employeeId) {
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        log.info("Employee by id: " + employeeId + " requested to exit from office");
        Boolean leftEmployee = reboardingService.handleEmployeeExit(employeeId);
        if (leftEmployee) {
            responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
            log.info("Employee by id: " + employeeId + "left the office");
        }
        return responseEntity;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> removeReservation(@RequestBody ReservationCreationData reservation) {
        log.info("Employee by id:\t" + reservation.getEmployeeId() + " requested to remove reservation for the day:\t" + reservation.getReservedDate());
        try {
            reboardingService.removeReservation(reservation.getEmployeeId(), reservation.getReservedDate());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.info("Employee by id:\t" + reservation.getEmployeeId() + " do not have reservation for the day:\t" + reservation.getReservedDate());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/layout")
    public ResponseEntity<Void> getDailyLayout(){
        log.info("Daily layout is requested");

        reboardingService.getCurrentOfficeLayout();

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
