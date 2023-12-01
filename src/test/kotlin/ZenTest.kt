import com.ubertob.pesticide.core.*
import org.junit.jupiter.api.DynamicContainer
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class ZenTest : ZenDDT(allActions()) {
    private val satan by NamedActor(::TodoListOwner)
    private val me by NamedActor(::TodoListOwner)

    private val satansTodoList = ToDoList(
        name = ListName("home"),
        items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
    )
    private val myTodoList = ToDoList(ListName("Home"))

    @DDT
    fun `new user have no lists`() = ddtScenario {
        play(me.`can not see any list`())
    }

    @DDT
    fun `only owner can see all their lists`() = ddtScenario {
        setUp {
            satan.`starts with a list`(satansTodoList)
        }.thenPlay(
            me.`can not see any list`(),
            satan.`can see list of TODOs #todos`(satansTodoList.name)
        )
    }

    @DDT
    fun `can see list of lists`() = ddtScenario {
        setUp {
            me.`starts with a list`(myTodoList)
        }.thenPlay(
            me.`can see list of TODOs #todos`(myTodoList.name),
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

    @DDT
    fun `the list owner can add a new item`(): Stream<DynamicContainer> {
        val shopping = ListName("shopping")
        val rolls = ToDoItem("rolls")
        val butter = ToDoItem("butter")
        val ham = ToDoItem("ham")

        return ddtScenario {
            setUp {
                me.`starts with a list`(ToDoList(shopping))
            }.thenPlay(
                me.`can add #item to #listname`(rolls, shopping),
                me.`can add #item to #listname`(butter, shopping),
                me.`can add #item to #listname`(ham, shopping),
                me.`can see #listname with #expected`(shopping, ToDoList(shopping, listOf(rolls, butter, ham)))
            )
        }
    }
}

class TodoListOwner(override val name: String) : DdtActor<ZenActions>() {
    val user = User(name)
    fun `can see list of TODOs #todos`(expected: ListName) = step(expected) {
        val actual = listOfLists(user)
        expectThat(actual).containsExactlyInAnyOrder(expected)
    }

    fun `can see #listname with #expected`(listName: ListName, expected: ToDoList) = step(listName, expected) {
        val actual = todoList(user to listName)
        expectThat(actual).isEqualTo(expected)
    }

    fun `can add #item to #listname`(toDoItem: ToDoItem, listName: ListName) = step(toDoItem, listName) {
        addListItem(user to listName, toDoItem)
    }

    fun `can not see any list`() = step {
        val actual = listOfLists(user)
        expectThat(actual).isEmpty()
    }
}

interface ZenActions : DdtActions<DdtProtocol> {
    fun TodoListOwner.`starts with a list`(toDoList: ToDoList)
    fun listOfLists(user: User): List<ListName>
    fun todoList(listId: Pair<User, ListName>): ToDoList?
    fun addListItem(listId: Pair<User, ListName>, toDoItem: ToDoItem): Unit
}

fun allActions() = setOf(
    DomainActions(),
    HttpActions(),
)

typealias ZenDDT = DomainDrivenTest<ZenActions>
