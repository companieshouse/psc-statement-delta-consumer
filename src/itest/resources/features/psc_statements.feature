Feature: Psc Statements
  Scenario: Can transform and send a psc statement
    Given the application is running
    When the consumer receives a message
    Then a PUT request is sent to the psc statement data api with the encoded data

