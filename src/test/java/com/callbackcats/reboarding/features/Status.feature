Feature: Status
 #Shows the employee's position in the queue

  Scenario Outline: Employee's position is shown for the current day
    Given employee ID "<currentEmployeeId>"
    When service looks for the position
    Then returns "<position>"
    Examples:
      | currentEmployeeId | position |
      | 5                 | 1        |