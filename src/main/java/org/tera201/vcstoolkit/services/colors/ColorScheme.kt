package org.tera201.vcstoolkit.services.colors

enum class ColorScheme(private val displayName: String, val index: Int) {
    DEFAULT("Default", 0),
    UML("UML", 1);

    override fun toString(): String {
        return displayName // Display friendly name in JComboBox
    }
}
