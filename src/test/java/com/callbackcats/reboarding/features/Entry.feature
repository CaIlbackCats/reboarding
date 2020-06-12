Feature: Entry

  Scenario Outline: User ID is valid or not
    Given User ID is "<currentUserId>"
    When Service check User ID
    Then It should return "<valid>"

    Examples:
      | currentUserId | valid |
      | 0             | true  |
      | 1             | false |

  Scenario Outline:

    Examples: