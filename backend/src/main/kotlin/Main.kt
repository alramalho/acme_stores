import api.GetStoresHandler
import api.ImportHandler
import api.UpdateStoreHandler
import api.usecases.ImportData
import config.Config
import database.Repository
import io.javalin.Javalin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() {
    with(Config) {
        WebApp(
            port = 7000,
            repo = repository,
            importUseCase = importUseCase,
        ).start()

        GlobalScope.launch {
            while(true) {
                importUseCase()
                delay(3_600_000)
            }
        }
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