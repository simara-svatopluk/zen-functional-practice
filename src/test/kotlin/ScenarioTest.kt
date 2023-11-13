import org.http4k.client.JettyClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

interface ScenarioActor {
    val name: String
}

interface Actions {
    fun listOfLists(user: User): List<ToDoList>
    fun todoList(listId: Pair<User, ListName>): ToDoList?
}

typealias Step = Actions.() -> Unit

class TodoListOwner(override val name: String) : ScenarioActor {
    fun `can see list of TODOs`(expected: ToDoList): Step = {
        val actual = listOfLists(User(name))
        expectThat(actual).contains(expected)
    }

    fun `can see a TODO list`(listName: ListName, expected: ToDoList): Step = {
        val actual = todoList(User(name) to listName)
        expectThat(actual).isEqualTo(expected)
    }
}

interface ApplicationForTests : Actions {
    val server: AutoCloseable

    fun runScenario(vararg steps: Step) {
        server.use {
            steps.onEach { step -> this.step() }
        }
    }
}

abstract class ScenarioTest {
    @Test
    fun `can see list of lists`() {
        val me = TodoListOwner("me")
        val todoList = ToDoList(ListName("Home"))
        val app = `application started`(User(me.name) to listOf(todoList))
        app.runScenario(
            me.`can see list of TODOs`(todoList),
        )
    }

    @Test
    fun `can see a user's list`() {
        val satan = TodoListOwner("satan")
        val todoList = ToDoList(
            name = ListName("home"),
            items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
        )
        val app = `application started`(User(satan.name) to listOf(todoList))

        app.runScenario(
            satan.`can see a TODO list`(ListName("home"), todoList),
        )
    }

    abstract fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>): ApplicationForTests
}

class AppApplicationForTests(
    val forListOfLists: ForListOfLists,
    val forTodoList: ForTodoList,
) : ApplicationForTests {
    override val server = AutoCloseable { }

    override fun listOfLists(user: User): List<ToDoList> = forListOfLists(user)
    override fun todoList(listId: Pair<User, ListName>): ToDoList = forTodoList(listId)
}

class ApplicationScenarioTest : ScenarioTest() {
    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>): ApplicationForTests {
        val forListOfLists = generateForListOfLists(defaultLists.toMap())
        val forTodoList = generateForTodoList(defaultLists.toMap())

        return AppApplicationForTests(forListOfLists, forTodoList)
    }
}

class HttpApplicationForTests(
    private val client: HttpHandler,
    override val server: AutoCloseable,
) : ApplicationForTests {
    override fun listOfLists(user: User): List<ToDoList> = listOfTodosUrl(user)
        .let(::createGetRequest)
        .let(client)
        .body.toString()
        .let(::parseListOfTodoLists)

    override fun todoList(listId: Pair<User, ListName>) = todoListUrl(listId)
        .let(::createGetRequest)
        .let(client)
        .body.toString()
        .let(::parseTodoList)

    private fun todoListUrl(listId: Pair<User, ListName>): String {
        val (user, listName) = listId
        return "/${user.name}/${listName.name}/"
    }

    private fun parseTodoList(body: String): ToDoList {
        val h1 = parseH1(body)

        val todos = parseLIs(body)
            .map { ToDoItem(it) }

        return ToDoList(ListName(h1), todos)
    }

    private fun createGetRequest(url: String): Request = Request(Method.GET, url)

    private fun listOfTodosUrl(user: User) = "/${user.name}/"

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

class HttpScenarioTest : ScenarioTest() {
    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>): ApplicationForTests {
        val httpServer = createHttpApplication(
            port = 8081,
            defaultLists = defaultLists.toMap()
        ).start()

        val client = ClientFilters
            .SetBaseUriFrom(Uri.of("http://localhost:${httpServer.port()}/"))
            .then(JettyClient())

        return HttpApplicationForTests(client, httpServer)
    }
}
