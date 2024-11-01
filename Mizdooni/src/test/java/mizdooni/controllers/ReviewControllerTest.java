package mizdooni.controllers;

import mizdooni.model.*;
import mizdooni.service.*;
import mizdooni.response.*;
import mizdooni.exceptions.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    final int DEFAULT_PAGE_NUM = 1;
    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private Restaurant ExistingRestaurant;

    @Mock
    private Review mockReview;

    int existing_restaurant_id() {
        return 1;
    }

    int non_existing_restaurant_id() {
        return 9;
    }

    Map<String, Object> make_valid_map_rating()
    {
        return Map.of(
                "comment", "comment!",
                "rating", Map.of(
                        "food", 1,
                        "service", 1,
                        "ambiance", 1,
                        "overall", 1
                )
        );
    }

    Rating make_a_rating() {
        Rating r = new Rating();
        r.service = 1;
        r.food = 1;
        r.ambiance = 1;
        r.overall = 1;
        return  r;
    }

    void stub_set_up_existing_restaurant() {
        lenient().when(ExistingRestaurant.getName()).thenReturn("existing restaurant");
        lenient().when(ExistingRestaurant.getId()).thenReturn(existing_restaurant_id());
        when(restaurantService.getRestaurant(existing_restaurant_id())).thenReturn(ExistingRestaurant);
    }

    @Test
    void getReviews_When_NonExistingRestaurant_Then_NotFound() {
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reviewController.getReviews(non_existing_restaurant_id(), DEFAULT_PAGE_NUM);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
        verify(restaurantService).getRestaurant(non_existing_restaurant_id());
        verifyNoInteractions(reviewService);
    }

    @Test
    void getReviews_When_ValidReview_Then_success() {
        try {
            PagedList<Review> pagedList = new PagedList<>(new ArrayList<>(List.of(mockReview)), DEFAULT_PAGE_NUM, 10);
            stub_set_up_existing_restaurant();
            when(reviewService.getReviews(existing_restaurant_id(), DEFAULT_PAGE_NUM)).thenReturn(pagedList);
            Response response = reviewController.getReviews(existing_restaurant_id(), DEFAULT_PAGE_NUM);

            assertEquals(HttpStatus.OK, response.getStatus());
            assertEquals("reviews for restaurant (" + existing_restaurant_id() + "): " + ExistingRestaurant.getName(), response.getMessage());
            assertTrue(response.isSuccess());
            assertEquals(pagedList, response.getData());
            verify(restaurantService).getRestaurant(existing_restaurant_id());
            verify(reviewService).getReviews(existing_restaurant_id(), DEFAULT_PAGE_NUM);
        } catch (RestaurantNotFound e) {
            fail("Unexpected RestaurantNotFound exception thrown");
        }
    }

    @Test
    void addReviews_When_ExistingRestaurant_Then_success() {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = make_valid_map_rating();
        Response response = reviewController.addReview(existing_restaurant_id(), ratingMap);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());
        assertEquals(true, response.isSuccess());
    }

    @Test
    void addReview_When_NoRatingMap_Then_ParameterMissing()
    {
        stub_set_up_existing_restaurant();
        Map<String, Object> InValidratingMap = Map.of( "s.th", " " , " ",Map.of(" ", 1));
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reviewController.addReview(existing_restaurant_id(), InValidratingMap);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("parameters missing", exception.getMessage());
        verifyNoInteractions(reviewService);
    }

    @Test
    void addReview_When_InValidRatingTypes_Then_ParameterBadType()
    {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = Map.of("comment", "comment!", "rating", Map.of(
                "food", "food type",
                "service", 1,
                "ambiance", 1,
                "overall", 1
        ));
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reviewController.addReview(existing_restaurant_id(), ratingMap);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
        verifyNoInteractions(reviewService);
    }

    @Test
    void addReview_When_NotLogInAccount_Then_UserNotFound()
            throws UserNotFound, ManagerCannotReview, RestaurantNotFound, InvalidReviewRating, UserHasNotReserved {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = make_valid_map_rating();
        try {
            doThrow(new UserNotFound()).when(reviewService).addReview(anyInt(), any(), anyString());
            reviewController.addReview(existing_restaurant_id(), ratingMap);
        }
        catch (Throwable e){
            assertTrue(e.getClass()==ResponseException.class);
            assertEquals(e.getMessage(), "User not found.");
            verify(reviewService).addReview(anyInt(), any(Rating.class), anyString());
        }
    }

    @Test
    void addReview_When_ManagerWriteReview_Then_ManagerCannotReview()
            throws UserNotFound, ManagerCannotReview, RestaurantNotFound, InvalidReviewRating, UserHasNotReserved {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = make_valid_map_rating();
        try {
            doThrow(new ManagerCannotReview()).when(reviewService).addReview(anyInt(), any(), anyString());
            reviewController.addReview(existing_restaurant_id(), ratingMap);
        }
        catch (Throwable e){
            assertTrue(e.getClass()==ResponseException.class);
            assertEquals(e.getMessage(), "Manager cannot review.");
            verify(reviewService).addReview(anyInt(), any(Rating.class), anyString());
        }
    }

    @Test
    void addReview_When_InvalidRating_Then_InvalidReviewRating()
            throws UserNotFound, ManagerCannotReview, RestaurantNotFound, InvalidReviewRating, UserHasNotReserved {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = make_valid_map_rating();
        try {
            doThrow(new InvalidReviewRating("cause")).when(reviewService).addReview(anyInt(), any(), anyString());
            reviewController.addReview(existing_restaurant_id(), ratingMap);
        }
        catch (Throwable e){
            assertTrue(e.getClass()==ResponseException.class);
            assertEquals(e.getMessage(), "Review rating parameter <cause> out of range.");
            verify(reviewService).addReview(anyInt(), any(Rating.class), anyString());
        }
    }

    @Test
    void addReview_When_ReviewWithoutExperience_Then_UserHasNotReserved()
            throws UserNotFound, ManagerCannotReview, RestaurantNotFound, InvalidReviewRating, UserHasNotReserved {
        stub_set_up_existing_restaurant();
        Map<String, Object> ratingMap = make_valid_map_rating();
        try {
            doThrow(new UserHasNotReserved()).when(reviewService).addReview(anyInt(), any(), anyString());
            reviewController.addReview(existing_restaurant_id(), ratingMap);
        }
        catch (Throwable e){
            assertTrue(e.getClass()==ResponseException.class);
            assertEquals(e.getMessage(), "User cannot review a restaurant without reserving a table first.");
            verify(reviewService).addReview(anyInt(), any(Rating.class), anyString());
        }
    }

}
