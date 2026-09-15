# java-swing-gui

A small desktop GUI built with **Java Swing**, using only the standard library —
no Maven, no Gradle, no external dependencies. Just `javac` and `java`.

The point is to learn how a desktop UI is actually assembled: a window, a panel,
some components, a layout, and an event listener that ties a button to code.

## Requirements

- JDK 11 or newer (`javac -version` to check)

## Build and run

```bash
# compile into out/
javac -d out src/*.java

# run
java -cp out Main
```

On a machine with a display this opens a small window. On a headless machine
(a server or container with no display) `Main` prints a message and exits
cleanly instead of crashing with `HeadlessException` — which makes the
compile step easy to verify anywhere.

## Project layout

```
src/        Java source files
out/        compiled .class files (git-ignored)
```

## Notes to self

- Swing components must be created and updated on the **Event Dispatch Thread**.
  `SwingUtilities.invokeLater(...)` is how you get onto it.
- `pack()` sizes the window to fit its contents; `setLocationRelativeTo(null)`
  centres it.
- `setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)` is what makes the X button
  actually stop the program.

## Roadmap

- [x] Day 1 — Scaffold: README, `.gitignore`, `Main.java` with a JFrame that compiles headless
- [ ] Day 2 — Add a `JPanel` with a real layout manager (BorderLayout / GridLayout)
- [ ] Day 3 — Add components: labels, a text field, and buttons
- [ ] Day 4 — Event handling: wire a button's `ActionListener` to update the UI
- [ ] Day 5 — A small feature: greeting generator or simple calculator
- [ ] Day 6 — Polish: input validation, window sizing, README build instructions
