import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.expect
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.vertx.mutiny.pgclient.PgPool
import org.hamcrest.CoreMatchers.equalTo
import org.jboss.resteasy.reactive.RestResponse.StatusCode
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoServiceTest {

    @Inject
    lateinit var pgPool: PgPool

    @BeforeAll
    fun reset() {
        pgPool.query("DELETE FROM bar").executeAndForget()
    }

    @Test
    fun releases() {
        expect().body(equalTo("v1.6.21")).`when`().get("/")
    }

    @Test
    fun addBar() {
        given()
            .contentType(ContentType.JSON)
            .body(Bar(null, "asdf"))
            .`when`().post("/bars")
            .then().statusCode(StatusCode.NO_CONTENT)
    }

    @Test
    fun getBars() {
        expect().body("size()", equalTo(1)).`when`().get("/bars")
    }

}