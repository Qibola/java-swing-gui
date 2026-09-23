import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * The main content panel for the app.
 *
 * Day 2 goal: take control of the layout with BorderLayout + GridLayout.
 * Day 3 goal: replace the placeholder cells with the components the app
 *             actually needs - labels, a text field, and buttons.
 * Day 4 goal: make the buttons do something - attach ActionListeners that
 *             read the text field and update the labels.
 * Day 5 goal: the feature itself - a style drop-down, with the wording of
 *             each greeting delegated to the Greetings class so the panel
 *             stays responsible only for input and display.
 * Day 6 goal: polish - real input validation (delegated to NameValidator),
 *             an error colour on the status line, and a field that refuses
 *             over-long input rather than complaining about it afterwards.
 *
 * The layout managers doing the work here:
 *
 *   BorderLayout - five regions (NORTH, SOUTH, EAST, WEST, CENTER), one
 *   component each. The edges keep their preferred size and CENTER soaks up
 *   whatever is left, which is why the header and the button bar stay thin
 *   while the middle grows when the window is resized.
 *
 *   GridLayout - equal-sized cells filled left to right. Used for the single
 *   form row so the label and the text field line up predictably.
 *
 *   FlowLayout - lays components out in a row at their preferred size. The
 *   right tool for a button bar, because buttons should stay button-sized
 *   instead of being stretched to fill a whole region.
 *
 * Nesting panels is the usual Swing answer to "I need more than five slots":
 * each region of the outer BorderLayout holds a panel running its own layout
 * manager inside it.
 *
 * Keeping the interactive components as fields (rather than locals) is what
 * makes Day 4 easy: the listeners can refer to them directly instead of
 * walking the container hierarchy looking for them.
 */
public class AppPanel extends JPanel {

    // Swing components are Serializable, and javac's -Xlint:serial warns when
    // a serializable class has no version id. Swing UIs are never really
    // serialised in practice, but declaring it keeps the build warning-free.
    private static final long serialVersionUID = 1L;

    /** Where the user types their name. */
    private final JTextField nameField = new JTextField(16);

    /** Picks which style of greeting to produce. */
    private final JComboBox<Greetings.Style> styleBox =
            new JComboBox<>(Greetings.Style.values());

    /** Produces the greeting. */
    private final JButton greetButton = new JButton("Greet");

    /** Clears the field and the output. */
    private final JButton clearButton = new JButton("Clear");

    /** The label in the middle that will show the result. */
    private final JLabel outputLabel = new JLabel("", JLabel.CENTER);

    /** The thin status line along the bottom of the window. */
    private final JLabel statusLabel = new JLabel("Ready", JLabel.LEFT);

    /** Normal status text: present but not shouting. */
    private static final Color STATUS_COLOR = Color.DARK_GRAY;

    /** Something the user has to fix. A darker red than Color.RED, which
     *  is hard to read as small text on a light background. */
    private static final Color ERROR_COLOR = new Color(0xB0, 0x00, 0x20);

    public AppPanel() {
        // A JPanel defaults to FlowLayout, so the layout must be set explicitly.
        // The two ints are the horizontal and vertical gaps, in pixels, that
        // BorderLayout leaves between regions.
        super(new BorderLayout(8, 8));

        // Breathing room between the panel's contents and the window frame.
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottom(), BorderLayout.SOUTH);

