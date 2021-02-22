import adapters.StoresGateway
import api.ImportHandler
import config.Config
import database.Repository
import io.javalin.Javalin

fun main(args: Array<String>) {
    with(Config) {
        WebApp(
            port = 7000,
            repo = repository,
            storesGateway=  storesGateway
        ).start()
    }
}

class WebApp(
    private val port: Int,
    repo: Repository,
    storesGateway: StoresGateway
) {
    private val app = Javalin.create()
        .get("/") { ctx ->
            ctx.result("Hello!")
        }
//        .get("/import", ImportHandler(storesGateway, repo))

    fun start() {
        app.start(port)
    }
}