class User {
  final String name;
  final String? email; // nullable String

  User(this.name, this.email);
}

void main() {
  final users = [
    User("Alex", "alex@example.com"),
    User("Blake", null),
    User("Casey", "casey@work.com"),
  ];

  // Requirements 1 & 2: Print emails in uppercase or "has no email"
  for (var user in users) {
    if (user.email != null) {
      print(user.email!.toUpperCase());
    } else {
      print("${user.name} has no email");
    }
  }

  // Requirement 3: Count and print users with valid emails
  final emailCount = users.where((u) => u.email != null).length;
  print("Total users with valid emails: $emailCount");
}