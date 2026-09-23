/**
 * Day 6: input validation, kept out of the UI.
 *
 * Same principle as Greetings - this is plain Java with no Swing imports, so
 * the rules can be checked by calling a method and comparing strings. The
 * panel's job is to show whatever message comes back, not to decide what the
 * rules are.
 *
 * The contract is deliberately boring: validate() returns null when the input
 * is fine, and a short, user-facing sentence when it is not. Returning the
 * message itself (rather than a boolean, or an enum the caller has to switch
 * on) means the UI has nothing left to decide.
 */
public final class NameValidator {

    /** Longest name the field will accept. Long enough for real names. */
    public static final int MAX_LENGTH = 40;

    /** Utility class: never instantiated. */
    private NameValidator() {
    }

    /**
     * Checks a name typed into the form.
     *
     * @return null if the name is acceptable, otherwise a message to show
     */
    public static String validate(String raw) {
        String name = raw == null ? "" : raw.trim();

        if (name.isEmpty()) {
            return "Type a name first.";
        }
        if (name.length() > MAX_LENGTH) {
            return "That name is too long (max " + MAX_LENGTH + " characters).";
        }

        for (int i = 0; i < name.length(); i++) {
            if (!isNameCharacter(name.charAt(i))) {
                return "Names can only contain letters, spaces, hyphens and apostrophes.";
            }
        }
        return null;
    }

    /**
     * Character.isLetter - not a-z - is the right test here: it is true for
     * accented and non-Latin letters too, so the form does not quietly reject
     * names like "Zoë" or "Ólafur".
     */
    private static boolean isNameCharacter(char c) {
        return Character.isLetter(c) || c == ' ' || c == '-' || c == '\'';
    }
}
