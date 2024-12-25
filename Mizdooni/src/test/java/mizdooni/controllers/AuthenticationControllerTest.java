package mizdooni.controllers;

import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // === Tests for User Retrieval ===

    @Test
    void getUser_WhenNoUserLoggedIn_ThrowsUnauthorizedException() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.user();
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    @Test
    void login_WhenValidCredentials_ReturnsSuccessfulResponse() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "validUser");
        credentials.put("password", "validPass");

        when(userService.login("validUser", "validPass")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(new User("validUser", "123", "valid@example.com", new Address("Country", "City", null), User.Role.client));

        Response response = authenticationController.login(credentials);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("login successful", response.getMessage());
    }

    @Test
    void login_WhenInvalidCredentials_ThrowsUnauthorizedException() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "invalidUser");
        credentials.put("password", "wrongPass");

        when(userService.login("invalidUser", "wrongPass")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.login(credentials);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("invalid username or password", exception.getMessage());
    }

    @Test
    void login_WhenMissingParameters_ThrowsBadRequestException() {
        Map<String, String> emptyCredentials = new HashMap<>();

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.login(emptyCredentials);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("parameters missing", exception.getMessage());
    }

    // === Tests for Signup ===

    @Test
    void signup_WhenAllParametersValid_ReturnsSuccessfulResponse() throws Exception {
        Map<String, Object> signupData = createValidSignupData();

        doNothing().when(userService).signup(anyString(), anyString(), anyString(), any(), any());
        when(userService.login(anyString(), anyString())).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(new User("testUser", "testPass", "test@example.com", new Address("Country", "City", null), User.Role.manager));

        Response response = authenticationController.signup(signupData);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("signup successful", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void signup_WhenParametersMissing_ThrowsBadRequestException() {
        Map<String, Object> incompleteSignupData = new HashMap<>();
        incompleteSignupData.put("username", "testUser");
        incompleteSignupData.put("password", "testPass");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(incompleteSignupData);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("parameters missing", exception.getMessage());
    }

    @Test
    void signup_WhenAddressNotMapType_ThrowsBadRequestException() {
        Map<String, Object> invalidSignupData = createValidSignupData();
        invalidSignupData.put("address", "Incorrect Type");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(invalidSignupData);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void signup_WhenRoleInvalid_ThrowsBadRequestException() {
        Map<String, Object> invalidRoleData = createValidSignupData();
        invalidRoleData.put("role", "INVALID_ROLE");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(invalidRoleData);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void signup_WhenServiceThrowsException_ThrowsBadRequestException() throws Exception {
        Map<String, Object> signupData = createValidSignupData();

        doThrow(new RuntimeException("Signup failed")).when(userService).signup(anyString(), anyString(), anyString(), any(), any());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(signupData);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Signup failed", exception.getMessage());
    }

    // === Tests for Logout ===

    @Test
    void logout_WhenUserLoggedIn_ReturnsSuccessfulResponse() {
        when(userService.logout()).thenReturn(true);

        Response response = authenticationController.logout();
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("logout successful", response.getMessage());
    }

    @Test
    void logout_WhenNoUserLoggedIn_ThrowsUnauthorizedException() {
        when(userService.logout()).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.logout();
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    // === Tests for Username Validation ===

    @Test
    void validateUsername_WhenInvalidFormat_ThrowsBadRequestException() {
        String invalidUsername = " ";

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.validateUsername(invalidUsername);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid username format", exception.getMessage());
    }

    @Test
    void validateUsername_WhenUsernameAvailable_ReturnsSuccessfulResponse() {
        String validUsername = "availableUser";
        when(userService.usernameExists(validUsername)).thenReturn(false);

        Response response = authenticationController.validateUsername(validUsername);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("username is available", response.getMessage());
    }

    @Test
    void validateUsername_WhenUsernameExists_ThrowsConflictException() {
        String existingUsername = "existingUser";
        when(userService.usernameExists(existingUsername)).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.validateUsername(existingUsername);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("username already exists", exception.getMessage());
    }

    // === Tests for Email Validation ===

    @Test
    void validateEmail_WhenInvalidFormat_ThrowsBadRequestException() {
        String invalidEmail = "invalid-email";

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.validateEmail(invalidEmail);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid email format", exception.getMessage());
    }

    @Test
    void validateEmail_WhenEmailAvailable_ReturnsSuccessfulResponse() {
        String availableEmail = "newuser@example.com";
        when(userService.emailExists(availableEmail)).thenReturn(false);

        Response response = authenticationController.validateEmail(availableEmail);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("email not registered", response.getMessage());
    }

    @Test
    void validateEmail_WhenEmailRegistered_ThrowsConflictException() {
        String registeredEmail = "registered@example.com";
        when(userService.emailExists(registeredEmail)).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.validateEmail(registeredEmail);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email already registered", exception.getMessage());
    }

    // Helper Method to Create Valid Signup Data
    private Map<String, Object> createValidSignupData() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("username", "testUser");
        inputData.put("password", "testPass");
        inputData.put("email", "test@example.com");
        inputData.put("role", "client");

        Map<String, String> address = new HashMap<>();
        address.put("country", "Country");
        address.put("city", "City");
        inputData.put("address", address);

        return inputData;
    }
}
