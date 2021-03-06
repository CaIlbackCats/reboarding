Feature: Employee reservation for the current day

  Scenario Outline: Employee ID is reserved today or not
    Given Employee ID is "<currentEmployeeId>"
    When Service check Employee ID
    Then It should return "<valid>"

    Examples:
      | currentEmployeeId | valid |
      | 0                 | true  |
      | 1                 | false |