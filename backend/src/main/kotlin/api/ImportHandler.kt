package api

import api.usecases.ImportData
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class ImportHandler(
    private val useCase: ImportData
) : Handler {
    override fun handle(ctx: Context) {
        useCase()

        ctx.status(HttpStatus.CREATED_201)
        ctx.result("Process finished successfully.")
    }
}