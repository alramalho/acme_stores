package api

import adapters.StoresGateway
import api.usecases.ImportData
import database.Repository
import entities.Season
import entities.Store
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class ImportHandler(
    private val useCase: ImportData
) : Handler {
    override fun handle(ctx: Context) {
        try {
            useCase.invoke()

            ctx.status(HttpStatus.CREATED_201)
            ctx.result("Process finished successfully.")
//            ctx.result("Process finished successfully. \nImported:\n${storesToImport.size} Stores\n${seasonsToImport.size} Seasons\n${storeSeasonsToImport.size} Season-Store relationships\nUpdated:\n${storesToUpdate.size} Stores\n")
        } catch (e: Exception) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE_503)
            ctx.result(e.message.toString())
            return
        }
    }
}