Feature: Entry
 #Employee can enter the office

  Scenario: Employee enters the office
    Given employee ID "<employeeId>"
    When service enters the qualified employee
    Then employee should be in office