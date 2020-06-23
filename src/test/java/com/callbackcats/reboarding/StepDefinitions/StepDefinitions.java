package com.callbackcats.reboarding.StepDefinitions;

import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.dto.*;
import com.callbackcats.reboarding.service.OfficeOptionsService;
import com.callbackcats.reboarding.service.EmployeeService;
import com.callbackcats.reboarding.service.ReboardingService;
import com.callbackcats.reboarding.service.WorkStationService;
import com.callbackcats.reboarding.util.InvalidLayoutException;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class StepDefinitions {

    @Autowired
    private ReboardingService reboardingService;

    @Autowired
    private OfficeOptionsService officeOptionsService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private WorkStationService workStationService;

    private String currentEmployeeId;
    private boolean isValid;
    private List<OfficeOptionsCreationData> capacities = new ArrayList<>();
    private List<CapacityData> savedCapacities = new ArrayList<>();
    private ReservationCreationData reservationCreationData;
    private EmployeeReservationData employeeReservationData;
    private Integer position;
    private List<PointData> disabledWorkstations;
    private List<WorkStation> workStations;

    @DataTableType
    public OfficeOptionsCreationData createCapacity(Map<String, String> dataTable) {
        return new OfficeOptionsCreationData(dataTable);
    }

    @DataTableType
    public ReservationCreationData createReservation(Map<String, String> dataTable) {
        return new ReservationCreationData(dataTable);
    }

    @DataTableType
    public PointData createPointData(Map<String, String> dataTable) {
        return new PointData(dataTable);
    }


    @Given("Employee ID is {string}")
    public void employee_id_is(String currentEmployeeId) {
        this.currentEmployeeId = currentEmployeeId;
    }

    @When("Service check Employee ID")
    public void service_check_employee_id() {
        isValid = reboardingService.isEmployeeReservedGivenDay(currentEmployeeId, LocalDate.now());
    }

    @Then("It should return {string}")
    public void it_should_return(String expectedAnswer) {
        boolean expectedBoolean = Boolean.parseBoolean(expectedAnswer);
        assertEquals(expectedBoolean, isValid);
    }


    @Given("^capacity with the following details:$")
    public void capacity_with_the_following_details(List<OfficeOptionsCreationData> officeOptionsCreationData) {
        this.capacities = officeOptionsCreationData;
    }

    @When("service saved the data")
    public void service_saves_the_data() {
        this.savedCapacities = officeOptionsService.saveCapacities(this.capacities);
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
        employeeReservationData = reboardingService.handleReservationRequest(reservationCreationData);
    }

    @Then("saved_reservation_should_have_proper_fields_and_should_be_reserved_type")
    public void saved_reservation_should_be_reserved_type() {
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.RESERVED, reservationType);
    }

    @Then("saved_reservation_should_have_proper_fields_and_should_be_queued_type")
    public void saved_reservation_should_be_queued_type() {
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.QUEUED, reservationType);
    }

    @Then("saved_reservation_should_return_correct_position_upon_new_reservation")
    public void saved_reservation_should_return_correct_position_upon_new_reservation() {
        ReservationType reservationType = ReservationType.valueOf(employeeReservationData.getReservationType());
        assertSame(ReservationType.RESERVED, reservationType);
    }


    @Given("employee ID {string}")
    public void employeeID(String employeeId) {
        this.currentEmployeeId = employeeId;
    }

    @When("service looks for the position")
    public void serviceLooksForThePosition() {
        this.position = this.reboardingService.getStatus(currentEmployeeId);
    }

    @Then("returns {string}")
    public void returns(String positionString) {
        Integer position = Integer.parseInt(positionString);
        assertEquals(position, this.position);
    }

    @When("service enters the qualified employee")
    public void serviceEntersTheQualifiedEmployee() {
        reboardingService.enterEmployee(this.currentEmployeeId);
    }

    @Then("employee should be in office")
    public void employeeShouldBeInOffice() {
        EmployeeData employee = employeeService.findEmployeeDataById(this.currentEmployeeId);
        assertTrue(employee.getInOffice());
    }

    @Then("employee should not be in office")
    public void employeeShouldNotBeInOffice() {
        EmployeeData employee = employeeService.findEmployeeDataById(this.currentEmployeeId);
        assertFalse(employee.getInOffice());
    }

    @When("service signs out an already in office employee")
    public void serviceSignsOutAnAlreadyInOfficeEmployee() {
        reboardingService.handleEmployeeExit(currentEmployeeId);
    }


    @Given("list of disabled workstations:")
    public void listOfDisabledWorkstations(List<PointData> disabledWorkstations) {
        this.disabledWorkstations = disabledWorkstations;
    }

    @When("service creates the map")
    public void serviceCreatesTheMap() {
        LocalDate date = LocalDate.of(2020, 5, 17);
        this.workStations = workStationService.generateLayoutWithRange(this.disabledWorkstations, date);

    }


    @Then("return saved map")
    public void returnSavedMap() {
        assertNotNull(workStations);
        assertEquals(50, workStations.size());
    }
}
