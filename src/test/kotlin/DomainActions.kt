import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Ready

class DomainActions : ZenActions {
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