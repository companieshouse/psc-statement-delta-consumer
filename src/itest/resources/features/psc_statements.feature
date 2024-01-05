Feature: Psc Statements
  Scenario Outline: Can transform and send a psc statement
    Given the application is running
    When the consumer receives a message of <type>
    Then a PUT request is sent to the psc statement data api with the encoded data
    Examples:
    | type                |
    | "failed_to_confirm" |
    | "oe_nobody"         |
    | "oe_somebody"       |

  Scenario: Process invalid avro message
    Given the application is running
    When an invalid avro message is sent
    Then the message should be moved to topic psc-statement-delta-invalid

  Scenario: Process message with invalid data
    Given the application is running
    When a message with invalid data is sent
    Then the message should be moved to topic psc-statement-delta-invalid

  Scenario: Process message when the data api returns 400
    Given the application is running
    When the consumer receives a message failed_to_confirm but the data api returns a 400
    Then the message should be moved to topic psc-statement-delta-invalid

  Scenario: Process message when the data api returns 503
    Given the application is running
    When the consumer receives a message failed_to_confirm but the data api returns a 503
    Then the message should retry 3 times and then error