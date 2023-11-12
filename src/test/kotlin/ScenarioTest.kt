import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

interface ScenarioActor {
    val name: String
}

class TodoListOwner(override val name: String) : ScenarioActor {
    fun `can see list of TODOs`(expected: ToDoList, app: ApplicationForTests) {
        val actual = app.listOfLists(User(name))
        expectThat(actual).contains(expected)
    }

    fun `can see a TODO list`(listName: ListName, expected: ToDoList, app: ApplicationForTests) {
        val actual = app.todoList(User(name) to listName)
        expectThat(actual).isEqualTo(expected)
    }
}

interface ApplicationForTests {
    fun listOfLists(user: User): List<ToDoList>
    fun todoList(listId: Pair<User, ListName>): ToDoList
}

class AppApplicationForTests(
    val forListOfLists: ForListOfLists,
    val forTodoList: ForTodoList,
) : ApplicationForTests {
    override fun listOfLists(user: User): List<ToDoList> = forListOfLists(user)
    override fun todoList(listId: Pair<User, ListName>): ToDoList = forTodoList(listId)
}

abstract class ScenarioTest {

    @AfterEach
    abstract fun tearDown()

    @Test
    fun `can see list of lists`() {
        val me = TodoListOwner("me")
        val todoList = ToDoList(ListName("Home"))
        val application = `application started`(User(me.name) to listOf(todoList))

        me.`can see list of TODOs`(todoList, application)
    }

    @Test
    fun `can see a user's list`() {
        val satan = TodoListOwner("satan")
        val todoList = ToDoList(
            name = ListName("home"),
            items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
        )
        val app = `application started`(User(satan.name) to listOf(todoList))

        satan.`can see a TODO list`(ListName("home"), todoList, app)
    }

    abstract fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>): ApplicationForTests
}


class ApplicationScenarioTest : ScenarioTest() {
    override fun tearDown() {}
    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>): ApplicationForTests {
        val forListOfLists = generateForListOfLists(defaultLists.toMap())
        val forTodoList = generateForTodoList(defaultLists.toMap())

        return AppApplicationForTests(forListOfLists, forTodoList)
    }
}

//class HttpScenarioTest : ScenarioTest() {
//
//    private lateinit var httpServer: Http4kServer
//
//    override fun tearDown() {
//        httpServer.stop()
//    }
//
//    override fun `can see a TODO list`(listId: Pair<User, ListName>): ToDoList =
//        todoListUrl(listId)
//            .let(::createGetRequest)
//            .let(JettyClient())
//            .body.toString()
//            .let(::parseTodoList)
//
//    private fun todoListUrl(listId: Pair<User, ListName>): String {
//        val (user, listName) = listId
//
//        return "http://localhost:${httpServer.port()}/${user.name}/${listName.name}/"
//    }
//
//    private fun parseTodoList(body: String): ToDoList {
//        val h1 = parseH1(body)
//
//        val todos = parseLIs(body)
//            .map { ToDoItem(it) }
//
//        return ToDoList(ListName(h1), todos)
//    }
//
//    override fun `application started`(vararg defaultLists: Pair<User, List<ToDoList>>) {
//        httpServer = createHttpApplication(0, defaultLists.toMap()).start()
//    }
//
//    override fun `can see list of TODOs`(user: User): List<ToDoList> =
//        listOfTodosUrl(user)
//            .let(::createGetRequest)
//            .let(JettyClient())
//            .body.toString()
//            .let(::parseListOfTodoLists)
//
//    private fun createGetRequest(url: String): Request = Request(Method.GET, url)
//
//    private fun listOfTodosUrl(user: User) = "http://localhost:${httpServer.port()}/${user.name}/"
//
//    private fun parseListOfTodoLists(body: String): List<ToDoList> = parseLIs(body)
//        .map { ToDoList(ListName(it)) }
//
//    private fun parseLIs(body: String): List<String> = "<li>([^<>]+)</li>"
//        .toRegex()
//        .findAll(body)
//        .map(::singleValue)
//        .toList()
//
//    private fun parseH1(body: String): String = "<h1>([^<>]+)</h1>"
//        .toRegex()
//        .find(body)
//        ?.let(::singleValue)
//        .orEmpty()
//
//    private fun singleValue(matchResult: MatchResult): String = matchResult.destructured.component1()
//}
