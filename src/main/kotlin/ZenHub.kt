class ZenHub(
    private val storage: Storage
) {
    fun getLists(user: User): List<ToDoList> = generateForListOfLists(storage)(user)
    fun getList(listId: Pair<User, ListName>): ToDoList = generateForTodoList(storage)(listId)
    fun addItemToList(listId: Pair<User, ListName>, toDoItem: ToDoItem) =
        generateForAddingItem(storage)(listId, toDoItem)
}

data class MemoryStorage(
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
) : Storage {
    override fun addItemToList(listId: Pair<User, ListName>, toDoItem: ToDoItem) {
        val (user, listName) = listId
        val list = this(user).single { it.name == listName }
        val new = list.copy(items = list.items + toDoItem)
        lists[user] = lists.getValue(user).minus(list).plus(new)
    }

    override fun invoke(user: User): List<ToDoList> = lists[user] ?: emptyList()
}
