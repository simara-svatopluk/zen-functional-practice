import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Ready

class DomainActions : ZenActions {
    override val protocol: DdtProtocol = DomainOnly
    override fun addListItem(listId: Pair<User, ListName>, toDoItem: ToDoItem) {
        hub.addItemToList(listId, toDoItem)
    }

    override fun prepare(): DomainSetUp = Ready

    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val hub = ZenHub(MemoryStorage(lists))

    override fun TodoListOwner.`starts with a list`(toDoList: ToDoList) {
        lists[user] = listOf(toDoList)
    }

    override fun listOfLists(user: User): List<ListName> = hub.getLists(user).map { it.name }
    override fun todoList(listId: Pair<User, ListName>): ToDoList? = hub.getList(listId)
}
