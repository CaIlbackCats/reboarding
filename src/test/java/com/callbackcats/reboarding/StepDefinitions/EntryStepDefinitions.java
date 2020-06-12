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

    private String currentUserId;
    private boolean isValid;


    @Given("User ID is {string}")
    public void user_id_is(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @When("Service check User ID")
    public void service_check_user_id() {
        isValid = reservationService.checkUserId(currentUserId);
    }

    @Then("It should return {string}")
    public void i_should_be_told(String expectedAnswer) {
        boolean expectedBoolean = Boolean.parseBoolean(expectedAnswer);
        assertEquals(expectedBoolean, isValid);
    }

}
