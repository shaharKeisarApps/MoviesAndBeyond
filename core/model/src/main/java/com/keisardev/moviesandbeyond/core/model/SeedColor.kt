package com.keisardev.moviesandbeyond.core.model

/** Seed colors for theme customization when dynamic color is not available or disabled. */
enum class SeedColor(val argb: Long) {
    DEFAULT(0xFF6750A4), // Material default purple
    BLUE(0xFF1976D2),
    GREEN(0xFF388E3C),
    ORANGE(0xFFE65100),
    RED(0xFFD32F2F),
    TEAL(0xFF00796B),
    PINK(0xFFE91E63),
    AMBER(0xFFFFA000),
    CUSTOM(0); // Custom color - actual value stored separately in customColorArgb

    companion object {
        fun fromName(name: String): SeedColor = entries.find { it.name == name } ?: DEFAULT

        const val DEFAULT_CUSTOM_COLOR_ARGB: Long = 0xFF6750A4
    }
}
