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

On a machine with a display this opens a small window with a name field, a
greeting-style drop-down, a Greet button and a Clear button. Type a name and press Greet (or just hit
Enter in the field) and the greeting appears; Clear puts everything back.
On a headless machine
(a server or container with no display) `Main` prints a message and exits
cleanly instead of crashing with `HeadlessException` — and it now clicks the
buttons via `doClick()` and asserts on the results, so the behaviour is
verified too, not just the compile.

If a machine has the JRE but no `javac` binary, the compiler is often still
in the runtime image and can be called directly:

```bash
java -m jdk.compiler/com.sun.tools.javac.Main -d out src/*.java
```

## Project layout

```
src/Main.java       creates and shows the JFrame
src/AppPanel.java   the contents of the window, and their layout
src/Greetings.java  the greeting wording - plain Java, no Swing
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
- A `JTextField(16)` sizes itself by *columns*, not pixels, so the field stays
  sensible whatever font the platform uses.
- `label.setLabelFor(field)` is worth the one line: it pairs the two for screen
  readers and makes the label's mnemonic (Alt+N) jump focus into the field.
- `FlowLayout` is the right layout for a button bar — it leaves buttons at their
  preferred size instead of stretching them the way BorderLayout would.
- More than five things to place? Nest panels. Each BorderLayout region can hold
  a panel that runs its own layout manager inside it.
- `frame.getRootPane().setDefaultButton(button)` makes Enter trigger that button
  from anywhere in the window.
- Keeping components as fields (not locals) is what lets the next step attach
  listeners to them without digging through the container hierarchy.
- `ActionListener` has exactly one method, so a lambda or a method reference
  (`this::onGreet`) *is* an implementation — no anonymous class needed.
- Swing calls listeners **on the EDT**, so a handler can update components
  freely. The catch: the EDT also repaints the window, so slow work inside a
  handler visibly freezes the UI.
- A `JTextField` fires an ActionEvent when Enter is pressed in it. Handing it
  the same listener as the Greet button gets Enter-to-submit for one line and
  zero duplicated logic.
- `button.doClick()` fires the listeners exactly as a real click does, and
  needs no display — which is how the behaviour gets tested headlessly.
- `requestFocusInWindow()` after an action puts the cursor where the user
  needs it next; cheap, and the form feels much less clumsy for it.
- Keep the *logic* out of the listener. `Greetings.greet(name, style, hour)`
  is a plain static method with no Swing imports, so it can be checked by
  comparing strings — no panel, no events, no display.
- Pass the clock in rather than calling `LocalTime.now()` inside the logic.
  A method handed the hour gives the same answer every run; a method that
  reads the clock gives a different one depending on when the tests run.
- `JComboBox` renders items with `toString()`, so giving the enum constants a
  display label is enough for a tidy drop-down — no `ListCellRenderer` needed.
- A `switch` over an enum should still have a `default` that throws. Add a new
  constant later and it fails loudly instead of quietly returning null.
- `GridLayout(2, 2, ...)` was all the second form row needed. Equal cells are
  the whole reason the two labels and the two inputs stay aligned.

## Roadmap

- [x] Day 1 — Scaffold: README, `.gitignore`, `Main.java` with a JFrame that compiles headless
- [x] Day 2 — Add a `JPanel` with a real layout manager (BorderLayout / GridLayout)
- [x] Day 3 — Add components: labels, a text field, and buttons
- [x] Day 4 — Event handling: wire a button's `ActionListener` to update the UI
- [x] Day 5 — A small feature: greeting generator or simple calculator
- [ ] Day 6 — Polish: input validation, window sizing, README build instructions
