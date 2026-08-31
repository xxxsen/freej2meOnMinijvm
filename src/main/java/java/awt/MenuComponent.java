package java.awt;

import java.io.Serializable;

/** Minimal AWT menu base type used by the desktop frontend API surface. */
public abstract class MenuComponent implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
