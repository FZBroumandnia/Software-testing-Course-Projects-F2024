import io.cucumber.java.en.*;
import mizdooni.MizdooniApplication;
import mizdooni.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.Arrays;

@CucumberContextConfiguration
@SpringBootTest(classes = MizdooniApplication.class)
public class UserScenarioTest {
    private User user;
    private User manager;
    private Reservation initialReservation;
    private Reservation newReservation;
    private Restaurant restaurant;
    private Rating rating;

    private RatingDetails parseRatingDetails(io.cucumber.datatable.DataTable dataTable) {
        List<List<String>> rows = dataTable.asLists(String.class);
        Rating rating = new Rating();
        String comment = "";

        for (List<String> row : rows) {
            String key = row.get(0).toLowerCase();
            String value = row.get(1);
            switch (key) {
                case "food":
                    rating.food = Integer.parseInt(value);
                    break;
                case "service":
                    rating.service = Integer.parseInt(value);
                    break;
                case "ambiance":
                    rating.ambiance = Integer.parseInt(value);
                    break;
                case "overall":
                    rating.overall = Integer.parseInt(value);
                    break;
                case "comment":
                    comment = value;
                    break;
                default:
                    fail("Unexpected detail key: " + key);
            }
        }

        return new RatingDetails(rating, comment);
    }

    private static class RatingDetails {
        final Rating rating;
        final String comment;

        RatingDetails(Rating rating, String comment) {
            this.rating = rating;
            this.comment = comment;
        }
    }

    @Given("a user with the username {string}")
    public void aUserWithTheUsername(String username) {
        user = new User(username, "123", "s@gmail.com",
                new Address("iran", "tehran", "tajrish"), User.Role.client);
    }

    @And("the user already has {int} reservation for a restaurant named {string}")
    public void theUserAlreadyHasReservationForARestaurantNamed(int count, String restaurantName) {
        Address address = new Address("Iran", "Shiraz", "Hafez");
        manager = new User("manager", "123", "manager@gmail.com", address, User.Role.manager);
        Restaurant initialRestaurant = new Restaurant(restaurantName, manager, "Fast Food",
                LocalTime.of(9, 0), LocalTime.of(23, 0), "Disgusting", address, "image.jpg");
        initialReservation = new Reservation(user, initialRestaurant, new Table(1, 2, 3), LocalDateTime.now());
        user.addReservation(initialReservation);
        assertEquals(count, user.getReservations().size());
    }

    @When("the user adds a reservation for a restaurant named {string}")
    public void theUserAddsAReservationForARestaurantNamed(String restaurantName) {
        Restaurant initialRestaurant = new Restaurant(restaurantName, manager, "Fast Food",
                LocalTime.of(9, 0), LocalTime.of(23, 0), "Disgusting",
                new Address("iran", "tehran", "tajrish"), "image.jpg");
        newReservation = new Reservation(user, initialRestaurant, new Table(1, 2, 3), LocalDateTime.now());
        user.addReservation(newReservation);
    }

    @Then("the reservation list should contain {int} reservations")
    public void theReservationListShouldContainReservations(int count) {
        List<Reservation> reservations = user.getReservations();
        assertEquals(count, reservations.size(), "The total reservation count should match.");
    }

    @And("one of the reservations should be for the restaurant named {string}")
    public void oneOfTheReservationsShouldBeForTheRestaurantNamed(String expectedName) {
        assertTrue(
                user.getReservations().stream()
                        .anyMatch(r -> r.getRestaurant().getName().equals(expectedName)),
                "The reservation list should contain a reservation for the specified restaurant."
        );
    }

    @And("the other reservation should be for the restaurant named {string}")
    public void theOtherReservationShouldBeForTheRestaurantNamed(String expectedName) {
        assertTrue(
                user.getReservations().stream()
                        .anyMatch(r -> r.getRestaurant().getName().equals(expectedName)),
                "The reservation list should also contain the initial reservation for the specified restaurant."
        );
    }

    @Given("a restaurant named {string} managed by a user {string}")
    public void aRestaurantNamedManagedByAUser(String restaurantName, String managerName) {
        Address address = new Address("Iran", "Tehran", "Valiasr");
        manager = new User(managerName, "123", managerName + "@example.com", address, User.Role.manager);
        restaurant = new Restaurant(restaurantName, manager, "Fine Dining",
                LocalTime.of(9, 0), LocalTime.of(23, 0),
                "Exquisite dining experience", address, "image.jpg");
    }

