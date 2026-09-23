import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Entry point for the Swing GUI practice app.
 *
 * Day 1 goal: get a window on screen using only built-in Java libraries.
 * Day 2 goal: hand the window's contents over to AppPanel so this class is
 *             only responsible for creating and showing the frame.
 * Day 3 goal: keep that split intact while AppPanel grows real components,
 *             and extend the headless check to cover them.
 * Day 4 goal: the buttons now do something, so the headless check clicks them
 *             and asserts on what changed.
 * Day 5 goal: a real feature - selectable greeting styles - with the wording
 *             logic in Greetings, which is pure enough to test directly.
 * Day 6 goal: polish - validation rules of their own (NameValidator), and
 *             window sizing derived from the packed layout instead of
 *             guessed at in pixels.
 *
 * Two Swing rules worth remembering from the start:
 *   1. Build and show the UI on the Event Dispatch Thread (EDT).
 *      Swing components are not thread-safe, so SwingUtilities.invokeLater
 *      hands the work to the one thread Swing is allowed to touch them from.
 *   2. setDefaultCloseOperation(EXIT_ON_CLOSE) makes the X button end the
 *      program. Without it the window closes but the JVM keeps running.
 */
public class Main {

    /** Builds the main window. Must be called on the EDT. */
    private static JFrame createWindow() {
        JFrame frame = new JFrame("Swing Practice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AppPanel panel = new AppPanel();

        // setContentPane replaces the frame's default panel outright, so
        // AppPanel's BorderLayout is the one in charge of the window.
        frame.setContentPane(panel);

        // The "default button" is the one Enter activates from anywhere in
        // the window - a free keyboard shortcut that costs one line.
        frame.getRootPane().setDefaultButton(panel.getGreetButton());

        // Sizing, in the order that actually behaves.
        //
        // pack() asks the layout managers how much room the contents need and
        // sizes the window to exactly that. Calling setPreferredSize first (as
        // this did until Day 6) overrides that answer with a guess in pixels,
        // which goes wrong the moment a platform's font is bigger than the one
        // the number was picked on.
        frame.pack();

        // The packed size is the true minimum: shrink below it and components
        // start getting clipped. Taking it from pack() rather than hard-coding
        // it means the floor follows the layout instead of drifting from it.
        frame.setMinimumSize(frame.getSize());

        // Then open a little roomier than the bare minimum, so the output area
        // has somewhere to put a long greeting without the window jumping.
        frame.setSize(new Dimension(
                Math.max(frame.getWidth(), 460),
                Math.max(frame.getHeight(), 300)));

        frame.setLocationRelativeTo(null); // centre on screen
        return frame;
    }

    public static void main(String[] args) {
        // A headless machine (like a CI box or a container) has no display,
        // so opening a window would throw HeadlessException. Detecting that
        // up front lets the same code compile and run everywhere.
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment detected - skipping window.");
            checkPanelHeadless();
            System.out.println("Compiled and ran successfully. Run this on a desktop to see the GUI.");
            return;
        }

        SwingUtilities.invokeLater(() -> createWindow().setVisible(true));
    }

    /**
     * A JFrame needs a display, but lightweight components like JPanel,
     * JLabel, JButton and JTextField do not. That means the panel can still
     * be built and inspected on a headless machine, which is enough to catch
     * a broken layout or a missing component without a screen.
     */
    private static void checkPanelHeadless() {
        AppPanel panel = new AppPanel();
        BorderLayout layout = (BorderLayout) panel.getLayout();

        String[] regions = {BorderLayout.NORTH, BorderLayout.CENTER, BorderLayout.SOUTH};
        for (String region : regions) {
            if (layout.getLayoutComponent(region) == null) {
                throw new IllegalStateException("AppPanel is missing its " + region + " component");
            }
        }
        System.out.println("AppPanel check: NORTH, CENTER and SOUTH are all filled.");

        require(panel.getNameField() != null, "name field is missing");
        require(panel.getOutputLabel() != null, "output label is missing");
        require(panel.getStatusLabel() != null, "status label is missing");
        require("Greet".equals(panel.getGreetButton().getText()), "Greet button is mislabelled");
        require("Clear".equals(panel.getClearButton().getText()), "Clear button is mislabelled");

        // The text field should start empty and be editable, and the label it
        // is paired with should point back at it via setLabelFor.
        require(panel.getNameField().getText().isEmpty(), "name field should start empty");
        require(panel.getNameField().isEditable(), "name field should be editable");

        System.out.println("Component check: name field, Greet/Clear buttons and labels are all present.");

        checkBehaviour(panel);
    }

    /**
     * Day 4: exercise the button handlers without a screen.
     *
     * JButton.doClick() fires the button's ActionListeners exactly as a real
     * click would, and none of that needs a display - so the behaviour added
     * today is genuinely testable on a headless box, not just compilable.
     */
    private static void checkBehaviour(AppPanel panel) {
        require(panel.getGreetButton().getActionListeners().length == 1,
                "Greet button should have exactly one listener");
        require(panel.getClearButton().getActionListeners().length == 1,
                "Clear button should have exactly one listener");
        require(panel.getNameField().getActionListeners().length == 1,
                "Enter in the name field should greet too");

        // Empty input: no greeting, and the status line says why.
        panel.getGreetButton().doClick();
        require(panel.getOutputLabel().getText().isEmpty(),
                "an empty name should not produce a greeting");
        require("Type a name first.".equals(panel.getStatusLabel().getText()),
                "an empty name should explain itself in the status line");

        // A real name: the greeting appears, surrounding whitespace trimmed.
        panel.getNameField().setText("  Alex  ");
        panel.getGreetButton().doClick();
        require("Hello, Alex!".equals(panel.getOutputLabel().getText()),
                "expected a trimmed greeting, got: " + panel.getOutputLabel().getText());
        require("Greeted Alex (Friendly).".equals(panel.getStatusLabel().getText()),
                "status line should name the style used");

        // Switching the drop-down changes the wording.
        panel.getStyleBox().setSelectedItem(Greetings.Style.CASUAL);
        panel.getGreetButton().doClick();
        require(panel.getOutputLabel().getText().startsWith("Hey Alex"),
                "casual style should change the wording, got: " + panel.getOutputLabel().getText());

        // Clear puts everything back to its starting state.
        panel.getClearButton().doClick();
        require(panel.getNameField().getText().isEmpty(), "Clear should empty the name field");
        require(panel.getOutputLabel().getText().isEmpty(), "Clear should empty the output");
        require("Ready".equals(panel.getStatusLabel().getText()), "Clear should reset the status line");
        require(panel.getStyleBox().getSelectedIndex() == 0, "Clear should reset the style drop-down");

        System.out.println("Behaviour check: Greet handles empty and real input, and Clear resets.");

        checkGreetings();
        checkValidation(panel);
    }

