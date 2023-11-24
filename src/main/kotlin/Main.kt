fun main() {
    val defaultLists = mapOf(
        User("satan") to listOf(
            ToDoList(
                name = ListName("home"),
                items = listOf(ToDoItem("burn house"), ToDoItem("cut tree"))
            )
        )
    )
    createHttpApplication(
        hub = ZenHub(MemoryStorage(defaultLists.toMutableMap()))
    ).start()
}
