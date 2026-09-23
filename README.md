# java-swing-gui

A small desktop GUI built with **Java Swing**, using only the standard library —
no Maven, no Gradle, no external dependencies. Just `javac` and `java`.

The point is to learn how a desktop UI is actually assembled: a window, a panel,
some components, a layout, and an event listener that ties a button to code.

## Requirements

- JDK 11 or newer (`javac -version` to check)

## Build and run

```bash
# compile into out/ (warnings on, so problems surface at build time)
javac -Xlint:all -d out src/*.java

# run
java -cp out Main
```

If a machine has the JRE but no `javac` binary, the compiler is often still
in the runtime image and can be called directly:

```bash
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d out src/*.java
```

### On a desktop

A small window opens with a name field, a greeting-style drop-down, and Greet
and Clear buttons. Type a name and press Greet (or just hit Enter in the
field) and the greeting appears; Clear puts everything back. Invalid input -
blank, over-long, or containing digits or punctuation - shows a red message on
the status line instead of a greeting. The field itself refuses to hold more
than 40 characters, so the length rule is enforced as you type.

Keyboard: `Alt+N` jumps to the name field, `Alt+S` to the style drop-down,
`Alt+G` greets, `Alt+C` clears, and Enter greets from anywhere in the window.

### On a headless machine

A server or container with no display cannot open a window, so `Main` detects
that and runs its built-in checks instead of crashing with `HeadlessException`.
The checks build the panel, click the buttons with `doClick()`, and assert on
the results, so `java -cp out Main` is effectively the test suite:

```
Headless environment detected - skipping window.
AppPanel check: NORTH, CENTER and SOUTH are all filled.
Component check: name field, Greet/Clear buttons and labels are all present.
Behaviour check: Greet handles empty and real input, and Clear resets.
Greetings check: all four styles and the hour boundaries are correct.
Validation check: names, limits and the error state all behave.
Compiled and ran successfully. Run this on a desktop to see the GUI.
```

Any failure throws `IllegalStateException` with the reason, and the exit code
is non-zero - so this works as a CI step as-is.

## Project layout

```
src/Main.java       creates and shows the JFrame
src/AppPanel.java   the contents of the window, and their layout
src/Greetings.java  the greeting wording - plain Java, no Swing
src/NameValidator.java  the input rules - also plain Java, no Swing
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
- `setPreferredSize` on a frame *overrides* what `pack()` worked out, which
  throws away the layout managers' answer in favour of a guess in pixels. Let
  `pack()` size the window, then read `getSize()` back for the minimum — the
  floor then follows the layout instead of drifting away from it.
- A `DocumentFilter` sits between the keyboard and a text field's model, so it
  catches typing *and* pasting. Call `super.replace(...)` to allow an edit;
  just return to drop it. Capping length there beats validating afterwards —
  the bad state never exists.
- `insertString` and `replace` both need overriding, but `insertString` can
  simply delegate to `replace` with a length of 0 and the rule lives once.
- `Character.isLetter(c)` rather than `c >= 'a' && c <= 'z'`: it is true for
  accented and non-Latin letters, so the form does not quietly reject "Zoë".
- Return the error *message* from a validator instead of a boolean. The caller
  then has nothing left to decide, and the wording stays in one place.
- Colour is a second signal, never the only one. The status line says what is
  wrong in words; the red just makes it faster to notice.
- A validator that is plain Java can be tested by calling it. Most of Day 6's
  checks are one line each precisely because none of them need a window.

## Roadmap

- [x] Day 1 — Scaffold: README, `.gitignore`, `Main.java` with a JFrame that compiles headless
- [x] Day 2 — Add a `JPanel` with a real layout manager (BorderLayout / GridLayout)
- [x] Day 3 — Add components: labels, a text field, and buttons
- [x] Day 4 — Event handling: wire a button's `ActionListener` to update the UI
- [x] Day 5 — A small feature: greeting generator or simple calculator
- [x] Day 6 — Polish: input validation, window sizing, README build instructions

All six steps are done — the project is complete.
