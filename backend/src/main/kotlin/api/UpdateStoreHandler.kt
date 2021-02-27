package api

import com.fasterxml.jackson.databind.ObjectMapper
import database.Repository
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class UpdateStoreHandler(private val repo: Repository) : Handler {

    private val objectMapper = ObjectMapper()

    override fun handle(ctx: Context) {
        try {
            repo.updateStoreName(
                ctx.pathParam("id").toLong(),
                objectMapper.readTree(ctx.body()).get("newName").textValue()
            )
        } catch (e: Exception) {
            ctx.result("Could not update Store: ${e.message}")
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }
    }
}