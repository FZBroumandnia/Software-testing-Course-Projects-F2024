package mizdooni.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantTest {

    Restaurant restaurant;
    @Mock
    User dummyUser;
    @Mock
    Address address;
    Review randomReview1;
    Review randomReview2;
    LocalTime startTime = LocalTime.of(12, 0);
    LocalTime endTime = LocalTime.of(23, 0);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        restaurant = new Restaurant("name", dummyUser, "type", startTime, endTime, "description", address, "imageLink");
        randomReview1 = new Review(dummyUser, new Rating(), "1", LocalDateTime.now());
        randomReview2 = new Review(dummyUser, new Rating(), "2", LocalDateTime.now());
    }

    private Table make_random_table() {
        return new Table(1, restaurant.getId(), 10);
    }

    private Table make_random_table(int seatsNumber) {
        return new Table(1, restaurant.getId(), seatsNumber);
    }

    private static Rating setRating(Rating rating, double food, double service, double ambiance, double overall) {
        rating.food = food;
        rating.service = service;
        rating.ambiance = ambiance;
        rating.overall = overall;
        return rating;
    }

    @Test
    public void addTable_When_TableAdded_Then_Exist() {
        Table table = make_random_table();
        restaurant.addTable(table);
        List<Table> tables = restaurant.getTables();
        assertTrue(tables.stream().anyMatch(t -> t.getTableNumber() == table.getTableNumber()));
    }

    @Test
    public void getTable_When_TableAdded_Then_Find() {
        Table table = make_random_table();
        restaurant.addTable(table);
        Table t = restaurant.getTable(table.getTableNumber());
        assertNotNull(t);
        assertEquals(t.getTableNumber(), table.getTableNumber());
    }

    @Test
    public void getTable_When_TableNotAdded_Then_NULL() {
        Table table = make_random_table();
        assertNull(restaurant.getTable(table.getTableNumber()));
    }

    @Test
    public void addReview_When_ReviewAdded_Then_Exist() {
        restaurant.addReview(randomReview1);
        List<Review> reviews = restaurant.getReviews();
        assertTrue(reviews.stream().anyMatch(r -> r.equals(randomReview1)));
    }

    @Test
    public void addReview_When_TwoReviewFromAUser_Then_LatestOneExist() {
        restaurant.addReview(randomReview1);
        restaurant.addReview(randomReview2);
        List<Review> reviews = restaurant.getReviews();
        assertFalse(reviews.stream().anyMatch(r -> r.equals(randomReview1)));
        assertTrue(reviews.stream().anyMatch(r -> r.equals(randomReview2)));
    }

    @Test
    public void getMaxSeatsNumber_When_NoTable_Then_Zero() {
        assertEquals(0, restaurant.getMaxSeatsNumber());
    }

    static Stream<Arguments> generateTableSeatsNumber() {
        return Stream.of(
                Arguments.of(Arrays.asList(1, 7, 2, 5), 7),
                Arguments.of(Arrays.asList(8, 8, 8), 8)
        );
    }

    @ParameterizedTest
    @MethodSource("generateTableSeatsNumber")
    public void getMaxSeatsNumber_When_MoreThanOneTable_Then_Max(List<Integer> seatsNumbers, int max) {
        for (int s : seatsNumbers) {
            restaurant.addTable(make_random_table(s));
        }
        assertEquals(max, restaurant.getMaxSeatsNumber());
    }

    @Test
    public void getMaxSeatsNumber_When_OneTable_Then_TableSeatsNumber() {
        Table t = make_random_table();
        restaurant.addTable(t);
        assertEquals(t.getSeatsNumber(), restaurant.getMaxSeatsNumber());
    }

    @Test
    public void getAverageRating_When_NoReview_Then_Zero() {
        Rating rating = setRating(new Rating(), 0, 0, 0, 0);
        Rating actualAverage = restaurant.getAverageRating();

        assertEquals(rating.food, actualAverage.food);
        assertEquals(rating.service, actualAverage.service);
        assertEquals(rating.ambiance, actualAverage.ambiance);
        assertEquals(rating.overall, actualAverage.overall);
    }


    static Stream<Arguments> provideRatingsAndExpectedAverages() {
        return Stream.of(

                Arguments.of(new Rating[]{}, setRating(new Rating(), 0, 0, 0, 0)),

                Arguments.of(new Rating[]{
                                setRating(new Rating(), 4, 5, 3, 4)},
                        setRating(new Rating(), 4, 5, 3, 4)
                ),

                Arguments.of(new Rating[]{
                                setRating(new Rating(), 4, 5, 3, 4),
                                setRating(new Rating(), 2, 3, 4, 3)
                        }, setRating(new Rating(), 3, 4, 3.5, 3.5)
                ),

                Arguments.of(new Rating[]{
                                setRating(new Rating(), 5, 5, 5, 5),
                                setRating(new Rating(), 3, 3, 3, 3),
                                setRating(new Rating(), 4, 4, 4, 4)
                        }, setRating(new Rating(), 4, 4, 4, 4)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideRatingsAndExpectedAverages")
    public void getAverageRating_When_DifferentRatingScenario_Then_Average(Rating[] ratings, Rating expectedAverage) {
        for (Rating rating : ratings) {
            Review review = new Review(Mockito.mock(User.class), rating, "", LocalDateTime.now());
            restaurant.addReview(review);
        }
        Rating actualAverage = restaurant.getAverageRating();

        assertEquals(expectedAverage.food, actualAverage.food);
        assertEquals(expectedAverage.service, actualAverage.service);
        assertEquals(expectedAverage.ambiance, actualAverage.ambiance);
        assertEquals(expectedAverage.overall, actualAverage.overall);
    }
}
