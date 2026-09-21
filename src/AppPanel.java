import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

    /** Produces the greeting. */
    private final JButton greetButton = new JButton("Greet");

    /** Clears the field and the output. */
    private final JButton clearButton = new JButton("Clear");

    /** The label in the middle that will show the result. */
    private final JLabel outputLabel = new JLabel("", JLabel.CENTER);

    /** The thin status line along the bottom of the window. */
    private final JLabel statusLabel = new JLabel("Ready", JLabel.LEFT);

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

        if (name.isEmpty()) {
            outputLabel.setText("");
            statusLabel.setText("Type a name first.");
            // Moving focus where the user needs to act next is a small thing
            // that makes a form feel much less clumsy.
            nameField.requestFocusInWindow();
            return;
        }

        outputLabel.setText("Hello, " + name + "!");
        statusLabel.setText("Greeted " + name + ".");
    }

    /** Puts the panel back the way it started. */
    private void onClear(ActionEvent event) {
        nameField.setText("");
        outputLabel.setText("");
        statusLabel.setText("Ready");
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
        nameField.setToolTipText("Type a name, then press Greet");

        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.add(nameLabel);
        row.add(nameField);
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
        statusLabel.setForeground(Color.DARK_GRAY);

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
}
