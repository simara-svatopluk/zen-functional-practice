import com.ubertob.pesticide.core.*
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

class ZenTest : ZenDDT(allActions()) {
    private val satan by NamedActor(::TodoListOwner)
    private val me by NamedActor(::TodoListOwner)

    private val satansTodoList = ToDoList(
        name = ListName("home"),
        items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
    )
    private val myTodoList = ToDoList(ListName("Home"))

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

interface ZenActions : DdtActions<DdtProtocol> {
    fun TodoListOwner.`starts with a list`(toDoList: ToDoList)
    fun listOfLists(user: User): List<ToDoList>
    fun todoList(listId: Pair<User, ListName>): ToDoList?
}

fun allActions() = setOf(
    DomainActions(),
    HttpActions(),
)

typealias ZenDDT = DomainDrivenTest<ZenActions>
