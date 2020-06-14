Feature: Entry
 #Employee can enter the office

  Scenario Outline: Employee enters the office
    Given employee ID "<employeeId>"
    When service enters the qualified employee
    Then employee should be in office
    Examples:
      | employeeId |
      | 2          |

  Scenario Outline: Employee can't enter the office
    Given employee ID "<employeeId>"
    When service enters the qualified employee
    Then employee should not be in office
    Examples:
      | employeeId |
      | 3          |