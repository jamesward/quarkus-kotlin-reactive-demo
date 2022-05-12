import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

data class Release(val name: String)

data class Bar(val id: Long?, val name: String)

@RegisterRestClient(baseUri = "https://api.github.com/repos/jetbrains/kotlin/tags")
interface ReleasesService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun get(): List<Release>
}

@Path("/")
class DemoService(@RestClient val releasesService: ReleasesService, @Inject val pgPool: PgPool) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun releases() = run {
        releasesService.get().filterNot { it.name.contains('-') }.firstOrNull()?.name ?: "not found"
    }

    @GET
    @Path("/bars")
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun getBars() = run {
        pgPool.query("SELECT * FROM bar").execute().onItem().transform { rowSet -> rowSet.map { Bar(it.getLong("id"), it.getString("name")) } }.awaitSuspending()
    }

    @POST
    @Path("/bars")
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun addBar(bar: Bar) = run {
        println(bar)
        val rowset = pgPool.preparedQuery("INSERT INTO bar (name) VALUES ($1)").execute(Tuple.of(bar.name)).awaitSuspending()
        if (rowset.rowCount() != 1) {
            throw Exception("Could not save bar")
        }
    }

    @PostConstruct
    fun initdb() {
        pgPool.query("CREATE TABLE IF NOT EXISTS bar (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)").executeAndForget()
    }
}