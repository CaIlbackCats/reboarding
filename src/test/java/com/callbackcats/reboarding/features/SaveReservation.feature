Feature: Save Reservation
 #Ability to save reservation of an employee on given date by the predefined rules

  Scenario: Employee reservation is saved in reserved
    Given employee id and chosen date
      | id | date       |
      | 5  | 2020-06-02 |

    When service decides where to save the reservation
    Then saved_reservation_should_have_proper_fields_and_should_be_reserved_type

  Scenario: Employee reservation is saved in a queue
    Given employee id and chosen date
      | id | date       |
      | 0  | 2020-05-01 |
    When service decides where to save the reservation
    Then saved_reservation_should_have_proper_fields_and_should_be_queued_type

  Scenario: There is no reservation for the day, it's created then saved
    Given employee id and chosen date
      | id | date       |
      | 0  | 2020-06-01 |
    When service decides where to save the reservation
    Then saved_reservation_should_return_correct_position_upon_new_reservation