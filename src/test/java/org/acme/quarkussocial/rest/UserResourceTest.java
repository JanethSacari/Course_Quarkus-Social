package org.acme.quarkussocial.rest;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.json.bind.JsonbBuilder;
import org.acme.quarkussocial.dto.ResponseError;
import org.acme.quarkussocial.dto.UserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @Order(1)
    @DisplayName("Should create an user successfully")
    public void createUserTest(){
        var user = new UserRequest();
        user.setName("Ana");
        user.setAge(15);

        Response response;
        response = given()
                .contentType(ContentType.JSON).body(JsonbBuilder.create().toJson(user)).when()
                .post(apiURL)
                .then()
                .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @Order(2)
    @DisplayName("Should Run an error when Json is not valid")
    public void createUserValidationErrorTest(){
        var user = new UserRequest();
        user.setAge(null);
        user.setName(null);

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(JsonbBuilder.create().toJson(user))
                    .when()
                        .post(apiURL)
                    .then()
                        .extract().response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        List<Map<String, String>> errorsList = response.jsonPath().getList("errors");
        assertNotNull(errorsList.get(0).get("message"));
        assertNotNull(errorsList.get(1).get("message"));
    }

    @Test
    @Order(3)
    @DisplayName("Should list all users")
    public void  listAllUsersTest(){
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(apiURL)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }
}