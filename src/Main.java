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

        frame.setPreferredSize(new Dimension(460, 320));
        frame.setMinimumSize(new Dimension(360, 260));
        frame.pack();
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

        // No listeners yet - that is Day 4's job. Asserting it keeps the
        // roadmap honest about what today actually delivered.
        require(panel.getGreetButton().getActionListeners().length == 0,
                "Greet button already has a listener (that is Day 4's step)");

        System.out.println("Component check: name field, Greet/Clear buttons and labels are all present.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("AppPanel check failed: " + message);
        }
    }
}
