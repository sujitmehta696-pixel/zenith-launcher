package com.example.data.model

enum class ThemeCategory(val displayName: String) {
    ALL("All"),
    AMOLED("AMOLED"),
    DARK("Dark"),
    LIGHT("Light"),
    MINIMAL("Minimal"),
    GRADIENT("Gradient"),
    MATERIAL("Material"),
    LUXURY("Luxury"),
    FUTURISTIC("Futuristic"),
    NATURE("Nature"),
    EXCLUSIVE("Exclusive")
}

enum class ThemeCardStyle(val displayName: String) {
    GLASSMORPHIC("Glassmorphic"),
    SOLID_ELEVATED("Solid Elevated"),
    OUTLINED_MINIMAL("Outlined Minimal"),
    GRADIENT_BORDER("Gradient Border")
}

enum class ThemeIconStyle(val displayName: String) {
    SQUIRCLE("Squircle"),
    CIRCLE("Circle"),
    ROUNDED_SQUARE("Rounded"),
    HEX_TECH("Hexagon"),
    TEARDROP("Teardrop"),
    CRYSTAL_GLASS("Crystal Glass")
}

enum class ThemeFontStyle(val displayName: String) {
    PLUS_JAKARTA_SANS("Plus Jakarta"),
    SPACE_GROTESK("Space Grotesk"),
    OUTFIT("Outfit"),
    PLAYFAIR_DISPLAY("Playfair Serif"),
    JETBRAINS_MONO("JetBrains Mono")
}

data class ThemeConfig(
    val id: String,
    val name: String,
    val description: String,
    val category: ThemeCategory,
    val isPremium: Boolean,
    val isDarkTheme: Boolean,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val textHex: Long,
    val cardStyle: ThemeCardStyle,
    val iconStyle: ThemeIconStyle,
    val fontStyle: ThemeFontStyle,
    val cardRadiusDp: Int,
    val associatedWallpaperId: String?,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0
)
