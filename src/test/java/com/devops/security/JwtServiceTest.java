package com.devops.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-for-unit-tests-only-0123456789abcdef", 60, new InMemoryConsumedTokenStore());
    }

    @Test
    void issueToken_returnsNonBlankJwt() {
        String token = jwtService.issueToken(45);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void issueToken_usesDefaultTtlWhenNonPositiveGiven() {
        String token = jwtService.issueToken(0);

        assertThat(token).isNotBlank();
    }

    @Test
    void validateAndConsume_acceptsFreshlyIssuedToken() {
        String token = jwtService.issueToken(45);

        assertThatThrownBy(() -> {
            jwtService.validateAndConsume(token);
            jwtService.validateAndConsume(token);
        }).isInstanceOf(JwtService.JwtValidationException.class)
          .hasMessageContaining("already used");
    }

    @Test
    void validateAndConsume_rejectsNullToken() {
        assertThatThrownBy(() -> jwtService.validateAndConsume(null))
                .isInstanceOf(JwtService.JwtValidationException.class)
                .hasMessageContaining("Missing JWT");
    }

    @Test
    void validateAndConsume_rejectsBlankToken() {
        assertThatThrownBy(() -> jwtService.validateAndConsume("   "))
                .isInstanceOf(JwtService.JwtValidationException.class);
    }

    @Test
    void validateAndConsume_rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtService.validateAndConsume("not-a-jwt"))
                .isInstanceOf(JwtService.JwtValidationException.class)
                .hasMessageContaining("Invalid JWT");
    }

    @Test
    void validateAndConsume_rejectsTokenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService(
                "a-completely-different-secret-key-9876543210zz", 60, new InMemoryConsumedTokenStore());
        String token = otherService.issueToken(45);

        assertThatThrownBy(() -> jwtService.validateAndConsume(token))
                .isInstanceOf(JwtService.JwtValidationException.class);
    }

    @Test
    void validateAndConsume_rejectsExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(
                "test-secret-key-for-unit-tests-only-0123456789abcdef", 60, new InMemoryConsumedTokenStore());
        String token = shortLived.issueToken(1);

        Thread.sleep(1500);

        assertThatThrownBy(() -> shortLived.validateAndConsume(token))
                .isInstanceOf(JwtService.JwtValidationException.class);
    }

    @Test
    void validateAndConsume_allowsTwoDifferentTokensSequentially() {
        String token1 = jwtService.issueToken(45);
        String token2 = jwtService.issueToken(45);

        jwtService.validateAndConsume(token1);
        jwtService.validateAndConsume(token2);
    }

    @Test
    void validateAndConsume_rejectsReuseAcrossDifferentServiceInstancesSharingAStore() {
        ConsumedTokenStore sharedStore = new InMemoryConsumedTokenStore();
        JwtService instanceA = new JwtService(
                "shared-secret-for-multi-instance-test-0123456789", 60, sharedStore);
        JwtService instanceB = new JwtService(
                "shared-secret-for-multi-instance-test-0123456789", 60, sharedStore);

        String token = instanceA.issueToken(45);
        instanceA.validateAndConsume(token);

        assertThatThrownBy(() -> instanceB.validateAndConsume(token))
                .isInstanceOf(JwtService.JwtValidationException.class)
                .hasMessageContaining("already used");
    }
}
