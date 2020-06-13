Feature: Save Reservation
 #Ability to save reservation of an employee on given date by the predefined rules

  Scenario: Employee reservation is saved in the reservation list
    Given employee id and chosen date
      | id     | date       |
      | 1      | 2020-06-21 |

    When service decides where to save the reservation
    Then return saved reservation