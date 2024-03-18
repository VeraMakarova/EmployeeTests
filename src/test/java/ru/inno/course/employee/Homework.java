package ru.inno.course.employee;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
public class Homework {
    public static final String URL = "https://x-clients-be.onrender.com/company";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";
    public static final String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee";
    private static String token;
    private static final int testCompanyId = 1541;

    public static String getToken() {
        String creds = """
                {
                   "username": "flora",
                   "password": "nature-fairy"
                 }""";
        String token = given()
                .log().all()
                .body(creds)
                .contentType(ContentType.JSON)
                .when().post(URL_AUTH)
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("userToken");
        return token;
    }

    public static int getCompanyId() {
        token = getToken();
        String requestBody = """
                {
                  "name": "Dark Side",
                  "description": "Cookies and cupcakes"
                }""";
        int myCompanyId = given()
                .body(requestBody)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL)
                .then()
                .extract().path("id");
        return myCompanyId;
    }

    @Test
    @DisplayName("1. Получение списка всех компаний")
    public void shouldReturnListOfCompanies (){
        given()
                .log().all()
                .get(URL)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(notNullValue()))
                .log().all();
    }
    @Test
    @DisplayName("2. Создание компании")
    public void iCanCreateCompany(){
        token = getToken();
        String requestBody = """
                {
                  "name": "Dark Side",
                  "description": "Cookies and cupcakes"
                }""";
        int myCompanyId = given()
                .log().all()
                .body(requestBody)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL)
                .then()
                .log().all()
                .statusCode(201).body("id", greaterThan(1) )
                .extract().path("id");

        given().log().all()
                .get(URL+"/"+myCompanyId)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Dark Side"));
    }

    @Test
    @DisplayName("3. Получение списка сотрудников новой компании ")
    public void getEmployeeList(){
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBody2 = """
                {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";

        given().log().all()
                .body(requestBody2)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(1));
        //
        given().log().all()
                .get("https://x-clients-be.onrender.com/employee?company="+ myCompanyId)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("4. Создание нового сотрудника в новую компанию")
    public void iCanCreateEmployeeInNewCompany(){
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBody2 = """
                {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";

        given().log().all()
                .body(requestBody2)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(1));
    }

    @Test
    @DisplayName("5. Невозможно создать сотрудника с некорректной почтой")
    public void iCannotCreateEmployeeWithIncorrectEmail(){
        token = getToken();
        int myCompanyId = getCompanyId();
        String requestBody2 = """
                {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "1234567",
                    "phone": "1111"
                  }""";
        given().log().all()
                .body(requestBody2)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("6. Невозможно создать сотрудника в несуществующую компанию")
    public void iCannotCreateEmployeeInNotExistingCompany(){
        token = getToken();
        String requestBody = """
                {
                    "firstName": "Jack2",
                    "lastName": "Smith",
                    "companyId": 15410000000,
                    "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        given().log().all()
                .body(requestBody)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(500);
    }
    @Test
    @DisplayName("7. При создании сотрудника правильно сохраняются данные")
    public void employeeDataIsSavedCorrectly(){
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBody2 = """
                {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";

        int userId = given().log().all()
                .body(requestBody2)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Jack"))
                .body("lastName", equalTo("Smith"))
                .body("companyId", equalTo(myCompanyId))
                .body("email", equalTo("casebat359@dovesilo.com"))
                .body("phone", equalTo("1111"));
    }

    @Test
    @DisplayName("8. При редактировании фамилии сотрудника правильно сохраняются данные")
    public void employeeLastNameIsEditedCorrectly() {
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");
        String requestBodyPath = """
                {
                    "lastName": "Adams"
                  }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE+"/"+userId)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Adams"));
    }

    @Test
    @DisplayName("9. При редактировании почты сотрудника правильно сохраняются данные")
    public void employeeEmailIsEditedCorrectly() {
        token = getToken();
        int myCompanyId = getCompanyId();
        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        String requestBodyPath = """
                {
                     "email": "123123@dovesilo.com"
                 }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE+"/"+userId)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("email", equalTo("123123@dovesilo.com"));
    }

    @Test
    @DisplayName("10. При редактировании почты сотрудника нельзя сохранить некорректную почту")
    public void employeeIncorrectEmailCanNotBeSaved() {
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");
        String requestBodyPath = """
                {
                     "email": "777"
                 }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE + "/" + userId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("11. При редактировании телефона сотрудника правильно сохраняются данные")
    public void employeePhoneIsEditedCorrectly() {
        token = getToken();
        int myCompanyId = getCompanyId();
        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");
        String requestBodyPath = """
                {
                     "phone": "9999999"
                 }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE+"/"+userId)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("phone", equalTo("9999999"));
    }

    @Test
    @DisplayName("12. При редактировании URL аватара сотрудника правильно сохраняются данные")
    public void employeeAvatarURLIsEditedCorrectly() {
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");
        String requestBodyPath = """
                {
                     "url": "https://static.1000.menu/img/content-v2/97/7b/36007/prostoi-i-vkusnyi-shokoladnyi-tort_1616052156_9_max.jpg"
                 }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE+"/"+userId)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("avatar_url", equalTo("https://static.1000.menu/img/content-v2/97/7b/36007/prostoi-i-vkusnyi-shokoladnyi-tort_1616052156_9_max.jpg"));
    }

    @Test
    @DisplayName("13. При редактировании isActive сотрудника правильно сохраняются данные")
    public void employeeIsActiveIsEditedCorrectly() {
        token = getToken();
        int myCompanyId = getCompanyId();

        String requestBodyPost = """
                 {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + myCompanyId + """ 
                    , "email": "casebat359@dovesilo.com",
                    "phone": "1111"
                  }""";
        int userId = given().log().all()
                .body(requestBodyPost)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .extract().path("id");
        String requestBodyPath = """
                {
                     "isActive": false
                 }""";
        given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", userId)
                .when().patch(URL_EMPLOYEE+"/"+userId)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE+"/"+userId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));
    }

}