    /**
     * Day 5: the greeting logic lives outside Swing, so it can be checked
     * without building a panel at all - no components, no event plumbing,
     * just inputs and expected strings.
     */
    private static void checkGreetings() {
        require("Hello, Sam!".equals(Greetings.greet("Sam", Greetings.Style.FRIENDLY, 9)),
                "friendly greeting is wrong");
        require(Greetings.greet("Sam", Greetings.Style.FORMAL, 9).startsWith("Good day, Sam."),
                "formal greeting is wrong");
        require(Greetings.greet("Sam", Greetings.Style.CASUAL, 9).startsWith("Hey Sam"),
                "casual greeting is wrong");

        // Because the hour is a parameter, every boundary is easy to pin down.
        require("Good morning".equals(Greetings.partOfDay(0)), "midnight should be morning");
        require("Good morning".equals(Greetings.partOfDay(11)), "11:00 should be morning");
        require("Good afternoon".equals(Greetings.partOfDay(12)), "noon should be afternoon");
        require("Good afternoon".equals(Greetings.partOfDay(17)), "17:00 should be afternoon");
        require("Good evening".equals(Greetings.partOfDay(18)), "18:00 should be evening");
        require("Good evening".equals(Greetings.partOfDay(23)), "23:00 should be evening");

        require("Good evening, Sam!".equals(Greetings.greet("Sam", Greetings.Style.TIME_OF_DAY, 20)),
                "time-of-day greeting is wrong");

        // A blank name is rejected by the logic itself, not just by the UI.
        boolean threw = false;
        try {
            Greetings.greet("   ", Greetings.Style.FRIENDLY, 9);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        require(threw, "a blank name should be rejected");

        System.out.println("Greetings check: all four styles and the hour boundaries are correct.");
    }

    /**
     * Day 6: the validation rules, checked directly and then through the UI.
     *
     * NameValidator is plain Java, so most of this is just calling a method
     * and comparing strings. The last few lines matter more: they confirm the
     * panel is actually wired to the validator, and that the document filter
     * stops over-long input at the field rather than letting it through.
     */
    private static void checkValidation(AppPanel panel) {
        require(NameValidator.validate("Alex") == null, "a plain name should be accepted");
        require(NameValidator.validate("Mary-Jane O'Neill") == null,
                "hyphens and apostrophes should be accepted");
        require(NameValidator.validate("Zoë") == null, "accented letters should be accepted");

        require(NameValidator.validate("") != null, "an empty name should be rejected");
        require(NameValidator.validate("   ") != null, "a whitespace-only name should be rejected");
        require(NameValidator.validate("Alex99") != null, "digits should be rejected");
        require(NameValidator.validate("<script>") != null, "punctuation should be rejected");

        String tooLong = repeat("a", NameValidator.MAX_LENGTH + 1);
        require(NameValidator.validate(tooLong) != null, "an over-long name should be rejected");
        require(NameValidator.validate(repeat("a", NameValidator.MAX_LENGTH)) == null,
                "a name exactly at the limit should be accepted");

        // Through the UI: a rejected name produces no greeting, and the status
        // line both explains itself and changes colour.
        panel.getNameField().setText("Alex99");
        panel.getGreetButton().doClick();
        require(panel.getOutputLabel().getText().isEmpty(),
                "an invalid name should not produce a greeting");
        require(panel.getStatusLabel().getText().contains("letters"),
                "the status line should say what is wrong, got: " + panel.getStatusLabel().getText());
        require(!java.awt.Color.DARK_GRAY.equals(panel.getStatusLabel().getForeground()),
                "an error should change the status colour");

        // ...and a good one afterwards clears the error state again.
        panel.getNameField().setText("Alex");
        panel.getGreetButton().doClick();
        require("Hello, Alex!".equals(panel.getOutputLabel().getText()),
                "a valid name after an invalid one should still greet");
        require(java.awt.Color.DARK_GRAY.equals(panel.getStatusLabel().getForeground()),
                "a successful greeting should restore the normal status colour");

        // The document filter should cap the field itself, so the too-long
        // case is prevented rather than merely reported.
        panel.getNameField().setText(repeat("b", NameValidator.MAX_LENGTH + 20));
        require(panel.getNameField().getText().length() == NameValidator.MAX_LENGTH,
                "the field should cap input at " + NameValidator.MAX_LENGTH
                        + ", got " + panel.getNameField().getText().length());

        panel.getClearButton().doClick();
        System.out.println("Validation check: names, limits and the error state all behave.");
    }

    /** String.repeat arrived in Java 11; this keeps the source usable on 8. */
    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder(s.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("AppPanel check failed: " + message);
        }
    }
}
