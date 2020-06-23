Feature: Daily Office Layout
 #Creates a map of a given day with all the usable workstations

  Scenario: Given a date and disabled workstations a list of available workstations is created
    Given list of disabled workstations:
      | x      | y     |
      | 1131.0 | 107.0 |
      | 1181.0 | 107.0 |
      | 734.0  | 92.0  |

    When service creates the map
    Then return saved map