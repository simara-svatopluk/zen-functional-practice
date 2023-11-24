class ZenHub(
    val lists: Map<User, List<ToDoList>> = emptyMap()
) {
    fun listOfLists(user: User): List<ToDoList> = generateForListOfLists(lists)(user)
    fun todoList(listId: Pair<User, ListName>): ToDoList = generateForTodoList(lists)(listId)
}
