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

        // setContentPane replaces the frame's default panel outright, so
        // AppPanel's BorderLayout is the one in charge of the window.
        frame.setContentPane(new AppPanel());

        frame.setPreferredSize(new Dimension(420, 280));
        frame.setMinimumSize(new Dimension(320, 220));
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
     * A JFrame needs a display, but lightweight components like JPanel and
     * JLabel do not. That means the panel can still be built and inspected on
     * a headless machine, which is enough to catch a broken layout without a
     * screen.
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
    }
}
