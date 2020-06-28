Feature: Daily Office Layout
 #Creates with all the usable workstations

  Scenario: A layout is generated based on the given parameters
    Given list of disabled workstations:
      | x      | y     |
      | 1131.0 | 107.0 |
      | 1181.0 | 107.0 |
      | 734.0  | 92.0  |
    And the minimum distance between workstations is 50 and the maximum number of employees are 50

    When service creates the map
    Then return saved map