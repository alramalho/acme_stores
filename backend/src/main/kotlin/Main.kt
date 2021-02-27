import adapters.StoresGateway
import api.GetStoresHandler
import api.ImportHandler
import api.UpdateStoreHandler
import config.Config
import database.Repository
import io.javalin.Javalin

fun main() {
    with(Config) {
        WebApp(
            port = 7000,
            repo = repository,
            storesGateway = storesGateway
        ).start()
    }
}

class WebApp(
    private val port: Int,
    repo: Repository,
    storesGateway: StoresGateway
) {
    private val app = Javalin.create { it.enableCorsForAllOrigins() }
        .get("/") { ctx ->
            ctx.result("Hello!")
        }
        .get("/import", ImportHandler(storesGateway, repo))
        .get("/stores", GetStoresHandler(repo))
        .put("/update_store_name/:id", UpdateStoreHandler(repo))

    fun start() {
        app.start(port)
    }
}