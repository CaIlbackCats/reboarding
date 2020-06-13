package com.callbackcats.reboarding.StepDefinitions;

import com.callbackcats.reboarding.service.ReservationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Rollback
public class EntryStepDefinitions {

    @Autowired
    private ReservationService reservationService;

    private String currentEmployeeId;
    private boolean isValid;


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

}
