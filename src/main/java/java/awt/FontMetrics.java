package java.awt;

public class FontMetrics {
    Font font;

    public FontMetrics(Font font) {
        this.font = font;
    }

    public int stringWidth(String str) {
        return (int) font.bitmapfont.stringWidth(str);
    }

    public int charWidth(char ch) {
        return font.bitmapfont.charWidth(ch);
    }

    public int getAscent() {
        return getHeight() - getDescent();
    }

    public int getDescent() {
        return Math.max(1, getHeight() / 4);
    }

    public int getLeading() {
        return 0;
    }

    public int getHeight() {
        return font.bitmapfont.getHeight();
    }
}
