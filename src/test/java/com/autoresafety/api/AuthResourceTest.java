package com.autoresafety.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.api.Test;

@ExtendWith(ByteBuddyExperimentalExtension.class)
@QuarkusTest
class AuthResourceTest extends QuarkusTestBase {

    @Test
    void loginReturnsJwtForValidCredentials() {
        given()
                .contentType("application/json")
                .body("""
                        {"username":"admin","password":"admin"}
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", equalTo(3600))
                .body("accessToken", not(isEmptyOrNullString()));
    }

    @Test
    void loginRejectsInvalidCredentials() {
        given()
                .contentType("application/json")
                .body("""
                        {"username":"admin","password":"wrong"}
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("Invalid credentials"));
    }
}
