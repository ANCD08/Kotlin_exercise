class Person {
  final String name;
  final int age;

  Person(this.name, this.age);
}

void main() {
  final people = [
    Person("Alice", 25),
    Person("Bob", 30),
    Person("Charlie", 35),
    Person("Anna", 22),
    Person("Ben", 28),
  ];

  // Filter people whose names start with 'A' or 'B'
  final filtered = people
      .where((p) => p.name.startsWith('A') || p.name.startsWith('B'))
      .toList();

  // Calculate average age
  final totalAge = filtered.fold(0, (sum, p) => sum + p.age);
  final average = totalAge / filtered.length;

  // Print rounded to 1 decimal place
  print(average.toStringAsFixed(1));
}