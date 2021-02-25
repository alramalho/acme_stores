package api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import database.Repository
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class GetStoresHandler(private val repo: Repository) : Handler {

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    override fun handle(ctx: Context) {
        try {
            ctx.result(objectMapper.writeValueAsString(repo.getStores()))
                .contentType("application/json")
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            ctx.result(e.message.toString())
        }
    }
}