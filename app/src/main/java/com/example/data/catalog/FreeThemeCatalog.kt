package com.example.data.catalog

import com.example.data.model.ThemeCardStyle
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeFontStyle
import com.example.data.model.ThemeIconStyle

object FreeThemeCatalog {

    private data class Spec(
        val id: String,
        val name: String,
        val description: String,
        val category: ThemeCategory,
        val isDark: Boolean,
        val bg: Long,
        val surface: Long,
        val primary: Long,
        val secondary: Long,
        val accent: Long,
        val text: Long,
        val cardStyle: ThemeCardStyle,
        val iconStyle: ThemeIconStyle,
        val fontStyle: ThemeFontStyle,
        val radius: Int,
        val wallpaperId: String
    )

    private val specs: List<Spec> = listOf(
        // 1-10: AMOLED
        Spec("theme_free_obsidian_core", "Obsidian Core", "3D rolling glass interface with Neon Cyan & Royal Purple", ThemeCategory.AMOLED, true, 0xFF080B18, 0xFF10152B, 0xFF00E5FF, 0xFF713BFF, 0xFF168BFF, 0xFFFFFFFF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_amoled_eclipse"),
        Spec("theme_free_pure_pitch_cyan", "Pitch Cyan", "Zero-pixel black canvas with surgical cyan highlights", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF0B1117, 0xFF06B6D4, 0xFF22D3EE, 0xFF67E8F9, 0xFFF1F5F9, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 18, "wp_free_amoled_void_ring"),
        Spec("theme_free_amoled_amber_dial", "Amber Chronometer", "Deep black chronometer palette with warm amber accents", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF14100A, 0xFFF59E0B, 0xFFD97706, 0xFFFDE68A, 0xFFFAF7F0, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_free_amoled_amber_pulse"),
        Spec("theme_free_amoled_crimson_edge", "Crimson Edge", "Pure black backdrop paired with ruby signal highlights", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF160B0F, 0xFFF43F5E, 0xFFFB7185, 0xFFFDA4AF, 0xFFFFF1F2, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 14, "wp_free_amoled_crimson_horizon"),
        Spec("theme_free_amoled_emerald_pulse", "Emerald Telemetry", "True black surface with emerald precision readouts", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF091611, 0xFF10B981, 0xFF34D399, 0xFFA7F3D0, 0xFFECFDF5, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.JETBRAINS_MONO, 16, "wp_free_amoled_emerald_orbit"),
        Spec("theme_free_amoled_monolith", "Monolith Silver", "Achromatic pitch black with brushed platinum surfaces", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF121318, 0xFFE2E8F0, 0xFF94A3B8, 0xFFFFFFFF, 0xFFF8FAFC, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 16, "wp_free_amoled_silver_trace"),
        Spec("theme_free_amoled_violet_shift", "Velvet Violet", "Blackout interface with luminous ultraviolet refraction", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF130D1F, 0xFF8B5CF6, 0xFFA78BFA, 0xFFDDD6FE, 0xFFF5F3FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_amoled_violet_fold"),
        Spec("theme_free_amoled_cobalt_radar", "Cobalt Radar", "Pitch dark radar aesthetic with deep azure rings", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF0A1324, 0xFF3B82F6, 0xFF60A5FA, 0xFF93C5FD, 0xFFEFF6FF, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 20, "wp_free_tech_lidar_sweep"),
        Spec("theme_free_amoled_mint_glass", "Mint Terminal", "AMOLED terminal theme with crisp mint monospace type", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF091514, 0xFF2DD4BF, 0xFF5EEAD4, 0xFFCCFBF1, 0xFFF0FDFA, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 12, "wp_free_tech_neural_mesh"),
        Spec("theme_free_amoled_champagne", "Noir Champagne", "Blackout surface accented with muted champagne gold", ThemeCategory.AMOLED, true, 0xFF030304, 0xFF14120F, 0xFFD4AF37, 0xFFE5C158, 0xFFFEF3C7, 0xFFFAF8F5, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 20, "wp_free_liquid_obsidian"),

        // 11-20: Dark Themes
        Spec("theme_free_dark_graphite_studio", "Graphite Studio", "Refined dark charcoal surfaces with sky blue focus", ThemeCategory.DARK, true, 0xFF0F1218, 0xFF1A1F29, 0xFF60A5FA, 0xFF38BDF8, 0xFFBAE6FD, 0xFFF1F5F9, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 18, "wp_free_gradient_graphite_steel"),
        Spec("theme_free_dark_nordic_fjord", "Nordic Slate", "Deep fjord teal-charcoal inspired by twilight water", ThemeCategory.DARK, true, 0xFF0D1519, 0xFF17252B, 0xFF52B788, 0xFF74C69D, 0xFFD8F3DC, 0xFFEDF6F9, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 20, "wp_free_nature_nordic_fjord"),
        Spec("theme_free_dark_indigo_dusk", "Indigo Dusk", "Deep midnight blue palette with periwinkle elevation", ThemeCategory.DARK, true, 0xFF0B0E1D, 0xFF161B33, 0xFF818CF8, 0xFF6366F1, 0xFFC7D2FE, 0xFFEEF2FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 22, "wp_free_gradient_twilight_indigo"),
        Spec("theme_free_dark_terracotta", "Smoked Terracotta", "Warm espresso dark mode with burnt sienna highlights", ThemeCategory.DARK, true, 0xFF16100E, 0xFF241A16, 0xFFEA580C, 0xFFFB923C, 0xFFFED7AA, 0xFFFFF7ED, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 18, "wp_free_abstract_terracotta_flow"),
        Spec("theme_free_dark_kyoto_cedar", "Kyoto Cedar", "Architectural dark wood tone with warm bronze details", ThemeCategory.DARK, true, 0xFF14110F, 0xFF211C18, 0xFFC2956E, 0xFFD4A373, 0xFFFAEDCD, 0xFFFAF5F0, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 16, "wp_free_arch_kyoto_timber"),
        Spec("theme_free_dark_quantum_blue", "Quantum Grid", "Dark navy engineering grid with electric azure accents", ThemeCategory.DARK, true, 0xFF09101C, 0xFF121D30, 0xFF0EA5E9, 0xFF38BDF8, 0xFFBAE6FD, 0xFFF0F9FF, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 12, "wp_free_tech_quantum_lattice"),
        Spec("theme_free_dark_plum_velvet", "Plum Velvet", "Moody aubergine dark canvas with rose-gold highlights", ThemeCategory.DARK, true, 0xFF140D18, 0xFF221629, 0xFFF472B6, 0xFFC084FC, 0xFFFBCFE8, 0xFFFDF2F8, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_gradient_rose_quartz"),
        Spec("theme_free_dark_evergreen", "Evergreen Moss", "Forest night canvas with calm botanical sage accents", ThemeCategory.DARK, true, 0xFF0C1410, 0xFF16241C, 0xFF6EE7B7, 0xFF34D399, 0xFFD1FAE5, 0xFFECFDF5, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_nature_emerald_ridge"),
        Spec("theme_free_dark_brutalist", "Concrete Atrium", "Architectural neutral charcoal with crisp ash borders", ThemeCategory.DARK, true, 0xFF141517, 0xFF202226, 0xFFD1D5DB, 0xFF9CA3AF, 0xFFF9FAFB, 0xFFF3F4F6, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 10, "wp_free_arch_brutalist_concrete"),
        Spec("theme_free_dark_nebula", "Deep Nebula", "Cosmic slate-violet palette tuned for space wallpapers", ThemeCategory.DARK, true, 0xFF0B0A18, 0xFF17142E, 0xFFA855F7, 0xFF38BDF8, 0xFFE9D5FF, 0xFFF5F3FF, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_free_cosmic_nebula"),

        // 21-28: Light Themes
        Spec("theme_free_light_alabaster", "Alabaster Atelier", "Warm sunlit ivory canvas with espresso contrast", ThemeCategory.LIGHT, false, 0xFFF7F4EE, 0xFFFFFFFF, 0xFF2C2623, 0xFF8C7A6B, 0xFFD97706, 0xFF1C1917, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 20, "wp_free_minimal_ivory_sand"),
        Spec("theme_free_light_porcelain", "Porcelain Mist", "Clean cool-white studio surface with slate blue highlights", ThemeCategory.LIGHT, false, 0xFFF1F5F9, 0xFFFFFFFF, 0xFF0284C7, 0xFF38BDF8, 0xFF0369A1, 0xFF0F172A, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 22, "wp_free_minimal_cloud_mist"),
        Spec("theme_free_light_arctic", "Arctic Glass", "Crisp glacier blue-white light mode with frosted panels", ThemeCategory.LIGHT, false, 0xFFECFEFF, 0xFFFFFFFF, 0xFF0891B2, 0xFF06B6D4, 0xFF155E75, 0xFF083344, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_gradient_nordic_ice"),
        Spec("theme_free_light_travertine", "Travertine Stone", "Limestone gallery daylight theme with muted bronze", ThemeCategory.LIGHT, false, 0xFFEFECE6, 0xFFFAF8F5, 0xFF57534E, 0xFFA8A29E, 0xFF9A3412, 0xFF292524, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLAYFAIR_DISPLAY, 14, "wp_free_arch_travertine_colonnade"),
        Spec("theme_free_light_sumi_paper", "Sumi Washi", "Japanese handmade paper white with charcoal ink type", ThemeCategory.LIGHT, false, 0xFFF5F1E8, 0xFFFFFDF9, 0xFF1F1D1A, 0xFF6B655D, 0xFFB91C1C, 0xFF171513, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 12, "wp_free_art_sumi_ink"),
        Spec("theme_free_light_sage_linen", "Sage Linen", "Soft daylight botanical theme with muted eucalyptus", ThemeCategory.LIGHT, false, 0xFFF2F7F4, 0xFFFFFFFF, 0xFF2D6A4F, 0xFF52B788, 0xFF1B4332, 0xFF132A1E, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_minimal_sage_stone"),
        Spec("theme_free_light_lavender_day", "Lavender Daylight", "Airy lilac-tinted white surface with violet accents", ThemeCategory.LIGHT, false, 0xFFF6F4FF, 0xFFFFFFFF, 0xFF6D28D9, 0xFF8B5CF6, 0xFF4C1D95, 0xFF1E1B4B, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_free_abstract_cellular_frost"),
        Spec("theme_free_light_solar_cream", "Solar Cream", "Warm sunlit cream interface with amber editorial accents", ThemeCategory.LIGHT, false, 0xFFFFFBEB, 0xFFFFFFFF, 0xFFB45309, 0xFFF59E0B, 0xFF78350F, 0xFF271705, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_nature_sahara_dunes"),

        // 29-36: Minimal Themes
        Spec("theme_free_minimal_bauhaus", "Bauhaus Grid", "Structured geometric minimal theme with crisp lines", ThemeCategory.MINIMAL, true, 0xFF101216, 0xFF191D24, 0xFFE2E8F0, 0xFF38BDF8, 0xFFF59E0B, 0xFFF8FAFC, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 8, "wp_free_minimal_bauhaus_slate"),
        Spec("theme_free_minimal_zen_ring", "Zen Platinum", "Ultra-quiet monochrome dark UI with zero visual noise", ThemeCategory.MINIMAL, true, 0xFF111318, 0xFF1A1E26, 0xFFCBD5E1, 0xFF64748B, 0xFFF8FAFC, 0xFFF1F5F9, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_minimal_platinum_zen"),
        Spec("theme_free_minimal_contour", "Fine Contour", "Minimalist slate linework with cool silver typography", ThemeCategory.MINIMAL, true, 0xFF0B0E14, 0xFF141A26, 0xFF94A3B8, 0xFF475569, 0xFFE2E8F0, 0xFFF1F5F9, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 16, "wp_free_minimal_midnight_contour"),
        Spec("theme_free_minimal_swiss_light", "Swiss Typographic", "Clean daylight Swiss grid with high-legibility contrast", ThemeCategory.MINIMAL, false, 0xFFFAFAFA, 0xFFFFFFFF, 0xFF111827, 0xFF6B7280, 0xFFEF4444, 0xFF111827, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 6, "wp_free_minimal_cloud_mist"),
        Spec("theme_free_minimal_courtyard", "Monolith Lightless", "Matte carbon minimal cards with restrained contrast", ThemeCategory.MINIMAL, true, 0xFF121214, 0xFF1C1C20, 0xFFE4E4E7, 0xFF71717A, 0xFFFFFFFF, 0xFFFAFAFA, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 14, "wp_free_arch_monolith_courtyard"),
        Spec("theme_free_minimal_mono_code", "Mono Precision", "Minimal developer aesthetic using JetBrains Mono type", ThemeCategory.MINIMAL, true, 0xFF0A0C10, 0xFF131720, 0xFF38BDF8, 0xFF64748B, 0xFF7DD3FC, 0xFFE2E8F0, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 10, "wp_free_tech_carbon_telemetry"),
        Spec("theme_free_minimal_chalk", "Soft Chalk", "Warm matte gray-white minimal surface for calm focus", ThemeCategory.MINIMAL, false, 0xFFEBECEF, 0xFFF8F9FA, 0xFF334155, 0xFF64748B, 0xFF0F172A, 0xFF0F172A, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_free_minimal_ivory_sand"),
        Spec("theme_free_minimal_eucalyptus", "Quiet Eucalyptus", "Low-saturation botanical slate for restful screen time", ThemeCategory.MINIMAL, true, 0xFF121816, 0xFF1B2420, 0xFF84A98C, 0xFF52796F, 0xFFCAD2C5, 0xFFE9F0EC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_minimal_sage_stone"),

        // 37-43: Gradient Themes
        Spec("theme_free_grad_aurora", "Aurora Borealis", "Luminous teal-to-emerald gradient glass interface", ThemeCategory.GRADIENT, true, 0xFF07131A, 0xFF10252E, 0xFF2EC4B6, 0xFF38BDF8, 0xFFCBF3F0, 0xFFF0FDFA, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_free_nature_aurora_borealis"),
        Spec("theme_free_grad_solstice", "Solstice Dusk", "Warm plum-to-tangerine twilight gradient styling", ThemeCategory.GRADIENT, true, 0xFF190E21, 0xFF2A1633, 0xFFF97316, 0xFFEC4899, 0xFFFDE047, 0xFFFFF7ED, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_gradient_solstice_peach"),
        Spec("theme_free_grad_cobalt_silk", "Cobalt Silk", "Rich sapphire silk layers with luminous sky borders", ThemeCategory.GRADIENT, true, 0xFF070F24, 0xFF111D3D, 0xFF3B82F6, 0xFF06B6D4, 0xFFDBEAFE, 0xFFEFF6FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_abstract_cobalt_silk"),
        Spec("theme_free_grad_lagoon", "Deep Lagoon", "Oceanic teal gradient with translucent aqua glass cards", ThemeCategory.GRADIENT, true, 0xFF05181F, 0xFF0B2A35, 0xFF14B8A6, 0xFF22D3EE, 0xFF99F6E4, 0xFFF0FDFA, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_free_gradient_teal_lagoon"),
        Spec("theme_free_grad_prism_glass", "Prism Spectrum", "Iridescent violet-cyan glass borders on deep slate", ThemeCategory.GRADIENT, true, 0xFF0E0F1A, 0xFF191B2E, 0xFF8B5CF6, 0xFF06B6D4, 0xFFF472B6, 0xFFF8FAFC, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.HEX_TECH, ThemeFontStyle.SPACE_GROTESK, 18, "wp_free_abstract_prism_refract"),
        Spec("theme_free_grad_magenta_silk", "Magenta Tide", "Vibrant nocturnal rose-violet silk highlights", ThemeCategory.GRADIENT, true, 0xFF140718, 0xFF240F2B, 0xFFEC4899, 0xFFA855F7, 0xFFFBCFE8, 0xFFFDF2F8, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_abstract_magenta_tide"),
        Spec("theme_free_grad_alpine_mist", "Alpine Twilight", "Cool indigo-to-glacier gradient tuned to mountain peaks", ThemeCategory.GRADIENT, true, 0xFF091221, 0xFF13243D, 0xFF60A5FA, 0xFF457B9D, 0xFFA8DADC, 0xFFF1FAEE, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_free_alpine_twilight"),

        // 44-50: Material-Inspired Themes
        Spec("theme_free_mat_tonal_cobalt", "Material Cobalt", "Material 3 tonal elevation with balanced azure containers", ThemeCategory.MATERIAL, true, 0xFF0D131F, 0xFF192338, 0xFF8AB4F8, 0xFFAECBFA, 0xFFD2E3FC, 0xFFE8EAED, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 24, "wp_free_arch_glass_curtain"),
        Spec("theme_free_mat_sage_you", "Material Sage", "Calm Material You botanical green tonal hierarchy", ThemeCategory.MATERIAL, true, 0xFF0F1612, 0xFF1B2620, 0xFF81C995, 0xFFA8DAB5, 0xFFCEEAD6, 0xFFE8F0EB, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_nature_emerald_ridge"),
        Spec("theme_free_mat_terracotta_light", "Material Clay Light", "Warm Material 3 light tonal surfaces with terracotta seed", ThemeCategory.MATERIAL, false, 0xFFFFF8F5, 0xFFF5EAE4, 0xFF9A4522, 0xFFC26A46, 0xFFFFDBCD, 0xFF231915, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 24, "wp_free_abstract_terracotta_flow"),
        Spec("theme_free_mat_amethyst", "Material Amethyst", "Refined Material 3 violet container system in dark mode", ThemeCategory.MATERIAL, true, 0xFF14111B, 0xFF211C2D, 0xFFD0BCFF, 0xFFCCC2DC, 0xFFEFB8C8, 0xFFE6E1E5, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_space_andromeda_drift"),
        Spec("theme_free_mat_ocean_light", "Material Ocean Light", "Daylight Material 3 seafoam and deep navy container palette", ThemeCategory.MATERIAL, false, 0xFFF5FAFD, 0xFFE6F1F7, 0xFF006494, 0xFF247BA0, 0xFFCBE7F5, 0xFF0F1E26, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 24, "wp_free_gradient_nordic_ice"),
        Spec("theme_free_mat_sandstone", "Material Sandstone", "Warm neutral tonal theme with crisp bronze focus", ThemeCategory.MATERIAL, true, 0xFF161310, 0xFF241F1A, 0xFFE5C185, 0xFFC7B299, 0xFFF5E6CC, 0xFFF2ECE4, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 22, "wp_free_space_saturn_rings"),
        Spec("theme_free_mat_glacial", "Material Glacial", "High-clarity dark cyan Material 3 tonal system", ThemeCategory.MATERIAL, true, 0xFF0A1417, 0xFF142429, 0xFF67E8F9, 0xFFA5F3FC, 0xFFCCFBF1, 0xFFE6F4F6, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_free_nature_glacial_topo")
    )

    val items: List<ThemeConfig> = specs.mapIndexed { index, spec ->
        ThemeConfig(
            id = spec.id,
            name = spec.name,
            description = spec.description,
            category = spec.category,
            isPremium = false,
            isDarkTheme = spec.isDark,
            backgroundHex = spec.bg,
            surfaceHex = spec.surface,
            primaryHex = spec.primary,
            secondaryHex = spec.secondary,
            accentHex = spec.accent,
            textHex = spec.text,
            cardStyle = spec.cardStyle,
            iconStyle = spec.iconStyle,
            fontStyle = spec.fontStyle,
            cardRadiusDp = spec.radius,
            associatedWallpaperId = spec.wallpaperId,
            sortOrder = index
        )
    }
}
