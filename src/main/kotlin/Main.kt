import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

data class ZenFunctionalHttp(
    val forListOfLists: ForListOfLists,
    val forTodoList: ForTodoList
) : HttpHandler {
    private val routes: HttpHandler = routes(
        "/{user}/" bind Method.GET to { request ->
            request
                .let(::parseUser)
                .let(forListOfLists)
                .let(::renderListOfLists)
                .let(::createResponse)
        },
        "{user}/{list}" bind Method.GET to { request ->
            request
                .let(::parseListId)
                .let(forTodoList)
                .let(::renderTodoList)
                .let(::createResponse)
        }
    )

    private fun parseUser(request: Request) = User(request.path("user").orEmpty())

    private fun parseListId(request: Request): Pair<User, ListName> = parseUser(request) to parseListName(request)

    private fun parseListName(request: Request) = ListName(request.path("list").orEmpty())

    private fun createResponse(html: String) = Response(Status.OK).body(html)

    private fun renderTodoList(toDoList: ToDoList): String {
        val items = toDoList.items.map { "<li>${it.description}</li>\n" }

        return """
            <h1>${toDoList.name.name}</h1>
            <ul>
                $items
            <ul>
        """
    }

    private fun renderListOfLists(listOfLists: List<ToDoList>): String {
        val items = listOfLists.map { "<li>${it.name.name}</li>\n" }

        return """
            <h1>List of TODOs</h1>
            <ul>
                $items
            <ul>
        """
    }

    override fun invoke(request: Request) = routes(request)
}

fun createHttpApplication(port: Int = 8080, defaultLists: Map<User, List<ToDoList>> = emptyMap()): Http4kServer {
    return ZenFunctionalHttp(
        generateForListOfLists(defaultLists),
        generateForTodoList(defaultLists),
    )
        .asServer(Jetty(port))
}

fun main() {
    createHttpApplication().start()
}
