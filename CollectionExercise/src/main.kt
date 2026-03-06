fun main() {
    val words = listOf("apple", "cat", "banana", "dog", "elephant")

    val wordLengths = words.associateWith { it.length }

    wordLengths
        .filter { (_, length) -> length > 4 }
        .forEach { (word, length) -> println("$word has length $length") }
}