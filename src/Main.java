import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * Entry point for the Swing GUI practice app.
 *
 * Day 1 goal: get a window on screen using only built-in Java libraries.
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

        JLabel hello = new JLabel("Hello, Swing!", JLabel.CENTER);
        frame.add(hello);

        frame.setPreferredSize(new Dimension(360, 200));
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
            System.out.println("Compiled and ran successfully. Run this on a desktop to see the GUI.");
            return;
        }

        SwingUtilities.invokeLater(() -> createWindow().setVisible(true));
    }
}
