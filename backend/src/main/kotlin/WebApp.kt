import adapters.StoresGateway
import api.GetStoresHandler
import api.ImportHandler
import api.UpdateStoreHandler
import api.usecases.ImportData
import config.Config
import database.Repository
import io.javalin.Javalin

fun main() {
    with(Config) {
        WebApp(
            port = 7000,
            repo = repository,
            importUseCase = importUseCase,
        ).start()
    }
}

class WebApp(
    private val port: Int,
    repo: Repository,
    importUseCase: ImportData
) {
    private val app = Javalin.create { it.enableCorsForAllOrigins() }
        .get("/") { ctx ->
            ctx.result("Hello!")
        }
        .get("/import", ImportHandler(importUseCase))
        .get("/stores", GetStoresHandler(repo))
        .put("/update_store_name/:id", UpdateStoreHandler(repo))

    fun start() {
        app.start(port)
    }
}