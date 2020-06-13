package com.callbackcats.reboarding.StepDefinitions;

import com.callbackcats.reboarding.dto.CapacityCreationData;
import com.callbackcats.reboarding.dto.CapacityData;
import com.callbackcats.reboarding.dto.ReservationCreationData;
import com.callbackcats.reboarding.dto.EmployeeReservationData;
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

    @DataTableType
    public CapacityCreationData createCapacity(Map<String, String> dataTable) {
        return new CapacityCreationData(dataTable);
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
        //save
        reservationService.saveReservation(reservationCreationData);
    }

    @Then("return saved reservation")
    public void return_saved_reservation() {
        assertNotNull(employeeReservationData);
    }
}
