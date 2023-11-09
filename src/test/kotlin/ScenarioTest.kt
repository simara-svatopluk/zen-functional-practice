import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.server.Http4kServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

abstract class ScenarioTest {

    @AfterEach
    abstract fun tearDown()

    @Test
    fun `can see list of lists`() {
        `application started`(User("me") to listOf(ToDoList(ListName("Home"))))
        val list = `can see list of TODOs`(User("me"))
        expectThat(list).contains(ToDoList(ListName("Home")))
    }

    @Test
    fun `can see a user's list`() {
        val todoList = ToDoList(
            name = ListName("home"),
            items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
        )
        `application started`(User("satan") to listOf(todoList))

        val list = `can see a TODO list`(User("satan") to ListName("home"))
        expectThat(list).isEqualTo(todoList)
    }

    abstract fun `can see a TODO list`(listId: Pair<User, ListName>): ToDoList

    abstract fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>)

    abstract fun `can see list of TODOs`(user: User): List<ToDoList>
}

class HttpScenarioTest : ScenarioTest() {

    private lateinit var httpServer: Http4kServer

    override fun tearDown() {
        httpServer.stop()
    }

    override fun `can see a TODO list`(listId: Pair<User, ListName>): ToDoList =
        todoListUrl(listId)
            .let(::createGetRequest)
            .let(JettyClient())
            .body.toString()
            .let(::parseTodoList)

    private fun todoListUrl(listId: Pair<User, ListName>): String {
        val (user, listName) = listId

        return "http://localhost:${httpServer.port()}/${user.name}/${listName.name}/"
    }

    private fun parseTodoList(body: String): ToDoList {
        val h1 = parseH1(body)

        val todos = parseLIs(body)
            .map { ToDoItem(it) }

        return ToDoList(ListName(h1), todos)
    }

    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>) {
        httpServer = createHttpApplication(0, defaultLists.toMap()).start()
    }

    override fun `can see list of TODOs`(user: User): List<ToDoList> =
        listOfTodosUrl(user)
            .let(::createGetRequest)
            .let(JettyClient())
            .body.toString()
            .let(::parseListOfTodoLists)

    private fun createGetRequest(url: String): Request = Request(Method.GET, url)

    private fun listOfTodosUrl(user: User) = "http://localhost:${httpServer.port()}/${user.name}/"

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

class ApplicationScenarioTest : ScenarioTest() {
    lateinit var forListOfLists: ForListOfLists
    lateinit var forTodoList: ForTodoList

    override fun tearDown() {}
    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>) {
        forListOfLists = generateForListOfLists(defaultLists.toMap())
        forTodoList = generateForTodoList(defaultLists.toMap())
    }

    override fun `can see a TODO list`(listId: Pair<User, ListName>): ToDoList = forTodoList(listId)

    override fun `can see list of TODOs`(user: User): List<ToDoList> = forListOfLists(user)
}
