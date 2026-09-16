import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * The main content panel for the app.
 *
 * Day 2 goal: stop letting the JFrame place things by accident and take
 * control of the layout.
 *
 * Two layout managers are doing the work here:
 *
 *   BorderLayout — divides a container into five regions: NORTH, SOUTH,
 *   EAST, WEST and CENTER. Only one component fits per region. The edges
 *   keep their preferred size and CENTER soaks up whatever space is left,
 *   which is why the header and footer stay thin while the middle grows
 *   when the window is resized.
 *
 *   GridLayout — splits a container into equal-sized cells, filled left to
 *   right, top to bottom. Every cell is exactly the same size no matter what
 *   is inside it, so it is the right tool for something like a keypad and the
 *   wrong tool for a form with a long label next to a short one.
 *
 * Splitting the UI into its own JPanel subclass (rather than piling
 * components onto the JFrame) keeps Main.java about *showing* a window and
 * this file about *what is in it* — which is what makes Day 3's components
 * easy to drop in.
 */
public class AppPanel extends JPanel {

    // Swing components are Serializable, and javac's -Xlint:serial warns when
    // a serializable class has no version id. Swing UIs are never really
    // serialised in practice, but declaring it keeps the build warning-free.
    private static final long serialVersionUID = 1L;

    public AppPanel() {
        // A JPanel defaults to FlowLayout, so the layout must be set explicitly.
        // The two ints are the horizontal and vertical gaps, in pixels, that
        // BorderLayout leaves between regions.
        super(new BorderLayout(8, 8));

        // Breathing room between the panel's contents and the window frame.
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(createHeader(), BorderLayout.NORTH);
        add(createGrid(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JLabel createHeader() {
        JLabel header = new JLabel("Swing Practice", JLabel.CENTER);
        // deriveFont keeps the platform's font family and only changes the
        // style/size, so the app still looks native on every OS.
        header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        return header;
    }

    /**
     * A 2x2 grid of placeholder cells. Real components arrive on Day 3 — for
     * now these just make the grid visible so the layout can be checked.
     */
    private JPanel createGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        for (int i = 1; i <= 4; i++) {
            JLabel cell = new JLabel("Cell " + i, JLabel.CENTER);
            cell.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
            cell.setOpaque(true);
            grid.add(cell);
        }
        return grid;
    }

    private JLabel createFooter() {
        JLabel footer = new JLabel("Ready", JLabel.LEFT);
        footer.setFont(footer.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
        footer.setForeground(java.awt.Color.DARK_GRAY);
        return footer;
    }
}
