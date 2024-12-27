Feature: Add Reservations
  As a user, I want to add multiple reservations so that I can book tables at different restaurants.

  Scenario: Add a new reservation to a user's reservation list
    Given a user with the username "john_doe"
    And the user already has 1 reservation for a restaurant named "Cafe Delight"
    When the user adds a reservation for a restaurant named "Gourmet Bistro"
    Then the reservation list should contain 2 reservations
    And one of the reservations should be for the restaurant named "Gourmet Bistro"
    And the other reservation should be for the restaurant named "Cafe Delight"
