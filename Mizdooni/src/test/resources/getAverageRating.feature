Feature: Add Review to Restaurant
  As a user, I want to add reviews with comments to a restaurant to reflect my experiences.

  Scenario: Add a new review with a comment to a restaurant
    Given a restaurant named "Sunset Grill" managed by a user "manager"
    And the restaurant already has the following reviews:
      | username  | food | service | ambiance | overall | comment             |
      | user1     | 4    | 5       | 4        | 4       | Great experience    |
    When a user with the username "user2" adds a review with the following details:
      | food     | 5    |
      | service  | 4    |
      | ambiance | 3    |
      | overall  | 4    |
      | comment  | Loved the ambiance |
    Then the restaurant should have 2 reviews
    And the reviews should include a review by "user2" with the following details:
      | food     | 5    |
      | service  | 4    |
      | ambiance | 3    |
      | overall  | 4    |
      | comment  | Loved the ambiance |