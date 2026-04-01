package ru.squidory.trainingdairymobile;

import org.junit.Test;
import static org.junit.Assert.*;

import ru.squidory.trainingdairymobile.data.model.AuthRequest;
import ru.squidory.trainingdairymobile.data.model.AuthResponse;

/**
 * Unit tests for Auth functionality.
 */
public class AuthUnitTest {

    @Test
    public void authRequest_constructor_withAllParameters() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        String birthDate = "1995-05-15";
        String gender = "MALE";

        // When
        AuthRequest request = new AuthRequest(email, password, name, birthDate, gender);

        // Then
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(name, request.getName());
        assertEquals(birthDate, request.getBirthDate());
        assertEquals(gender, request.getGender());
    }

    @Test
    public void authRequest_constructor_withEmailAndPasswordOnly() {
        // Given
        String email = "test@example.com";
        String password = "password123";

        // When
        AuthRequest request = new AuthRequest(email, password);

        // Then
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertNull(request.getName());
        assertNull(request.getBirthDate());
        assertNull(request.getGender());
    }

    @Test
    public void authResponse_settersAndGetters_workCorrectly() {
        // Given
        AuthResponse response = new AuthResponse();
        long userId = 1L;
        String email = "test@example.com";
        String accessToken = "access_token_123";
        String refreshToken = "refresh_token_456";

        // When
        response.setUserId(userId);
        response.setEmail(email);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
