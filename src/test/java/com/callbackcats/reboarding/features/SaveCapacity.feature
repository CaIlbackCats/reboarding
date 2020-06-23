Feature: Ability to save capacities

  Scenario: Given interval of dates and capacity values are saved to database
    Given capacity with the following details:
      | max | capacityValue | startDate  | endDate    | minDistance |
      | 250 | 10            | 2020-06-13 | 2020-06-19 | 5           |
      | 250 | 20            | 2020-06-20 | 2020-06-26 | 4           |
      | 250 | 30            | 2020-06-27 | 2020-07-03 | 3           |
      | 250 | 50            | 2020-07-04 | 2020-07-10 | 2           |
      | 250 | 100           | 2020-07-11 | 2020-07-17 | 0           |

    When service saved the data
    Then return saved capacity data