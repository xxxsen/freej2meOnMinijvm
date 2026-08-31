package java.awt;

public class RenderingHints {
    public static class Key {
        private final int id;

        protected Key(int id) {
            this.id = id;
        }

        public int hashCode() {
            return id;
        }
    }

    public static final Key KEY_TEXT_ANTIALIASING = new Key(2);
    public static final Object VALUE_TEXT_ANTIALIAS_ON = new Object();
}
