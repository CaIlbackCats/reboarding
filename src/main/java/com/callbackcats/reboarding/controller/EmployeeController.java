package com.callbackcats.reboarding.controller;

import com.callbackcats.reboarding.service.EmployeeReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/employees")
@Slf4j
public class EmployeeController {

    @Value("${employee-path}")
    private String employeePath;

    private final EmployeeReservationService employeeReservationService;

    public EmployeeController(EmployeeReservationService employeeReservationService) {
        this.employeeReservationService = employeeReservationService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<String> getEmployeeLayoutPathByDate(@PathVariable String employeeId, @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("Employee reservation layout path is requested");
        String path = employeeReservationService.getEmployeeReservationLayoutPath(employeeId, date);
        String url = employeePath + "layout/" + path;
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping(value = "/layout/{layoutPath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getEmployeeLayout(@PathVariable String layoutPath) {
        log.info("Employee reservation layout  is requested");
        byte[] layout;
        ResponseEntity<byte[]> responseEntity;
        try {
            layout = employeeReservationService.getEmployeeReservationLayout(layoutPath);
            responseEntity = new ResponseEntity<>(layout, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return responseEntity;
    }
}
