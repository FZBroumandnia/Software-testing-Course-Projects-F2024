Feature: Update Review for Restaurant
  As a user, I want to update my review for a restaurant to reflect my updated experience.

  Scenario: A user updates their review after adding it
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
    And the user with the username "user2" updates their review with the following details:
      | food     | 3    |
      | service  | 5    |
      | ambiance | 4    |
      | overall  | 4    |
      | comment  | Changed my mind, it was better |
    Then the restaurant should have 2 reviews
    And the reviews should include a review by "user2" with the following updated details:
      | food     | 3    |
      | service  | 5    |
      | ambiance | 4    |
      | overall  | 4    |
      | comment  | Changed my mind, it was better |