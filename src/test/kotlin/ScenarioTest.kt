import com.ubertob.pesticide.core.*
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

interface ZenActions : DdtActions<DdtProtocol> {
    fun TodoListOwner.`starts with a list`(toDoList: ToDoList)

    fun listOfLists(user: User): List<ToDoList>
    fun todoList(listId: Pair<User, ListName>): ToDoList?
}

typealias ZenDDT = DomainDrivenTest<ZenActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions(),
)

class DomainOnlyActions : ZenActions {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare(): DomainSetUp = Ready

    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val hub = ZenHub(lists)

    override fun TodoListOwner.`starts with a list`(toDoList: ToDoList) {
        lists[user] = listOf(toDoList)
    }

    override fun listOfLists(user: User): List<ToDoList> = hub.listOfLists(user)
    override fun todoList(listId: Pair<User, ListName>): ToDoList? = hub.todoList(listId)
}

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

class TodoListOwner(override val name: String) : DdtActor<ZenActions>() {
    val user = User(name)

    fun `can see list of TODOs #todos`(expected: ToDoList) = step(expected) {
        val actual = listOfLists(User(name))
        expectThat(actual).containsExactlyInAnyOrder(expected)
    }

    fun `can see #listname with #expected`(listName: ListName, expected: ToDoList) = step(listName, expected) {
        val actual = todoList(User(name) to listName)
        expectThat(actual).isEqualTo(expected)
    }
}

class ScenarioTest : ZenDDT(allActions()) {
    val satan by NamedActor(::TodoListOwner)
    val me by NamedActor(::TodoListOwner)

    val satansTodoList = ToDoList(
        name = ListName("home"),
        items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
    )
    val myTodoList = ToDoList(ListName("Home"))

    @DDT
    fun `can see list of lists`() = ddtScenario {
        setUp {
            me.`starts with a list`(myTodoList)
        }.thenPlay(
            me.`can see list of TODOs #todos`(myTodoList),
        )
    }

    @DDT
    fun `can see a user's list`() = ddtScenario {
        setUp {
            satan.`starts with a list`(satansTodoList)
        }.thenPlay(
            satan.`can see #listname with #expected`(ListName("home"), satansTodoList)
        )
    }
}
