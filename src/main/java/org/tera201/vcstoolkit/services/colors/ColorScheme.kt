package org.tera201.vcstoolkit.services.colors;

public enum ColorScheme {
    DEFAULT("Default", 0),
    UML("UML", 1);

    private final String displayName;
    private final int index;

    ColorScheme(String displayName, int index) {
        this.displayName = displayName;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return displayName; // Display friendly name in JComboBox
    }
}
