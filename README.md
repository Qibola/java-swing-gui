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
src/Main.java       creates and shows the JFrame
src/AppPanel.java   the contents of the window, and their layout
out/                compiled .class files (git-ignored)
```

## Notes to self

- Swing components must be created and updated on the **Event Dispatch Thread**.
  `SwingUtilities.invokeLater(...)` is how you get onto it.
- `pack()` sizes the window to fit its contents; `setLocationRelativeTo(null)`
  centres it.
- `setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)` is what makes the X button
  actually stop the program.
- A `JPanel` defaults to `FlowLayout`, so the layout manager has to be set
  explicitly — usually by passing it to the constructor.
- `BorderLayout` gives five regions (NORTH/SOUTH/EAST/WEST/CENTER), one
  component each. The edges keep their preferred size; CENTER takes the rest,
  so that is the region that grows when the window is resized.
- `GridLayout` makes every cell identical in size. Good for a keypad, bad for
  a form where one label is much longer than another.
- `frame.setContentPane(panel)` swaps out the frame's default panel entirely,
  which hands the whole window over to that panel's layout.

## Roadmap

- [x] Day 1 — Scaffold: README, `.gitignore`, `Main.java` with a JFrame that compiles headless
- [x] Day 2 — Add a `JPanel` with a real layout manager (BorderLayout / GridLayout)
- [ ] Day 3 — Add components: labels, a text field, and buttons
- [ ] Day 4 — Event handling: wire a button's `ActionListener` to update the UI
- [ ] Day 5 — A small feature: greeting generator or simple calculator
- [ ] Day 6 — Polish: input validation, window sizing, README build instructions
