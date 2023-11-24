import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request

class HttpActions(val env: String = "local") : ZenActions {
    override val protocol: DdtProtocol = Http(env)

    private val port = 8081
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val zenHub = ZenHub(lists)

    private val server = createHttpApplication(port = port, hub = zenHub)
    private val client = JettyClient()

    override fun TodoListOwner.`starts with a list`(toDoList: ToDoList) {
        lists[user] = listOf(toDoList)
    }

    override fun prepare(): DomainSetUp = Ready.also {
        server.start()
    }

    override fun tearDown(): HttpActions = also { server.stop() }

    override fun listOfLists(user: User): List<ToDoList> = listOfTodosPath(user)
        .let(::createGetRequest)
        .let(client)
        .body.toString()
        .let(::parseListOfTodoLists)

    override fun todoList(listId: Pair<User, ListName>) = todoListPath(listId)
        .let(::createGetRequest)
        .let(client)
        .body.toString()
        .let(::parseTodoList)

    private fun todoListPath(listId: Pair<User, ListName>): String {
        val (user, listName) = listId
        return "${user.name}/${listName.name}/"
    }

    private fun parseTodoList(body: String): ToDoList {
        val listName = parseH1(body).let(::ListName)

        val todos = parseLIs(body)
            .map { ToDoItem(it) }

        return ToDoList(listName, todos)
    }

    private fun createGetRequest(path: String): Request = Request(Method.GET, "http://localhost:$port/$path")

    private fun listOfTodosPath(user: User) = "${user.name}/"

    private fun parseListOfTodoLists(body: String): List<ToDoList> = parseLIs(body)
        .map { ToDoList(ListName(it)) }

    private fun parseLIs(body: String): List<String> = "<li>([^<>]+)</li>"
        .toRegex()
        .findAll(body)
        .map(::singleValue)
        .toList()

    private fun parseH1(body: String): String = "<h1>([^<>]+)</h1>"
        .toRegex()
        .find(body)
        ?.let(::singleValue)
        .orEmpty()

    private fun singleValue(matchResult: MatchResult): String = matchResult.destructured.component1()
}
