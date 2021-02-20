import io.javalin.Javalin

fun main(args: Array<String>) {
    val app = Javalin.create()
        .start(7000)
        .get("/") {
                ctx -> ctx.result("Hello!")
        }
}