    @And("the restaurant has the following reviews:")
    public void theRestaurantHasTheFollowingReviews(io.cucumber.datatable.DataTable dataTable) {
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows.subList(1, rows.size())) {
            String username = row.get(0);
            int food = Integer.parseInt(row.get(1));
            int service = Integer.parseInt(row.get(2));
            int ambiance = Integer.parseInt(row.get(3));
            int overall = Integer.parseInt(row.get(4));

            User reviewer = new User(username, "123", username + "@example.com", null, User.Role.client);
            rating = new Rating();
            rating.food = food;
            rating.service = service;
            rating.ambiance = ambiance;
            rating.overall = overall;

            Review review = new Review(reviewer, rating, "Great food!", LocalDateTime.now());
            restaurant.addReview(review);
        }
    }

    @When("I calculate the average rating of the restaurant")
    public void iCalculateTheAverageRatingOfTheRestaurant() {
        // This step is handled in the next step
    }

    @Then("the average rating should be:")
    public void theAverageRatingShouldBe(io.cucumber.datatable.DataTable dataTable) {
        List<List<String>> rows = dataTable.asLists(String.class);
        Rating averageRating = restaurant.getAverageRating();

        for (List<String> row : rows.subList(1, rows.size())) {
            String category = row.get(0);
            double expectedValue = Double.parseDouble(row.get(1));

            switch (category.toLowerCase()) {
                case "food":
                    assertEquals(expectedValue, averageRating.food, 0.1, "Food rating mismatch");
                    break;
                case "service":
                    assertEquals(expectedValue, averageRating.service, 0.1, "Service rating mismatch");
                    break;
                case "ambiance":
                    assertEquals(expectedValue, averageRating.ambiance, 0.1, "Ambiance rating mismatch");
                    break;
                case "overall":
                    assertEquals(expectedValue, averageRating.overall, 0.1, "Overall rating mismatch");
                    break;
                default:
                    fail("Unexpected rating category: " + category);
            }
        }
    }

    @And("the restaurant already has the following reviews:")
    public void theRestaurantAlreadyHasTheFollowingReviews(io.cucumber.datatable.DataTable dataTable) {
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows.subList(1, rows.size())) {
            String username = row.get(0);
            int food = Integer.parseInt(row.get(1));
            int service = Integer.parseInt(row.get(2));
            int ambiance = Integer.parseInt(row.get(3));
            int overall = Integer.parseInt(row.get(4));
            String comment = row.get(5);

            User reviewer = new User(username, "123", username + "@example.com", null, User.Role.client);
            rating = new Rating();
            rating.food = food;
            rating.service = service;
            rating.ambiance = ambiance;
            rating.overall = overall;

            Review review = new Review(reviewer, rating, comment, LocalDateTime.now());
            restaurant.addReview(review);
        }
    }

    @When("a user with the username {string} adds a review with the following details:")
    public void aUserWithTheUsernameAddsAReviewWithTheFollowingDetails(String username,
                                                                       io.cucumber.datatable.DataTable dataTable) {
        RatingDetails details = parseRatingDetails(dataTable);
        User reviewer = new User(username, "123", username + "@example.com", null, User.Role.client);
        Review review = new Review(reviewer, details.rating, details.comment, LocalDateTime.now());
        restaurant.addReview(review);
    }

    @Then("the restaurant should have {int} reviews")
    public void theRestaurantShouldHaveReviews(int reviewCount) {
        List<Review> reviews = restaurant.getReviews();
        assertEquals(reviewCount, reviews.size(), "The number of reviews should match the expected count.");
    }

    @And("the reviews should include a review by {string} with the following details:")
    public void theReviewsShouldIncludeAReviewByWithTheFollowingDetails(String username,
                                                                        io.cucumber.datatable.DataTable dataTable) {
        RatingDetails expectedDetails = parseRatingDetails(dataTable);

        assertTrue(
                restaurant.getReviews().stream().anyMatch(r ->
                        r.getUser().getUsername().equals(username) &&
                                r.getRating().food == expectedDetails.rating.food &&
                                r.getRating().service == expectedDetails.rating.service &&
                                r.getRating().ambiance == expectedDetails.rating.ambiance &&
                                r.getRating().overall == expectedDetails.rating.overall
                ),
                "The reviews should include a review by " + username + " with the expected details."
        );
    }

    @When("the user with the username {string} updates their review with the following details:")
    public void theUserWithTheUsernameUpdatesTheirReviewWithTheFollowingDetails(String username,
                                                                                io.cucumber.datatable.DataTable dataTable) {
        RatingDetails updatedDetails = parseRatingDetails(dataTable);

        Review existingReview = restaurant.getReviews().stream()
                .filter(r -> r.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Review not found for user " + username));

        existingReview.getRating().food = updatedDetails.rating.food;
        existingReview.getRating().service = updatedDetails.rating.service;
        existingReview.getRating().ambiance = updatedDetails.rating.ambiance;
        existingReview.getRating().overall = updatedDetails.rating.overall;
    }

    @Then("the reviews should include a review by {string} with the following updated details:")
    public void theReviewsShouldIncludeAReviewByWithTheFollowingUpdatedDetails(String username,
                                                                               io.cucumber.datatable.DataTable dataTable) {
        RatingDetails expectedDetails = parseRatingDetails(dataTable);

        assertTrue(
                restaurant.getReviews().stream().anyMatch(r ->
                        r.getUser().getUsername().equals(username) &&
                                r.getRating().food == expectedDetails.rating.food &&
                                r.getRating().service == expectedDetails.rating.service &&
                                r.getRating().ambiance == expectedDetails.rating.ambiance &&
                                r.getRating().overall == expectedDetails.rating.overall
                ),
                "The reviews should include a review by " + username + " with the expected details."
        );
    }
}
