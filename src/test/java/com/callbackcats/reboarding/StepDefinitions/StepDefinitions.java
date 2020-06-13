package com.callbackcats.reboarding.StepDefinitions;

import com.callbackcats.reboarding.domain.Reservation;
import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.dto.*;
import com.callbackcats.reboarding.service.ReservationService;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class StepDefinitions {

    @Autowired
    private ReservationService reservationService;

    private String currentEmployeeId;
    private boolean isValid;
    private List<CapacityCreationData> capacities = new ArrayList<>();
    private List<CapacityData> savedCapacities = new ArrayList<>();
    private ReservationCreationData reservationCreationData;
    private EmployeeReservationData employeeReservationData;
    private Integer position;

    @DataTableType
    public CapacityCreationData createCapacity(Map<String, String> dataTable) {
        return new CapacityCreationData(dataTable);
    }

    @DataTableType
    public ReservationCreationData createReservation(Map<String, String> dataTable) {
        return new ReservationCreationData(dataTable);
    }


    @Given("Employee ID is {string}")
    public void employee_id_is(String currentEmployeeId) {
        this.currentEmployeeId = currentEmployeeId;
    }

    @When("Service check Employee ID")
    public void service_check_employee_id() {
        isValid = reservationService.isEmployeeIdReservedToday(currentEmployeeId);
    }

    @Then("It should return {string}")
    public void it_should_return(String expectedAnswer) {
        boolean expectedBoolean = Boolean.parseBoolean(expectedAnswer);
        assertEquals(expectedBoolean, isValid);
    }


    @Given("^capacity with the following details:$")
    public void capacity_with_the_following_details(List<CapacityCreationData> capacityCreationData) {
        this.capacities = capacityCreationData;
    }

    @When("service saved the data")
    public void service_saves_the_data() {
        this.savedCapacities = reservationService.saveCapacities(this.capacities);
    }

    @Then("return saved capacity data")
    public void return_saved_capacity_data() {
        assertFalse(this.savedCapacities.isEmpty());
    }


    @Given("employee id and chosen date")
    public void employee_id_and_chosen_date(ReservationCreationData reservationCreationData) {
        this.reservationCreationData = reservationCreationData;
    }

    @When("service decides where to save the reservation")
    public void service_decides_where_to_save_the_reservations() {
        employeeReservationData = reservationService.saveReservation(reservationCreationData);
    }

    @Then("saved_reservation_should_have_proper_fields_and_should_be_reserved_type")
    public void saved_reservation_should_be_reserved_type() {
        assertEquals(4, employeeReservationData.getPosition());
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.RESERVED, reservationType);
    }

    @Then("saved_reservation_should_have_proper_fields_and_should_be_queued_type")
    public void saved_reservation_should_be_queued_type() {
        assertEquals(1, employeeReservationData.getPosition());
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.QUEUED, reservationType);
    }

    @Then("saved_reservation_should_return_correct_position_upon_new_reservation")
    public void saved_reservation_should_return_correct_position_upon_new_reservation() {
        assertEquals(1, employeeReservationData.getPosition());
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.RESERVED, reservationType);
    }


    @Given("employee ID {string}")
    public void employeeID(String employeeId) {
        this.currentEmployeeId = employeeId;
    }

    @When("service looks for the position")
    public void serviceLooksForThePosition() {
        this.position = this.reservationService.findPosition(currentEmployeeId);
    }

    @Then("returns {string}")
    public void returns(String positionString) {
        Integer position = Integer.parseInt(positionString);
        assertEquals(position, this.position);
    }
}
