data class ToDoList(val name: ListName, val items: List<ToDoItem> = emptyList())

data class ToDoItem(val description: String)

data class ListName(val name: String)

data class User(val name: String)
enum class TodoStatus {
    Todo,
    InProgress,
    Done,
    Blocked,
}

typealias ForListOfLists = (User) -> List<ToDoList>
typealias ForTodoList = (Pair<User, ListName>) -> ToDoList

fun generateForListOfLists(lists: Map<User, List<ToDoList>> = emptyMap()): ForListOfLists = { user ->
    lists.getValue(user)
}

fun generateForTodoList(lists: Map<User, List<ToDoList>> = emptyMap()): ForTodoList = { (user, listName) ->
    lists.getValue(user).single { it.name == listName }
}

class ZenHub(
    val lists: Map<User, List<ToDoList>> = emptyMap()
) {
    fun listOfLists(user: User): List<ToDoList> = generateForListOfLists(lists)(user)
    fun todoList(listId: Pair<User, ListName>): ToDoList = generateForTodoList(lists)(listId)
}
