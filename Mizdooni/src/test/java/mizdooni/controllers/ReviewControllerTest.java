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
        when(ExistingRestaurant.getId()).thenReturn(existing_restaurant_id());
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
    void getReviews_When_NonExistingReviewForRestaurant_Then_Null() {
        try {
            stub_set_up_existing_restaurant();
            when(reviewService.getReviews(anyInt(), anyInt())).thenReturn(null);
            Response r = reviewController.getReviews(existing_restaurant_id(), DEFAULT_PAGE_NUM);

            assertEquals(HttpStatus.OK, r.getStatus());
            assertEquals(r.getMessage(), "reviews for restaurant (" + existing_restaurant_id() + "): " + ExistingRestaurant.getName());
            assertEquals(r.getData(), null);
            verify(restaurantService).getRestaurant(existing_restaurant_id());
        } catch (RestaurantNotFound e) {
            fail("Unexpected RestaurantNotFound exception thrown");
        }
    }

    @Test
    void getReviews_When_ExistingReview_Then_success() {
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
        Map<String, Object> ratingMap = Map.of("comment", "comment!", "rating", Map.of(
                "food", 1,
                "service", 1,
                "ambiance", 1,
                "overall", 1
        ));
        Response response = reviewController.addReview(existing_restaurant_id(), ratingMap);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());
        assertEquals(true, response.isSuccess());
    }

//    @Test
//    void testAddReview_UserNotFound() {
//        stub_set_up_existing_restaurant();
//        Map<String, Object> ratingMap = Map.of(
//                "comment", "comment!",
//                "rating", Map.of(
//                        "food", 1,
//                        "service", 1,
//                        "ambiance", 1,
//                        "overall", 1
//                )
//        );
//        try {
//
//            when(reviewService.addReview(anyInt(), any(Rating.class), anyString())).thenThrow(new UserNotFound());
//            reviewController.addReview(existing_restaurant_id(), ratingMap);
//            fail("Expected UserNotFound to be thrown");
//        } catch (UserNotFound exception) {
//            assertEquals(new UserNotFound(), exception);
//        }
//        catch (ManagerCannotReview o){
//            fail("Expected UserNotFound to be thrown");
//        }
//        catch (RestaurantNotFound o)
//        {
//            fail("Expected UserNotFound to be thrown");
//        }
//        catch (InvalidReviewRating o)
//        {
//            fail("Expected UserNotFound to be thrown");
//        }
//        catch (UserHasNotReserved o)
//        {
//            fail("Expected UserNotFound to be thrown");
//        }
//        verify(reviewService).addReview(anyInt(), any(Rating.class), anyString());
//    }
}
