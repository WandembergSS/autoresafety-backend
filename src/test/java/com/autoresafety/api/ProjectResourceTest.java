package com.autoresafety.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

@ExtendWith(ByteBuddyExperimentalExtension.class)
@QuarkusTest
class ProjectResourceTest extends QuarkusTestBase {
    @Test
    void projectCrudLifecycle() {
    var location = given()
        .contentType("application/json")
        .body("""
            {"name":"Demo Project","description":"Initial test project"}
            """)
        .when()
        .post("/api/projects")
        .then()
        .statusCode(201)
        .extract()
        .header("Location");

    given()
        .when()
        .get(location)
        .then()
        .statusCode(200)
        .body("name", equalTo("Demo Project"));

    given()
        .when()
        .get("/api/projects")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    given()
        .when()
        .delete(location)
        .then()
        .statusCode(204);
    }

}