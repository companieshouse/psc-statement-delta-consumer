Feature: Psc Statements delete

  Scenario: send DELETE request to the data api
    Given the application is running
    When the consumer receives a delete payload
    Then a DELETE request is sent to the psc statement data api with the encoded Id

  Scenario: send DELETE with invalid JSON
    Given the application is running
    When the consumer receives an invalid delete payload
    Then the message should retry 3 times and then error

  Scenario Outline: send DELETE with 400 from data api
    Given the application is running
    When the consumer receives a delete message but the data api returns a <status_code>
    Then the message should be moved to topic psc-statement-delta-invalid
    Examples:
      | status_code |
      | 400         |
      | 409         |

  Scenario Outline: send DELETE with retryable response from data api
    Given the application is running
    When the consumer receives a delete message but the data api returns a <status_code>
    Then the message should retry 3 times and then error
    Examples:
      | status_code |
      | 401         |
      | 403         |
      | 404         |
      | 405         |
      | 500         |
      | 503         |