        wireUpActions();
    }

    /**
     * Day 4: connect the buttons to code.
     *
     * An ActionListener is a single-method interface, so a lambda works as one
     * directly - `e -> ...` is the whole implementation. Swing calls it on the
     * Event Dispatch Thread, which is exactly the thread allowed to change
     * components, so the handlers can update labels without any extra
     * ceremony. (The flip side: anything slow in here freezes the UI, because
     * the EDT is also the thread that repaints the window.)
     *
     * Adding the same listener to the text field as to the Greet button means
     * pressing Enter inside the field greets too, without duplicating logic.
     */
    private void wireUpActions() {
        greetButton.addActionListener(this::onGreet);
        nameField.addActionListener(this::onGreet);
        clearButton.addActionListener(this::onClear);
    }

    /**
     * Reads the name field and shows a greeting.
     *
     * The ActionEvent parameter carries which component fired and when. This
     * handler does not need it, but the signature has to match the interface,
     * so it stays.
     */
    private void onGreet(ActionEvent event) {
        String name = nameField.getText().trim();

        // One call covers empty, too-long and illegal-character input. The
        // panel does not know or care which rule failed - it just shows the
        // sentence it is handed.
        String problem = NameValidator.validate(name);
        if (problem != null) {
            outputLabel.setText("");
            showError(problem);
            // Moving focus where the user needs to act next is a small thing
            // that makes a form feel much less clumsy.
            nameField.requestFocusInWindow();
            return;
        }

        // The clock is read here, in the UI layer, and passed in. Greetings
        // itself stays a pure function of its arguments, which is what makes
        // it testable at any time of day.
        Greetings.Style style = (Greetings.Style) styleBox.getSelectedItem();
        int hour = java.time.LocalTime.now().getHour();

        outputLabel.setText(Greetings.greet(name, style, hour));
        showStatus("Greeted " + name + " (" + style + ").");
    }

    /** Ordinary status text, in the quiet grey. */
    private void showStatus(String message) {
        statusLabel.setForeground(STATUS_COLOR);
        statusLabel.setText(message);
    }

    /**
     * Status text for something the user needs to fix. Colour alone is a poor
     * signal - it is invisible to a colour-blind user and to a screen reader -
     * so the message itself always says what went wrong; the red is only a
     * second, faster hint on top of it.
     */
    private void showError(String message) {
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setText(message);
    }

    /** Puts the panel back the way it started. */
    private void onClear(ActionEvent event) {
        nameField.setText("");
        styleBox.setSelectedIndex(0);
        outputLabel.setText("");
        showStatus("Ready");
        nameField.requestFocusInWindow();
    }

    private JLabel createHeader() {
        JLabel header = new JLabel("Swing Practice", JLabel.CENTER);
        // deriveFont keeps the platform's font family and only changes the
        // style/size, so the app still looks native on every OS.
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        return header;
    }

    /** The middle of the window: a form row on top, the output area below. */
    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(createFormRow(), BorderLayout.NORTH);
        center.add(createOutputArea(), BorderLayout.CENTER);
        return center;
    }

    /**
     * One labelled input: "Your name:" next to a text field.
     *
     * setLabelFor is the bit that is easy to skip and worth doing - it ties
     * the label to the field so screen readers announce them together, and so
     * the mnemonic (Alt+N here) moves focus into the field rather than just
     * underlining a letter.
     */
    private JPanel createFormRow() {
        JLabel nameLabel = new JLabel("Your name:", JLabel.RIGHT);
        nameLabel.setDisplayedMnemonic('N');
        nameLabel.setLabelFor(nameField);

        // A tooltip is one line of code and makes the UI explain itself.
        nameField.setToolTipText("Type a name, then press Greet (max "
                + NameValidator.MAX_LENGTH + " characters)");

        // Stopping the over-long input at the keystroke is friendlier than
        // accepting it and then complaining: a DocumentFilter sits between the
        // keyboard and the field's model and can simply decline the edit.
        ((AbstractDocument) nameField.getDocument())
                .setDocumentFilter(new MaxLengthFilter(NameValidator.MAX_LENGTH));

        JLabel styleLabel = new JLabel("Greeting style:", JLabel.RIGHT);
        styleLabel.setDisplayedMnemonic('S');
        styleLabel.setLabelFor(styleBox);
        styleBox.setToolTipText("Pick how the greeting should be worded");

        // Two rows now, so GridLayout gets a second row rather than a second
        // nested panel: every cell is the same size, which is exactly what
        // keeps the two labels and the two inputs in line with each other.
        JPanel row = new JPanel(new GridLayout(2, 2, 8, 6));
        row.add(nameLabel);
        row.add(nameField);
        row.add(styleLabel);
        row.add(styleBox);
        return row;
    }

    /**
     * The result area. An empty JLabel has almost no preferred height, which
     * would make the window jump in size the moment text appeared, so the
     * border reserves that space up front.
     */
    private JPanel createOutputArea() {
        outputLabel.setFont(outputLabel.getFont().deriveFont(Font.PLAIN, 16f));
        outputLabel.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));

        JPanel area = new JPanel(new BorderLayout());
        area.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        area.add(outputLabel, BorderLayout.CENTER);
        return area;
    }

    /**
     * The bottom strip: a right-aligned button bar with the status line
     * tucked underneath it.
     */
    private JPanel createBottom() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        greetButton.setMnemonic('G');
        clearButton.setMnemonic('C');
        buttons.add(clearButton);
        buttons.add(greetButton);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        statusLabel.setForeground(STATUS_COLOR);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(statusLabel, BorderLayout.SOUTH);
        return bottom;
    }

    // --- Accessors, so Day 4 can wire up behaviour without reaching into the
    // --- layout to hunt for components.

    public JTextField getNameField() {
        return nameField;
    }

    public JComboBox<Greetings.Style> getStyleBox() {
        return styleBox;
    }

    public JButton getGreetButton() {
        return greetButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JLabel getOutputLabel() {
        return outputLabel;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * Caps how much text a field will hold.
     *
     * A DocumentFilter intercepts every change on its way into the document,
     * so both typing (insertString/replace) and pasting go through it. Calling
     * super.* lets the edit through; returning without calling it drops the
     * edit silently, which is what keeps the field at its limit.
     */
    private static final class MaxLengthFilter extends DocumentFilter {

        private final int max;

        MaxLengthFilter(int max) {
            this.max = max;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            String incoming = text == null ? "" : text;
            int resulting = fb.getDocument().getLength() - length + incoming.length();

            if (resulting <= max) {
                super.replace(fb, offset, length, incoming, attr);
                return;
            }

            // Paste of something too long: keep the part that fits rather than
            // rejecting the whole thing.
            int room = max - (fb.getDocument().getLength() - length);
            if (room > 0) {
                super.replace(fb, offset, length, incoming.substring(0, room), attr);
            }
        }
    }
}
