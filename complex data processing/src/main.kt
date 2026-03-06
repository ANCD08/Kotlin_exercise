data class Person(val name: String, val age: Int)

fun main() {
    val people = listOf(
        Person("Alice", 25),
        Person("Bob", 30),
        Person("Charlie", 35),
        Person("Anna", 22),
        Person("Ben", 28)
    )

    val filtered = people.filter { it.name.startsWith("A") || it.name.startsWith("B") }
    val averageAge = filtered.map { it.age }.average()

    println("Average age: ${"%.1f".format(averageAge)}")
}