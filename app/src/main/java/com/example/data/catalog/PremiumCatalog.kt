package com.example.data.catalog

import android.content.Context
import com.example.data.model.CatalogManifest
import com.example.data.model.ProceduralStyle
import com.example.data.model.ThemeCardStyle
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeFontStyle
import com.example.data.model.ThemeIconStyle
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperSourceType
import kotlinx.serialization.json.Json

object PremiumCatalog {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadManifest(context: Context): CatalogManifest? {
        return runCatching {
            context.assets.open("catalog_manifest.json").bufferedReader().use { reader ->
                json.decodeFromString<CatalogManifest>(reader.readText())
            }
        }.getOrNull()
    }

    private data class WallpaperSpec(
        val id: String,
        val name: String,
        val category: WallpaperCategory,
        val collection: String,
        val style: ProceduralStyle,
        val palette: List<Long>,
        val tags: List<String>
    )

    private val premiumWallpaperSpecs: List<WallpaperSpec> = listOf(
        // AMOLED Collection (10)
        WallpaperSpec("wp_pro_amoled_singularity", "Black Hole Singularity", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF000000, 0xFF0E1B33, 0xFF60A5FA, 0xFFF8FAFC), listOf("pro", "amoled", "singularity", "space")),
        WallpaperSpec("wp_pro_amoled_gilded_ring", "Gilded Chronometer", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF000000, 0xFF1F1606, 0xFFD4AF37, 0xFFFEF3C7), listOf("pro", "amoled", "gold", "luxury")),
        WallpaperSpec("wp_pro_amoled_obsidian_rib", "Obsidian Silk Fold", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFF000000, 0xFF181A20, 0xFF475569, 0xFFE2E8F0), listOf("pro", "amoled", "silk", "minimal")),
        WallpaperSpec("wp_pro_amoled_bioluminescent", "Abyssal Biolume", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF000000, 0xFF032129, 0xFF06B6D4, 0xFFA5F3FC), listOf("pro", "amoled", "cyan", "deep")),
        WallpaperSpec("wp_pro_amoled_scarlet_prism", "Scarlet Facet", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.PRISM_SHARDS, listOf(0xFF000000, 0xFF2B0712, 0xFFE11D48, 0xFFFECDD3), listOf("pro", "amoled", "prism", "red")),
        WallpaperSpec("wp_pro_amoled_carbon_arch", "Carbon Archway", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF000000, 0xFF14161A, 0xFF64748B, 0xFFCBD5E1), listOf("pro", "amoled", "architecture")),
        WallpaperSpec("wp_pro_amoled_titanium_contour", "Titanium Relief", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF000000, 0xFF171A21, 0xFF94A3B8, 0xFFF1F5F9), listOf("pro", "amoled", "contour")),
        WallpaperSpec("wp_pro_amoled_iris_nebula", "Iris Void Spiral", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.NEBULA_SPIRAL, listOf(0xFF000000, 0xFF1E1038, 0xFF8B5CF6, 0xFF38BDF8), listOf("pro", "amoled", "nebula")),
        WallpaperSpec("wp_pro_amoled_jade_grid", "Jade Telemetry", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.CYBER_GRID, listOf(0xFF000000, 0xFF052419, 0xFF10B981, 0xFF6EE7B7), listOf("pro", "amoled", "jade", "tech")),
        WallpaperSpec("wp_pro_amoled_platinum_glass", "Smoked Platinum Pane", WallpaperCategory.AMOLED, "AMOLED Collection", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF000000, 0xFF12141A, 0xFF64748B, 0xFFE2E8F0), listOf("pro", "amoled", "glass")),

        // Cinematic Collection (8)
        WallpaperSpec("wp_pro_cine_blade_dusk", "Noir Rain Horizon", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF070C14, 0xFF182B49, 0xFF0EA5E9, 0xFFF97316), listOf("pro", "cinematic", "noir", "skyline")),
        WallpaperSpec("wp_pro_cine_dune_eclipse", "Arrakis Solar Disc", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF170D08, 0xFF5C2D14, 0xFFD97706, 0xFFFDE68A), listOf("pro", "cinematic", "dune", "eclipse")),
        WallpaperSpec("wp_pro_cine_interstellar_orbit", "Gargantua Halo", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF050508, 0xFF2E1E12, 0xFFF59E0B, 0xFFFFFBEB), listOf("pro", "cinematic", "orbit", "space")),
        WallpaperSpec("wp_pro_cine_iceland_storm", "Basalt Stormfront", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF090E14, 0xFF1F2D3D, 0xFF64748B, 0xFFCBD5E1), listOf("pro", "cinematic", "basalt", "mountains")),
        WallpaperSpec("wp_pro_cine_tokyo_midnight", "Shibuya Glass Rain", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF080A14, 0xFF1E1B4B, 0xFFEC4899, 0xFF22D3EE), listOf("pro", "cinematic", "tokyo", "glass")),
        WallpaperSpec("wp_pro_cine_amber_fog", "Golden Hour Monolith", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF18110B, 0xFF422C1B, 0xFFD4AF37, 0xFFFEF3C7), listOf("pro", "cinematic", "monolith")),
        WallpaperSpec("wp_pro_cine_arctic_expedition", "Polar Ice Shelf", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF06131F, 0xFF0E3454, 0xFF38BDF8, 0xFFE0F2FE), listOf("pro", "cinematic", "arctic")),
        WallpaperSpec("wp_pro_cine_velvet_curtain", "Velvet Stage Ribbon", WallpaperCategory.CINEMATIC, "Cinematic Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF1A070F, 0xFF58102D, 0xFFF43F5E, 0xFFFECDD3), listOf("pro", "cinematic", "velvet")),

        // Luxury Collection (8)
        WallpaperSpec("wp_pro_lux_geneva_tourbillon", "Geneva Tourbillon", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF0B0C10, 0xFF1F242D, 0xFFD4AF37, 0xFFFDF2D0), listOf("pro", "luxury", "horology", "gold")),
        WallpaperSpec("wp_pro_lux_carrara_vein", "Carrara Silk Marble", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFFF6F4F0, 0xFFE6E0D6, 0xFFB8A382, 0xFF3D362D), listOf("pro", "luxury", "marble", "light")),
        WallpaperSpec("wp_pro_lux_noir_atelier", "Atelier Cashmere", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFF0A0908, 0xFF241E19, 0xFFC8A97E, 0xFFF5EBD8), listOf("pro", "luxury", "noir", "champagne")),
        WallpaperSpec("wp_pro_lux_emerald_velvet", "Mayfair Emerald Silk", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF05140E, 0xFF0C3827, 0xFF10B981, 0xFFD4AF37), listOf("pro", "luxury", "emerald", "gold")),
        WallpaperSpec("wp_pro_lux_sapphire_cut", "Baguette Sapphire", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.PRISM_SHARDS, listOf(0xFF060B1A, 0xFF172554, 0xFF3B82F6, 0xFFE2E8F0), listOf("pro", "luxury", "sapphire", "gem")),
        WallpaperSpec("wp_pro_lux_rose_horology", "Rose Gold Dial", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF140D0E, 0xFF361F23, 0xFFE29578, 0xFFFFDDD2), listOf("pro", "luxury", "rosegold")),
        WallpaperSpec("wp_pro_lux_palazzo_arch", "Milanese Marble Arch", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF171411, 0xFF332B24, 0xFFD4AF37, 0xFFFAF3E6), listOf("pro", "luxury", "milan", "arch")),
        WallpaperSpec("wp_pro_lux_obsidian_facets", "Onyx Cut Facets", WallpaperCategory.LUXURY, "Luxury Collection", ProceduralStyle.PRISM_SHARDS, listOf(0xFF060608, 0xFF18181D, 0xFF52525B, 0xFFE4E4E7), listOf("pro", "luxury", "onyx", "dark")),

        // Abstract Collection (8)
        WallpaperSpec("wp_pro_abs_mercury_drop", "Liquid Mercury Chrome", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFF0A0C10, 0xFF272C36, 0xFF94A3B8, 0xFFF8FAFC), listOf("pro", "abstract", "chrome")),
        WallpaperSpec("wp_pro_abs_cobalt_refraction", "Refracted Sapphire Glass", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF060D21, 0xFF1E3A8A, 0xFF38BDF8, 0xFFE0F2FE), listOf("pro", "abstract", "glass")),
        WallpaperSpec("wp_pro_abs_ochre_contours", "Ochre Terrain Flow", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF17110B, 0xFF47301A, 0xFFD97706, 0xFFFDE68A), listOf("pro", "abstract", "ochre")),
        WallpaperSpec("wp_pro_abs_iridescent_foil", "Iridescent Foil Prism", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.PRISM_SHARDS, listOf(0xFF0D0E17, 0xFF312E81, 0xFF2DD4BF, 0xFFF472B6), listOf("pro", "abstract", "iridescent")),
        WallpaperSpec("wp_pro_abs_indigo_ribbon", "Cobalt Silk Current", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF060B1E, 0xFF1E40AF, 0xFF60A5FA, 0xFFDBEAFE), listOf("pro", "abstract", "ribbon")),
        WallpaperSpec("wp_pro_abs_graphite_mosaic", "Graphite Glass Grid", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF0B0D12, 0xFF1F2430, 0xFF475569, 0xFFCBD5E1), listOf("pro", "abstract", "mosaic")),
        WallpaperSpec("wp_pro_abs_vermilion_fold", "Vermilion Silk Fold", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFF1A0708, 0xFF691218, 0xFFEF4444, 0xFFFECACA), listOf("pro", "abstract", "vermilion")),
        WallpaperSpec("wp_pro_abs_opal_halo", "Opalescent Mist Ring", WallpaperCategory.ABSTRACT, "Abstract Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF12141E, 0xFF2D3250, 0xFFA5B4FC, 0xFFF5F3FF), listOf("pro", "abstract", "opal")),

        // Nature Collection (7)
        WallpaperSpec("wp_pro_nat_patagonia_granite", "Patagonia Spire Dusk", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF0B0F1A, 0xFF26334D, 0xFF9D4EDD, 0xFFFFB5A7), listOf("pro", "nature", "patagonia")),
        WallpaperSpec("wp_pro_nat_boreal_fjord", "Svalbard Ice Aurora", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF040E14, 0xFF0A3642, 0xFF10B981, 0xFF38BDF8), listOf("pro", "nature", "aurora")),
        WallpaperSpec("wp_pro_nat_dolomites_mist", "Dolomite Limestone Mist", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF11161C, 0xFF2D3A47, 0xFF8D99AE, 0xFFEDF2F4), listOf("pro", "nature", "dolomites")),
        WallpaperSpec("wp_pro_nat_redwood_canopy", "Redwood Fog Elevation", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF08140E, 0xFF173626, 0xFF40916C, 0xFFB7E4C7), listOf("pro", "nature", "forest")),
        WallpaperSpec("wp_pro_nat_pacific_swell", "Midnight Pacific Swell", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.LIQUID_WAVES, listOf(0xFF040F1C, 0xFF0B2F4A, 0xFF0284C7, 0xFFBAE6FD), listOf("pro", "nature", "ocean")),
        WallpaperSpec("wp_pro_nat_namib_ridge", "Namib Crimson Ridge", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF1C0C08, 0xFF6E2614, 0xFFEA580C, 0xFFFED7AA), listOf("pro", "nature", "desert")),
        WallpaperSpec("wp_pro_nat_bamboo_shadow", "Arashiyama Morning", WallpaperCategory.NATURE, "Nature Collection", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF0A140F, 0xFF1F3B2B, 0xFF74C69D, 0xFFD8F3DC), listOf("pro", "nature", "bamboo")),

        // Space Collection (7)
        WallpaperSpec("wp_pro_spc_carina_pillars", "Carina Ion Pillars", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.NEBULA_SPIRAL, listOf(0xFF04060E, 0xFF1E293B, 0xFF0EA5E9, 0xFFF59E0B), listOf("pro", "space", "carina")),
        WallpaperSpec("wp_pro_spc_magnetar_wave", "Magnetar Pulse Field", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF030510, 0xFF1E1B4B, 0xFF22D3EE, 0xFFE0F2FE), listOf("pro", "space", "magnetar")),
        WallpaperSpec("wp_pro_spc_titan_atmosphere", "Titan Amber Haze", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF0F0A05, 0xFF45270B, 0xFFF59E0B, 0xFFFEF3C7), listOf("pro", "space", "titan")),
        WallpaperSpec("wp_pro_spc_kepler_transit", "Kepler Exoplanet Transit", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF040712, 0xFF172554, 0xFF38BDF8, 0xFFFDF2F8), listOf("pro", "space", "exoplanet")),
        WallpaperSpec("wp_pro_spc_helix_cloud", "Helix Core Filaments", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.NEBULA_SPIRAL, listOf(0xFF080512, 0xFF3B0764, 0xFFEC4899, 0xFF38BDF8), listOf("pro", "space", "helix")),
        WallpaperSpec("wp_pro_spc_europa_fracture", "Europa Ice Lineaments", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF07131E, 0xFF1B3A54, 0xFF7DD3FC, 0xFFF0F9FF), listOf("pro", "space", "europa")),
        WallpaperSpec("wp_pro_spc_deep_field_grid", "Relativistic Star Grid", WallpaperCategory.SPACE, "Space Collection", ProceduralStyle.CYBER_GRID, listOf(0xFF03040A, 0xFF111827, 0xFF818CF8, 0xFFE0E7FF), listOf("pro", "space", "deepfield")),

        // Minimal & Exclusive Collection (12)
        WallpaperSpec("wp_pro_min_leica_matte", "Rangefinder Matte Black", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF0C0D10, 0xFF1B1D24, 0xFFE11D48, 0xFFE2E8F0), listOf("pro", "minimal", "camera")),
        WallpaperSpec("wp_pro_min_dieter_grid", "Braun Precision Grid", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.BAUHAUS_MONO, listOf(0xFFECEBE6, 0xFFD9D6CE, 0xFF27272A, 0xFFEA580C), listOf("pro", "minimal", "industrial", "light")),
        WallpaperSpec("wp_pro_min_ceramic_white", "Ceramic Studio White", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFFF8FAFC, 0xFFE2E8F0, 0xFF64748B, 0xFF0F172A), listOf("pro", "minimal", "ceramic", "light")),
        WallpaperSpec("wp_pro_min_slate_arch", "Nordic Concrete Void", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF12151B, 0xFF222834, 0xFF94A3B8, 0xFFF1F5F9), listOf("pro", "minimal", "concrete")),
        WallpaperSpec("wp_pro_min_silk_contour", "Minimal Silk Contours", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF101218, 0xFF1E222D, 0xFF64748B, 0xFFCBD5E1), listOf("pro", "minimal", "contour")),
        WallpaperSpec("wp_pro_min_cobalt_dot", "Cobalt Axis Minimal", WallpaperCategory.MINIMAL, "Minimal Collection", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF0C101A, 0xFF1B2336, 0xFF3B82F6, 0xFFEFF6FF), listOf("pro", "minimal", "cobalt")),
        WallpaperSpec("wp_pro_exc_spinner_signature", "Spinner Atelier Signature", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF05070D, 0xFF122340, 0xFF38BDF8, 0xFFE2E8F0), listOf("pro", "exclusive", "spinner")),
        WallpaperSpec("wp_pro_exc_kinetic_eclipse", "Kinetic Platinum Eclipse", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF06070A, 0xFF1B202C, 0xFF94A3B8, 0xFF38BDF8), listOf("pro", "exclusive", "eclipse")),
        WallpaperSpec("wp_pro_exc_chronos_lattice", "Chronos Quantum Mesh", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.CYBER_GRID, listOf(0xFF060A12, 0xFF13263E, 0xFF06B6D4, 0xFFA855F7), listOf("pro", "exclusive", "chronos")),
        WallpaperSpec("wp_pro_exc_atelier_gold_prism", "Atelier Gold Refraction", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.PRISM_SHARDS, listOf(0xFF0B0A08, 0xFF292112, 0xFFD4AF37, 0xFFFEF3C7), listOf("pro", "exclusive", "gold")),
        WallpaperSpec("wp_pro_exc_aurora_obsidian", "Obsidian Aurora Silk", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF04060A, 0xFF0E2A3A, 0xFF2DD4BF, 0xFF818CF8), listOf("pro", "exclusive", "aurora")),
        WallpaperSpec("wp_pro_exc_alpine_monolith", "Glacier Monolith Peak", WallpaperCategory.EXCLUSIVE, "Exclusive Collection", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF070C14, 0xFF18293D, 0xFF64748B, 0xFFE2E8F0), listOf("pro", "exclusive", "alpine"))
    )

    val wallpaperItems: List<WallpaperItem> = premiumWallpaperSpecs.mapIndexed { index, spec ->
        WallpaperItem(
            id = spec.id,
            name = spec.name,
            category = spec.category,
            collection = spec.collection,
            isPremium = true,
            resolution = "2160 x 3840",
            fileSizeLabel = "4.8 MB",
            tags = spec.tags,
            license = "Android Launcher Pro Studio License",
            sourceType = WallpaperSourceType.PROCEDURAL_STUDIO,
            drawableResName = null,
            paletteHexes = spec.palette,
            proceduralStyle = spec.style,
            seed = (index + 101) * 104729L + spec.id.hashCode().toLong(),
            sortOrder = 100 + index
        )
    }

    private data class ThemeSpec(
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

    private val premiumThemeSpecs: List<ThemeSpec> = listOf(
        // Luxury (12)
        ThemeSpec("theme_pro_geneva_gold", "Geneva Horology", "Brushed champagne gold dial tokens over obsidian black", ThemeCategory.LUXURY, true, 0xFF060709, 0xFF141311, 0xFFD4AF37, 0xFFE5C158, 0xFFFEF3C7, 0xFFFAF8F5, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 22, "wp_pro_lux_geneva_tourbillon"),
        ThemeSpec("theme_pro_carrara_atelier", "Carrara Atelier", "Warm sunlit Italian marble light mode with bronze edges", ThemeCategory.LUXURY, false, 0xFFF7F5F0, 0xFFFFFFFF, 0xFF785A3C, 0xFFB8A382, 0xFF3D362D, 0xFF1F1B17, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 18, "wp_pro_lux_carrara_vein"),
        ThemeSpec("theme_pro_mayfair_emerald", "Mayfair Emerald", "British racing emerald surfaces trimmed with brushed brass", ThemeCategory.LUXURY, true, 0xFF05110C, 0xFF0D241B, 0xFF10B981, 0xFFD4AF37, 0xFFA7F3D0, 0xFFECFDF5, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 20, "wp_pro_lux_emerald_velvet"),
        ThemeSpec("theme_pro_savile_cashmere", "Savile Cashmere", "Tailored warm charcoal wool surfaces with bone typography", ThemeCategory.LUXURY, true, 0xFF0E0C0A, 0xFF1B1814, 0xFFC8A97E, 0xFFA68A64, 0xFFF5EBD8, 0xFFFAF6F0, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLAYFAIR_DISPLAY, 16, "wp_pro_lux_noir_atelier"),
        ThemeSpec("theme_pro_rose_horology", "Rose Chronometer", "Deep espresso plum paired with warm 18k rose-gold accents", ThemeCategory.LUXURY, true, 0xFF120B0D, 0xFF211518, 0xFFE29578, 0xFFF4A261, 0xFFFFDDD2, 0xFFFFF5F2, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_lux_rose_horology"),
        ThemeSpec("theme_pro_sapphire_vault", "Sapphire Vault", "Faceted royal midnight sapphire with ice-platinum borders", ThemeCategory.LUXURY, true, 0xFF060A17, 0xFF101933, 0xFF60A5FA, 0xFF3B82F6, 0xFFE2E8F0, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.HEX_TECH, ThemeFontStyle.SPACE_GROTESK, 16, "wp_pro_lux_sapphire_cut"),
        ThemeSpec("theme_pro_palazzo_milan", "Palazzo Milan", "Architectural limestone dark bronze with editorial serifs", ThemeCategory.LUXURY, true, 0xFF13100D, 0xFF211C17, 0xFFD4AF37, 0xFFB08968, 0xFFFAF3E6, 0xFFFAF7F2, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 14, "wp_pro_lux_palazzo_arch"),
        ThemeSpec("theme_pro_onyx_facet", "Black Onyx Facet", "Pure matte black faceted luxury UI with razor silver lines", ThemeCategory.LUXURY, true, 0xFF040406, 0xFF121216, 0xFFE4E4E7, 0xFFA1A1AA, 0xFFFFFFFF, 0xFFFAFAFA, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.OUTFIT, 12, "wp_pro_lux_obsidian_facets"),
        ThemeSpec("theme_pro_champagne_light", "Champagne Silk Light", "Luminous pearl white cards with subtle champagne borders", ThemeCategory.LUXURY, false, 0xFFFAF7F2, 0xFFFFFFFF, 0xFF9C7A3C, 0xFFD4AF37, 0xFF4A3B22, 0xFF1E1914, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 22, "wp_pro_lux_carrara_vein"),
        ThemeSpec("theme_pro_amethyst_salon", "Amethyst Salon", "Deep twilight plum glass panels with lilac-pearl highlights", ThemeCategory.LUXURY, true, 0xFF0E0916, 0xFF1A1229, 0xFFC084FC, 0xFFF472B6, 0xFFF3E8FF, 0xFFFAF5FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_amoled_iris_nebula"),
        ThemeSpec("theme_pro_platinum_louvre", "Louvre Platinum", "Cool gallery slate surfaces with museum-grade contrast", ThemeCategory.LUXURY, true, 0xFF0C0E12, 0xFF181C24, 0xFFE2E8F0, 0xFF94A3B8, 0xFF38BDF8, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 18, "wp_pro_amoled_platinum_glass"),
        ThemeSpec("theme_pro_imperial_lacquer", "Imperial Lacquer", "Deep oxblood lacquer dark mode with warm gold trim", ThemeCategory.LUXURY, true, 0xFF140709, 0xFF240F13, 0xFFE11D48, 0xFFD4AF37, 0xFFFECDD3, 0xFFFFF1F2, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 18, "wp_pro_amoled_scarlet_prism"),

        // Futuristic (12)
        ThemeSpec("theme_pro_fut_singularity", "Singularity Zero", "Event-horizon black interface with hyper-crisp azure arcs", ThemeCategory.FUTURISTIC, true, 0xFF000000, 0xFF0B1220, 0xFF38BDF8, 0xFF818CF8, 0xFFE0F2FE, 0xFFF8FAFC, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 24, "wp_pro_amoled_singularity"),
        ThemeSpec("theme_pro_fut_quantum_hud", "Quantum HUD", "Technical aerospace telemetry interface in cyan & slate", ThemeCategory.FUTURISTIC, true, 0xFF050A12, 0xFF0D1726, 0xFF06B6D4, 0xFF22D3EE, 0xFFA5F3FC, 0xFFECFEFF, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 10, "wp_pro_exc_chronos_lattice"),
        ThemeSpec("theme_pro_fut_cyber_jade", "Cyber Jade Matrix", "Precision emerald-cyan telemetry grid on true AMOLED black", ThemeCategory.FUTURISTIC, true, 0xFF000000, 0xFF071A14, 0xFF10B981, 0xFF2DD4BF, 0xFFA7F3D0, 0xFFECFDF5, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 12, "wp_pro_amoled_jade_grid"),
        ThemeSpec("theme_pro_fut_orbital_glass", "Orbital Glass OS", "Translucent multi-layer frosted panels with electric sky", ThemeCategory.FUTURISTIC, true, 0xFF070B14, 0xFF121A2B, 0xFF60A5FA, 0xFF38BDF8, 0xFFBAE6FD, 0xFFF1F5F9, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 24, "wp_pro_exc_spinner_signature"),
        ThemeSpec("theme_pro_fut_magnetar", "Magnetar Pulse", "Deep cosmic cobalt with high-energy ice-cyan accents", ThemeCategory.FUTURISTIC, true, 0xFF040714, 0xFF0E142B, 0xFF22D3EE, 0xFF818CF8, 0xFFCFFAFE, 0xFFF0F9FF, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_pro_spc_magnetar_wave"),
        ThemeSpec("theme_pro_fut_titanium_os", "Titanium Core OS", "Aerospace alloy gray-blue cards with laser white highlights", ThemeCategory.FUTURISTIC, true, 0xFF0A0D12, 0xFF161B24, 0xFFCBD5E1, 0xFF38BDF8, 0xFFFFFFFF, 0xFFF8FAFC, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 14, "wp_pro_abs_mercury_drop"),
        ThemeSpec("theme_pro_fut_shibuya_glass", "Neo Shibuya Glass", "Midnight glassmorphism with restrained magenta-cyan rim", ThemeCategory.FUTURISTIC, true, 0xFF080914, 0xFF13152A, 0xFFEC4899, 0xFF22D3EE, 0xFFFBCFE8, 0xFFFDF2F8, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 20, "wp_pro_cine_tokyo_midnight"),
        ThemeSpec("theme_pro_fut_relativity", "Relativity Grid", "Deep star-chart indigo with crisp indigo-white lines", ThemeCategory.FUTURISTIC, true, 0xFF04060E, 0xFF0F1324, 0xFF818CF8, 0xFFA5B4FC, 0xFFE0E7FF, 0xFFEEF2FF, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 12, "wp_pro_spc_deep_field_grid"),
        ThemeSpec("theme_pro_fut_exoplanet", "Kepler Transit", "Deep space horizon blue with luminous silver-cyan halo", ThemeCategory.FUTURISTIC, true, 0xFF050812, 0xFF10192E, 0xFF38BDF8, 0xFF60A5FA, 0xFFE0F2FE, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_pro_spc_kepler_transit"),
        ThemeSpec("theme_pro_fut_titan_haze", "Titan Telemetry", "Warm spacecraft amber over obsidian cockpit panels", ThemeCategory.FUTURISTIC, true, 0xFF0D0905, 0xFF1C140C, 0xFFF59E0B, 0xFFFB923C, 0xFFFEF3C7, 0xFFFFFBEB, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.HEX_TECH, ThemeFontStyle.JETBRAINS_MONO, 12, "wp_pro_spc_titan_atmosphere"),
        ThemeSpec("theme_pro_fut_abyssal_sonar", "Abyssal Sonar", "Deep trench black-teal with sonar cyan rings", ThemeCategory.FUTURISTIC, true, 0xFF020B0E, 0xFF071B22, 0xFF06B6D4, 0xFF14B8A6, 0xFFCCFBF1, 0xFFECFEFF, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 24, "wp_pro_amoled_bioluminescent"),
        ThemeSpec("theme_pro_fut_kinetic_eclipse", "Kinetic Eclipse", "Signature dark chrome theme engineered for Spinner Mode", ThemeCategory.FUTURISTIC, true, 0xFF06080D, 0xFF121621, 0xFF38BDF8, 0xFF94A3B8, 0xFFE2E8F0, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_pro_exc_kinetic_eclipse"),

        // AMOLED & Dark Pro (12)
        ThemeSpec("theme_pro_amoled_gilded", "AMOLED Gilded Arc", "True black OLED battery-saver with warm gold rings", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF110E08, 0xFFD4AF37, 0xFFF59E0B, 0xFFFEF3C7, 0xFFFAF8F5, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_pro_amoled_gilded_ring"),
        ThemeSpec("theme_pro_amoled_obsidian_silk", "AMOLED Obsidian Silk", "Pure black base with smoked slate glassmorphism", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF12141A, 0xFF94A3B8, 0xFF64748B, 0xFFF1F5F9, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_pro_amoled_obsidian_rib"),
        ThemeSpec("theme_pro_amoled_carbon_arch", "AMOLED Carbon Arch", "Pitch black architecture with minimalist steel outlines", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF111317, 0xFFCBD5E1, 0xFF475569, 0xFFFFFFFF, 0xFFF8FAFC, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 14, "wp_pro_amoled_carbon_arch"),
        ThemeSpec("theme_pro_amoled_titanium_relief", "AMOLED Contour", "Pure OLED black with subtle silver topographic accents", ThemeCategory.AMOLED, true, 0xFF000000, 0xFF10131A, 0xFF94A3B8, 0xFF38BDF8, 0xFFE2E8F0, 0xFFF1F5F9, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 18, "wp_pro_amoled_titanium_contour"),
        ThemeSpec("theme_pro_dark_gargantua", "Gargantua Horizon", "Cinematic dark amber-black accretion disc styling", ThemeCategory.DARK, true, 0xFF070608, 0xFF171312, 0xFFF59E0B, 0xFFD97706, 0xFFFFFBEB, 0xFFFAF5F0, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_pro_cine_interstellar_orbit"),
        ThemeSpec("theme_pro_dark_basalt_iceland", "Icelandic Basalt", "Volcanic charcoal slate with cool glacial mist text", ThemeCategory.DARK, true, 0xFF0B0E12, 0xFF171E26, 0xFF94A3B8, 0xFF64748B, 0xFFE2E8F0, 0xFFF1F5F9, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 16, "wp_pro_cine_iceland_storm"),
        ThemeSpec("theme_pro_dark_arrakis", "Arrakis Spice Dusk", "Deep desert night obsidian with warm spice-amber highlights", ThemeCategory.DARK, true, 0xFF120B07, 0xFF21150E, 0xFFD97706, 0xFFF59E0B, 0xFFFDE68A, 0xFFFFFBEB, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 20, "wp_pro_cine_dune_eclipse"),
        ThemeSpec("theme_pro_dark_velvet_stage", "Crimson Velvet", "Deep burgundy-black editorial surfaces with rose highlights", ThemeCategory.DARK, true, 0xFF14070D, 0xFF240E18, 0xFFF43F5E, 0xFFFB7185, 0xFFFECDD3, 0xFFFFF1F2, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 22, "wp_pro_cine_velvet_curtain"),
        ThemeSpec("theme_pro_dark_vermilion", "Vermilion Studio", "Charcoal silk backdrop with bold architectural vermilion", ThemeCategory.DARK, true, 0xFF120808, 0xFF211011, 0xFFEF4444, 0xFFF87171, 0xFFFECACA, 0xFFFEF2F2, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.SPACE_GROTESK, 16, "wp_pro_abs_vermilion_fold"),
        ThemeSpec("theme_pro_dark_ochre", "Ochre Atelier", "Earth-toned dark umber surfaces with golden ochre accents", ThemeCategory.DARK, true, 0xFF140F0A, 0xFF241B12, 0xFFD97706, 0xFFB45309, 0xFFFDE68A, 0xFFFFFBEB, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.OUTFIT, 16, "wp_pro_abs_ochre_contours"),
        ThemeSpec("theme_pro_dark_blade_rain", "Noir Rain Slate", "Cinematic rainy midnight blue with neon-amber contrast", ThemeCategory.DARK, true, 0xFF070C14, 0xFF111C2E, 0xFF0EA5E9, 0xFFF97316, 0xFFBAE6FD, 0xFFF0F9FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.HEX_TECH, ThemeFontStyle.SPACE_GROTESK, 14, "wp_pro_cine_blade_dusk"),
        ThemeSpec("theme_pro_dark_opal_mist", "Opal Mist", "Soft twilight indigo-gray glass with lavender-pearl glow", ThemeCategory.DARK, true, 0xFF10121C, 0xFF1C2033, 0xFFA5B4FC, 0xFF818CF8, 0xFFF5F3FF, 0xFFEEF2FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_abs_opal_halo"),

        // Minimal, Nature, Gradient, Exclusive (24)
        ThemeSpec("theme_pro_min_leica_m", "Leica Matte M", "Minimalist camera-inspired matte black with crimson dot", ThemeCategory.MINIMAL, true, 0xFF0C0D10, 0xFF181A20, 0xFFE2E8F0, 0xFFE11D48, 0xFFF8FAFC, 0xFFF1F5F9, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 12, "wp_pro_min_leica_matte"),
        ThemeSpec("theme_pro_min_braun_studio", "Dieter Industrial", "Warm Bauhaus stone-white daylight industrial design", ThemeCategory.MINIMAL, false, 0xFFECEBE6, 0xFFF9F8F5, 0xFF27272A, 0xFFEA580C, 0xFF18181B, 0xFF18181B, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.SPACE_GROTESK, 8, "wp_pro_min_dieter_grid"),
        ThemeSpec("theme_pro_min_ceramic", "Ceramic Gallery", "Pure daylight porcelain white with razor-sharp slate type", ThemeCategory.MINIMAL, false, 0xFFF8FAFC, 0xFFFFFFFF, 0xFF0F172A, 0xFF64748B, 0xFF0284C7, 0xFF0F172A, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_pro_min_ceramic_white"),
        ThemeSpec("theme_pro_min_nordic_void", "Nordic Concrete", "Architectural cool gray-black minimal hierarchy", ThemeCategory.MINIMAL, true, 0xFF11141A, 0xFF1C212B, 0xFF94A3B8, 0xFF64748B, 0xFFF1F5F9, 0xFFF8FAFC, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 12, "wp_pro_min_slate_arch"),
        ThemeSpec("theme_pro_min_cobalt_axis", "Cobalt Axis", "Deep navy minimal UI with a single cobalt primary anchor", ThemeCategory.MINIMAL, true, 0xFF0B0F19, 0xFF151D30, 0xFF3B82F6, 0xFF60A5FA, 0xFFDBEAFE, 0xFFF8FAFC, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 16, "wp_pro_min_cobalt_dot"),
        ThemeSpec("theme_pro_min_silk_lines", "Silk Contour", "Dark graphite minimal theme with soft ash hairline borders", ThemeCategory.MINIMAL, true, 0xFF0F1117, 0xFF191D26, 0xFFCBD5E1, 0xFF475569, 0xFFF8FAFC, 0xFFE2E8F0, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.CIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_pro_min_silk_contour"),
        ThemeSpec("theme_pro_nat_patagonia", "Patagonia Spire", "Twilight granite slate with alpine violet-rose accents", ThemeCategory.NATURE, true, 0xFF0B0E17, 0xFF171E2E, 0xFFA855F7, 0xFFFFB5A7, 0xFFF3E8FF, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 20, "wp_pro_nat_patagonia_granite"),
        ThemeSpec("theme_pro_nat_svalbard", "Svalbard Aurora", "Polar dark ocean with luminous aurora emerald-cyan", ThemeCategory.NATURE, true, 0xFF040D12, 0xFF0B2029, 0xFF10B981, 0xFF38BDF8, 0xFFD1FAE5, 0xFFECFDF5, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 22, "wp_pro_nat_boreal_fjord"),
        ThemeSpec("theme_pro_nat_dolomites", "Dolomite Limestone", "Cool mountain mist slate with crisp alabaster highlights", ThemeCategory.NATURE, true, 0xFF10151B, 0xFF1D2630, 0xFF94A3B8, 0xFF8D99AE, 0xFFEDF2F4, 0xFFF8FAFC, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 18, "wp_pro_nat_dolomites_mist"),
        ThemeSpec("theme_pro_nat_redwood", "Redwood Canopy", "Deep mossy evergreen surfaces with sunlit fern highlights", ThemeCategory.NATURE, true, 0xFF08120D, 0xFF12261C, 0xFF40916C, 0xFF74C69D, 0xFFD8F3DC, 0xFFECFDF5, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_pro_nat_redwood_canopy"),
        ThemeSpec("theme_pro_nat_pacific", "Midnight Pacific", "Deep oceanic indigo-blue with foam aqua glass surfaces", ThemeCategory.NATURE, true, 0xFF040E1A, 0xFF0B2238, 0xFF0284C7, 0xFF38BDF8, 0xFFBAE6FD, 0xFFF0F9FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_nat_pacific_swell"),
        ThemeSpec("theme_pro_nat_arashiyama", "Arashiyama Bamboo", "Tranquil Kyoto bamboo grove greens on dark stone", ThemeCategory.NATURE, true, 0xFF0A120E, 0xFF15261D, 0xFF52B788, 0xFF74C69D, 0xFFD8F3DC, 0xFFF0FDF4, ThemeCardStyle.OUTLINED_MINIMAL, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 18, "wp_pro_nat_bamboo_shadow"),
        ThemeSpec("theme_pro_grad_iridescent", "Iridescent Prism", "Multi-spectrum teal-violet-rose iridescent glass edges", ThemeCategory.GRADIENT, true, 0xFF0B0C16, 0xFF16182B, 0xFF2DD4BF, 0xFFF472B6, 0xFFA5B4FC, 0xFFF8FAFC, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.SPACE_GROTESK, 22, "wp_pro_abs_iridescent_foil"),
        ThemeSpec("theme_pro_grad_cobalt_current", "Cobalt Current", "Flowing deep sapphire gradient panels with sky accents", ThemeCategory.GRADIENT, true, 0xFF060B1C, 0xFF0F1B3D, 0xFF3B82F6, 0xFF60A5FA, 0xFFDBEAFE, 0xFFEFF6FF, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_abs_indigo_ribbon"),
        ThemeSpec("theme_pro_grad_carina", "Carina Nebula", "Celestial blue-to-amber cosmic gradient theme", ThemeCategory.GRADIENT, true, 0xFF050812, 0xFF111A2E, 0xFF0EA5E9, 0xFFF59E0B, 0xFFBAE6FD, 0xFFF8FAFC, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.SPACE_GROTESK, 20, "wp_pro_spc_carina_pillars"),
        ThemeSpec("theme_pro_grad_helix", "Helix Spectrum", "Deep space magenta-to-cyan gradient glass cards", ThemeCategory.GRADIENT, true, 0xFF080512, 0xFF18102E, 0xFFEC4899, 0xFF38BDF8, 0xFFFBCFE8, 0xFFFDF2F8, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_spc_helix_cloud"),
        ThemeSpec("theme_pro_grad_europa", "Europa Glacial", "Icy sapphire-to-frost gradient with crisp geometric type", ThemeCategory.GRADIENT, true, 0xFF07121C, 0xFF112638, 0xFF38BDF8, 0xFF7DD3FC, 0xFFE0F2FE, 0xFFF0F9FF, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 20, "wp_pro_spc_europa_fracture"),
        ThemeSpec("theme_pro_grad_arctic_light", "Glacier Daylight", "Luminous sky-white gradient light mode for high clarity", ThemeCategory.GRADIENT, false, 0xFFF0F9FF, 0xFFFFFFFF, 0xFF0284C7, 0xFF06B6D4, 0xFF0C4A6E, 0xFF0F172A, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.CIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_pro_cine_arctic_expedition"),
        ThemeSpec("theme_pro_exc_atelier_pro", "Atelier Pro Gold", "Flagship Pro studio theme with obsidian glass and gold", ThemeCategory.EXCLUSIVE, true, 0xFF070709, 0xFF141310, 0xFFD4AF37, 0xFFE5C158, 0xFFFEF3C7, 0xFFFAF8F5, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLAYFAIR_DISPLAY, 22, "wp_pro_exc_atelier_gold_prism"),
        ThemeSpec("theme_pro_exc_aurora_obsidian", "Obsidian Aurora Pro", "Flagship dark glass theme with teal-indigo aurora edges", ThemeCategory.EXCLUSIVE, true, 0xFF04060A, 0xFF0D1924, 0xFF2DD4BF, 0xFF818CF8, 0xFFCCFBF1, 0xFFF0FDFA, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 24, "wp_pro_exc_aurora_obsidian"),
        ThemeSpec("theme_pro_exc_alpine_monolith", "Alpine Monolith Pro", "Flagship slate architectural theme with glacier contrast", ThemeCategory.EXCLUSIVE, true, 0xFF070B12, 0xFF121C2B, 0xFF94A3B8, 0xFF38BDF8, 0xFFE2E8F0, 0xFFF8FAFC, ThemeCardStyle.GLASSMORPHIC, ThemeIconStyle.ROUNDED_SQUARE, ThemeFontStyle.PLUS_JAKARTA_SANS, 18, "wp_pro_exc_alpine_monolith"),
        ThemeSpec("theme_pro_exc_material_onyx", "Material Onyx Pro", "Premium Material 3 tonal container theme in midnight slate", ThemeCategory.MATERIAL, true, 0xFF0B0E14, 0xFF161C28, 0xFF93C5FD, 0xFFA5B4FC, 0xFFE0E7FF, 0xFFF1F5F9, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.PLUS_JAKARTA_SANS, 24, "wp_pro_abs_cobalt_refraction"),
        ThemeSpec("theme_pro_exc_material_rose", "Material Rose Pro", "Refined Material 3 light blush-ivory tonal hierarchy", ThemeCategory.MATERIAL, false, 0xFFFFF5F7, 0xFFFCE8ED, 0xFF9D174D, 0xFFBE185D, 0xFF500724, 0xFF1F1217, ThemeCardStyle.SOLID_ELEVATED, ThemeIconStyle.SQUIRCLE, ThemeFontStyle.OUTFIT, 24, "wp_free_gradient_rose_quartz"),
        ThemeSpec("theme_pro_exc_spinner_pro", "Spinner Prime", "Official flagship Spinner Mode Pro theme in cyan-titanium", ThemeCategory.EXCLUSIVE, true, 0xFF05070C, 0xFF101624, 0xFF38BDF8, 0xFF0EA5E9, 0xFFE0F2FE, 0xFFF8FAFC, ThemeCardStyle.GRADIENT_BORDER, ThemeIconStyle.CIRCLE, ThemeFontStyle.SPACE_GROTESK, 24, "wp_pro_exc_spinner_signature")
    )

    val themeItems: List<ThemeConfig> = premiumThemeSpecs.mapIndexed { index, spec ->
        ThemeConfig(
            id = spec.id,
            name = spec.name,
            description = spec.description,
            category = spec.category,
            isPremium = true,
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
            sortOrder = 100 + index
        )
    }
}
