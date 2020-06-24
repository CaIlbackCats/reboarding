Feature: Ability to save capacities

  Scenario: Given interval of dates and capacity values are saved to database
    Given capacity with the following details:
      | max | capacityValue | startDate  | endDate    | minDistance |
      | 250 | 10            | 2020-01-01 | 2020-01-31 | 5           |
      | 250 | 20            | 2020-02-01 | 2020-02-28 | 4           |
      | 250 | 30            | 2020-03-01 | 2020-03-31 | 3           |
      | 250 | 50            | 2020-04-01 | 2020-04-15 | 2           |
      | 250 | 100           | 2020-04-16 | 2020-04-30 | 0           |

    When service saved the data
    Then return saved capacity data