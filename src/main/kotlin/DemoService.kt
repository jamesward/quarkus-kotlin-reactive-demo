import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

data class Release(val name: String)

@RegisterRestClient(baseUri = "https://api.github.com/repos/jetbrains/kotlin/tags")
interface ReleasesService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun get(): List<Release>
}

@Path("/")
class DemoService(@RestClient val releasesService: ReleasesService) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun releases() = run {
        releasesService.get().filterNot { it.name.contains('-') }.firstOrNull()?.name ?: "not found"
    }
}