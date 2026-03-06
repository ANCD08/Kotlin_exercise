void main() {
  final words = ["apple", "cat", "banana", "dog", "elephant"];

  // Create a map where keys are strings and values are their lengths
  final wordMap = {for (var word in words) word: word.length};

  // Filter entries where length > 4, then print each
  wordMap.entries
      .where((entry) => entry.value > 4)
      .forEach((entry) => print("${entry.key} has length ${entry.value}"));
}