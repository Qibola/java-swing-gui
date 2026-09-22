/**
 * Day 5: the actual feature - a greeting generator.
 *
 * Everything in here is deliberately plain Java: no Swing imports, no
 * components, no reference to a window. That separation is the point. The
 * UI's job is to collect input and display a result; deciding *what* the
 * result is belongs somewhere that can be reasoned about (and tested) on its
 * own. A method that takes a name and returns a String is trivial to check;
 * the same logic buried inside an ActionListener is not.
 *
 * Note that greet() takes the hour as a parameter rather than calling
 * LocalTime.now() itself. A method that reads the clock gives a different
 * answer depending on when the tests run; a method that is handed the hour
 * always gives the same answer for the same input. The UI is the layer that
 * looks up the real time.
 */
public final class Greetings {

    /** The greeting styles offered in the drop-down. */
    public enum Style {
        FRIENDLY("Friendly"),
        FORMAL("Formal"),
        CASUAL("Casual"),
        TIME_OF_DAY("Time of day");

        private final String label;

        Style(String label) {
            this.label = label;
        }

        /**
         * JComboBox renders each item with toString(), so returning the
         * human-readable label here is enough to get a tidy drop-down -
         * no custom ListCellRenderer needed.
         */
        @Override
        public String toString() {
            return label;
        }
    }

    /** Utility class: never instantiated. */
    private Greetings() {
    }

    /**
     * Builds a greeting for {@code name} in the requested style.
     *
     * @param hour hour of the day, 0-23, used only by {@link Style#TIME_OF_DAY}
     * @throws IllegalArgumentException if the name is blank or the hour is out of range
     */
    public static String greet(String name, Style style, int hour) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be 0-23, got " + hour);
        }

        String who = name.trim();

        switch (style) {
            case FRIENDLY:
                return "Hello, " + who + "!";
            case FORMAL:
                return "Good day, " + who + ". A pleasure to meet you.";
            case CASUAL:
                return "Hey " + who + " — what's up?";
            case TIME_OF_DAY:
                return partOfDay(hour) + ", " + who + "!";
            default:
                // Unreachable today, but a new enum constant would land here
                // instead of silently returning null.
                throw new IllegalStateException("unhandled style: " + style);
        }
    }

    /** Morning until noon, afternoon until 18:00, evening after that. */
    static String partOfDay(int hour) {
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }
}
