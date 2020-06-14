Feature: Exit
 #Employee leaves the office

  Scenario Outline: Employee leaves the office
    Given employee ID "<employeeId>"
    When service signs out an already in office employee
    Then employee should not be in office
    Examples:
      | employeeId |
      | 2          |