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
typealias ForAddingItem = (Pair<User, ListName>, ToDoItem) -> Unit

fun generateForListOfLists(storage: Storage): ForListOfLists = { user ->
    storage(user)
}

fun generateForTodoList(storage: Storage): ForTodoList = { (user, listName) ->
    storage(user).single { it.name == listName }
}

fun generateForAddingItem(storage: Storage): ForAddingItem = { listId, todoItem ->
    storage.addItemToList(listId, todoItem)
}

interface Storage : ForListOfLists {
    fun addItemToList(listId: Pair<User, ListName>, toDoItem: ToDoItem)
}
