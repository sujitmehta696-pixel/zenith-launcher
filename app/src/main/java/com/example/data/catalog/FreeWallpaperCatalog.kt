package com.example.data.catalog

import com.example.data.model.ProceduralStyle
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperSourceType

object FreeWallpaperCatalog {

    private data class Spec(
        val id: String,
        val name: String,
        val category: WallpaperCategory,
        val collection: String,
        val style: ProceduralStyle,
        val palette: List<Long>,
        val tags: List<String>,
        val drawableResName: String? = null,
        val resolution: String = "1440 x 3200",
        val sizeLabel: String = "2.4 MB"
    )

    private val specs: List<Spec> = listOf(
        // 1-4: Bespoke Generated Raster Studio Artworks
        Spec(
            id = "wp_free_amoled_eclipse",
            name = "Obsidian Solar Eclipse",
            category = WallpaperCategory.AMOLED,
            collection = "AMOLED Obsidian",
            style = ProceduralStyle.ECLIPSE_HALO,
            palette = listOf(0xFF040508, 0xFF102A4C, 0xFF38BDF8, 0xFFE2E8F0),
            tags = listOf("amoled", "eclipse", "dark", "minimal", "signature"),
            drawableResName = "wp_amoled_eclipse",
            resolution = "1440 x 2560",
            sizeLabel = "3.8 MB"
        ),
        Spec(
            id = "wp_free_liquid_obsidian",
            name = "Sculpted Obsidian Glass",
            category = WallpaperCategory.ABSTRACT,
            collection = "Sculpted Abstract",
            style = ProceduralStyle.LIQUID_WAVES,
            palette = listOf(0xFF0A0B0F, 0xFF1F232C, 0xFFD4AF37, 0xFF94A3B8),
            tags = listOf("glass", "obsidian", "gold", "abstract", "luxury"),
            drawableResName = "wp_liquid_obsidian",
            resolution = "1440 x 2560",
            sizeLabel = "4.1 MB"
        ),
        Spec(
            id = "wp_free_alpine_twilight",
            name = "Alpine Blue Hour",
            category = WallpaperCategory.NATURE,
            collection = "Atmospheric Nature",
            style = ProceduralStyle.ALPINE_PEAKS,
            palette = listOf(0xFF0A1120, 0xFF1D3557, 0xFF457B9D, 0xFFA8DADC),
            tags = listOf("mountain", "lake", "twilight", "nature", "calm"),
            drawableResName = "wp_alpine_twilight",
            resolution = "1440 x 2560",
            sizeLabel = "3.6 MB"
        ),
        Spec(
            id = "wp_free_cosmic_nebula",
            name = "Orbital Stardust Spiral",
            category = WallpaperCategory.SPACE,
            collection = "Deep Orbital Space",
            style = ProceduralStyle.NEBULA_SPIRAL,
            palette = listOf(0xFF04050C, 0xFF1E1B4B, 0xFF38BDF8, 0xFFA855F7),
            tags = listOf("space", "nebula", "cosmos", "spiral", "dark"),
            drawableResName = "wp_cosmic_nebula",
            resolution = "1440 x 2560",
            sizeLabel = "3.9 MB"
        ),

        // 5-10: AMOLED (6 more)
        Spec("wp_free_amoled_void_ring", "Void Halo Cyan", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF000000, 0xFF071B26, 0xFF06B6D4, 0xFFCCFBF1), listOf("amoled", "black", "cyan", "ring")),
        Spec("wp_free_amoled_amber_pulse", "Nocturne Amber Arc", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF000000, 0xFF1F1205, 0xFFF59E0B, 0xFFFDE68A), listOf("amoled", "amber", "orbital", "minimal")),
        Spec("wp_free_amoled_crimson_horizon", "Crimson Event Horizon", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF000000, 0xFF28060E, 0xFFF43F5E, 0xFFFFE4E6), listOf("amoled", "crimson", "horizon")),
        Spec("wp_free_amoled_emerald_orbit", "Emerald Midnight Orbit", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF000000, 0xFF031F16, 0xFF10B981, 0xFFA7F3D0), listOf("amoled", "emerald", "orbit")),
        Spec("wp_free_amoled_silver_trace", "Pure Black Monolith", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF000000, 0xFF12141A, 0xFF64748B, 0xFFE2E8F0), listOf("amoled", "monolith", "silver")),
        Spec("wp_free_amoled_violet_fold", "Velvet Violet Wave", WallpaperCategory.AMOLED, "AMOLED Obsidian", ProceduralStyle.LIQUID_WAVES, listOf(0xFF000000, 0xFF1B0B2E, 0xFF8B5CF6, 0xFFDDD6FE), listOf("amoled", "violet", "wave")),

        // 11-15: Nature (5 more)
        Spec("wp_free_nature_nordic_fjord", "Nordic Fjord Mist", WallpaperCategory.NATURE, "Atmospheric Nature", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF0B161C, 0xFF1E3A40, 0xFF52796F, 0xFFCAD2C5), listOf("fjord", "mist", "mountains", "nature")),
        Spec("wp_free_nature_aurora_borealis", "Polar Aurora Curtain", WallpaperCategory.NATURE, "Atmospheric Nature", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF06121A, 0xFF0F3D3E, 0xFF2EC4B6, 0xFFCBF3F0), listOf("aurora", "northern", "lights", "nature")),
        Spec("wp_free_nature_sahara_dunes", "Amber Desert Dunes", WallpaperCategory.NATURE, "Atmospheric Nature", ProceduralStyle.LIQUID_WAVES, listOf(0xFF1E120A, 0xFF6B3E26, 0xFFD97706, 0xFFFDE68A), listOf("desert", "dunes", "warm", "nature")),
        Spec("wp_free_nature_emerald_ridge", "Cascadia Evergreen Ridge", WallpaperCategory.NATURE, "Atmospheric Nature", ProceduralStyle.ALPINE_PEAKS, listOf(0xFF071510, 0xFF14452F, 0xFF2D6A4F, 0xFF95D5B2), listOf("forest", "ridge", "green", "nature")),
        Spec("wp_free_nature_glacial_topo", "Glacial Contour Map", WallpaperCategory.NATURE, "Atmospheric Nature", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF0A192F, 0xFF172A45, 0xFF64FFDA, 0xFFCCD6F6), listOf("glacier", "topography", "ice", "nature")),

        // 16-20: Abstract (5 more)
        Spec("wp_free_abstract_cobalt_silk", "Cobalt Liquid Silk", WallpaperCategory.ABSTRACT, "Sculpted Abstract", ProceduralStyle.LIQUID_WAVES, listOf(0xFF060D21, 0xFF1E3A8A, 0xFF3B82F6, 0xFFDBEAFE), listOf("silk", "cobalt", "abstract", "flow")),
        Spec("wp_free_abstract_prism_refract", "Refracted Quartz Shards", WallpaperCategory.ABSTRACT, "Sculpted Abstract", ProceduralStyle.PRISM_SHARDS, listOf(0xFF0F1016, 0xFF312E81, 0xFF06B6D4, 0xFFF472B6), listOf("prism", "crystal", "shards", "abstract")),
        Spec("wp_free_abstract_cellular_frost", "Frosted Glass Matrix", WallpaperCategory.ABSTRACT, "Sculpted Abstract", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF111827, 0xFF374151, 0xFF9CA3AF, 0xFFF9FAFB), listOf("glass", "frosted", "abstract", "minimal")),
        Spec("wp_free_abstract_magenta_tide", "Nocturnal Magenta Tide", WallpaperCategory.ABSTRACT, "Sculpted Abstract", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF120514, 0xFF581C87, 0xFFEC4899, 0xFFFBCFE8), listOf("magenta", "tide", "abstract")),
        Spec("wp_free_abstract_terracotta_flow", "Sculpted Terracotta Fold", WallpaperCategory.ABSTRACT, "Sculpted Abstract", ProceduralStyle.LIQUID_WAVES, listOf(0xFF1A0E0A, 0xFF7C2D12, 0xFFEA580C, 0xFFFED7AA), listOf("terracotta", "sculpted", "warm")),

        // 21-26: Minimal (6)
        Spec("wp_free_minimal_bauhaus_slate", "Bauhaus Slate Geometry", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF111317, 0xFF272B33, 0xFFE2E8F0, 0xFF38BDF8), listOf("bauhaus", "minimal", "geometric")),
        Spec("wp_free_minimal_ivory_sand", "Warm Alabaster Paper", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.BAUHAUS_MONO, listOf(0xFFF4F1EA, 0xFFE5DFD3, 0xFF8C7A6B, 0xFF2B2623), listOf("light", "alabaster", "minimal", "clean")),
        Spec("wp_free_minimal_platinum_zen", "Platinum Zen Circle", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF12151C, 0xFF232834, 0xFFCBD5E1, 0xFFF8FAFC), listOf("zen", "circle", "platinum", "minimal")),
        Spec("wp_free_minimal_sage_stone", "Sage Pebble Minimal", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF18201C, 0xFF2D3A33, 0xFF84A98C, 0xFFCAD2C5), listOf("sage", "stone", "calm", "minimal")),
        Spec("wp_free_minimal_midnight_contour", "Midnight Fine Contour", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF0A0D14, 0xFF161C2B, 0xFF475569, 0xFF94A3B8), listOf("contour", "lines", "minimal", "dark")),
        Spec("wp_free_minimal_cloud_mist", "Porcelain Mist Light", WallpaperCategory.MINIMAL, "Architectural Minimal", ProceduralStyle.LIQUID_WAVES, listOf(0xFFF1F5F9, 0xFFE2E8F0, 0xFF94A3B8, 0xFF334155), listOf("light", "porcelain", "minimal", "soft")),

        // 27-32: Gradient (6)
        Spec("wp_free_gradient_twilight_indigo", "Indigo Twilight Mesh", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF090D1F, 0xFF312E81, 0xFF6366F1, 0xFF38BDF8), listOf("gradient", "indigo", "mesh")),
        Spec("wp_free_gradient_solstice_peach", "Solstice Peach Dusk", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.LIQUID_WAVES, listOf(0xFF1F122B, 0xFF9D174D, 0xFFF97316, 0xFFFDE047), listOf("gradient", "sunset", "peach")),
        Spec("wp_free_gradient_teal_lagoon", "Deep Lagoon Velvet", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF041C24, 0xFF0F766E, 0xFF14B8A6, 0xFF99F6E4), listOf("gradient", "teal", "lagoon")),
        Spec("wp_free_gradient_graphite_steel", "Brushed Titanium Fade", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF0B0D11, 0xFF1F2937, 0xFF4B5563, 0xFFD1D5DB), listOf("gradient", "titanium", "steel")),
        Spec("wp_free_gradient_rose_quartz", "Rose Quartz Silk", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.LIQUID_WAVES, listOf(0xFF231123, 0xFF831843, 0xFFF472B6, 0xFFFCE7F3), listOf("gradient", "rose", "quartz")),
        Spec("wp_free_gradient_nordic_ice", "Arctic Glass Wash", WallpaperCategory.GRADIENT, "Spectrum Gradients", ProceduralStyle.AURORA_RIBBONS, listOf(0xFFE0F2FE, 0xFFBAE6FD, 0xFF38BDF8, 0xFF0284C7), listOf("gradient", "ice", "light", "arctic")),

        // 33-37: Space (5 more)
        Spec("wp_free_space_saturn_rings", "Cassini Ring Shadow", WallpaperCategory.SPACE, "Deep Orbital Space", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF05070E, 0xFF1E293B, 0xFFD97706, 0xFFFDE68A), listOf("saturn", "rings", "space", "planet")),
        Spec("wp_free_space_pulsar_core", "Pulsar Beacon Core", WallpaperCategory.SPACE, "Deep Orbital Space", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF030712, 0xFF0C4A6E, 0xFF22D3EE, 0xFFECFEFF), listOf("pulsar", "star", "space", "blue")),
        Spec("wp_free_space_andromeda_drift", "Andromeda Drift", WallpaperCategory.SPACE, "Deep Orbital Space", ProceduralStyle.NEBULA_SPIRAL, listOf(0xFF080514, 0xFF3B0764, 0xFFC084FC, 0xFFF0ABFC), listOf("galaxy", "andromeda", "space", "purple")),
        Spec("wp_free_space_lunar_eclipse", "Lunar Umbra Sphere", WallpaperCategory.SPACE, "Deep Orbital Space", ProceduralStyle.ECLIPSE_HALO, listOf(0xFF07080D, 0xFF27272A, 0xFFA1A1AA, 0xFFFAFAFA), listOf("moon", "lunar", "eclipse", "space")),
        Spec("wp_free_space_solar_flare", "Helios Corona Flare", WallpaperCategory.SPACE, "Deep Orbital Space", ProceduralStyle.NEBULA_SPIRAL, listOf(0xFF120604, 0xFF7C2D12, 0xFFF59E0B, 0xFFFEF08A), listOf("sun", "solar", "flare", "space")),

        // 38-42: Architecture (5)
        Spec("wp_free_arch_brutalist_concrete", "Brutalist Concrete Atrium", WallpaperCategory.ARCHITECTURE, "Architectural Minimal", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF141619, 0xFF2C3038, 0xFF6B7280, 0xFFE5E7EB), listOf("brutalist", "concrete", "architecture")),
        Spec("wp_free_arch_kyoto_timber", "Kyoto Pavilion Shadow", WallpaperCategory.ARCHITECTURE, "Architectural Minimal", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFF17120E, 0xFF3F2E21, 0xFFA47148, 0xFFE7D7C1), listOf("kyoto", "wood", "architecture", "warm")),
        Spec("wp_free_arch_glass_curtain", "Skyscraper Glass Facade", WallpaperCategory.ARCHITECTURE, "Architectural Minimal", ProceduralStyle.CELLULAR_GLASS, listOf(0xFF091526, 0xFF1E3A5F, 0xFF60A5FA, 0xFFDBEAFE), listOf("glass", "tower", "facade", "architecture")),
        Spec("wp_free_arch_travertine_colonnade", "Travertine Light Arch", WallpaperCategory.ARCHITECTURE, "Architectural Minimal", ProceduralStyle.ARCHITECTURAL_ARCH, listOf(0xFFE7E2D8, 0xFFD6CEC0, 0xFF9C8F7E, 0xFF3E3832), listOf("travertine", "arch", "light", "architecture")),
        Spec("wp_free_arch_monolith_courtyard", "Monolith Courtyard", WallpaperCategory.ARCHITECTURE, "Architectural Minimal", ProceduralStyle.BAUHAUS_MONO, listOf(0xFF18181B, 0xFF3F3F46, 0xFFD4D4D8, 0xFFF4F4F5), listOf("monolith", "courtyard", "architecture")),

        // 43-46: Technology (4)
        Spec("wp_free_tech_quantum_lattice", "Quantum Silicon Lattice", WallpaperCategory.TECHNOLOGY, "Future Systems", ProceduralStyle.CYBER_GRID, listOf(0xFF050A14, 0xFF0F2942, 0xFF0EA5E9, 0xFF7DD3FC), listOf("quantum", "grid", "technology", "circuit")),
        Spec("wp_free_tech_neural_mesh", "Neural Signal Mesh", WallpaperCategory.TECHNOLOGY, "Future Systems", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFF070B12, 0xFF134E4A, 0xFF2DD4BF, 0xFFCCFBF1), listOf("neural", "mesh", "technology", "teal")),
        Spec("wp_free_tech_carbon_telemetry", "Carbon Fiber Telemetry", WallpaperCategory.TECHNOLOGY, "Future Systems", ProceduralStyle.CYBER_GRID, listOf(0xFF090A0D, 0xFF181B22, 0xFF64748B, 0xFFE2E8F0), listOf("carbon", "telemetry", "dark", "technology")),
        Spec("wp_free_tech_lidar_sweep", "LiDAR Orbital Sweep", WallpaperCategory.TECHNOLOGY, "Future Systems", ProceduralStyle.ORBITAL_RINGS, listOf(0xFF040D12, 0xFF083344, 0xFF22D3EE, 0xFFA5F3FC), listOf("lidar", "radar", "spinner", "technology")),

        // 47-50: Artistic (4)
        Spec("wp_free_art_stained_prism", "Cathedral Glass Prism", WallpaperCategory.ARTISTIC, "Studio Atelier", ProceduralStyle.PRISM_SHARDS, listOf(0xFF140C1C, 0xFF4C1D95, 0xFFEC4899, 0xFFFBBF24), listOf("artistic", "prism", "stained", "color")),
        Spec("wp_free_art_sumi_ink", "Sumi Ink Topography", WallpaperCategory.ARTISTIC, "Studio Atelier", ProceduralStyle.TOPOGRAPHIC_LINES, listOf(0xFFF3EFE6, 0xFFD6CFC2, 0xFF4A443D, 0xFF171513), listOf("ink", "sumi", "artistic", "zen")),
        Spec("wp_free_art_gold_leaf_marble", "Noir Gold Leaf Veins", WallpaperCategory.ARTISTIC, "Studio Atelier", ProceduralStyle.LIQUID_WAVES, listOf(0xFF09090B, 0xFF1C1917, 0xFFD4AF37, 0xFFFEF3C7), listOf("gold", "marble", "artistic", "luxury")),
        Spec("wp_free_art_kinetic_ribbons", "Kinetic Silk Ribbons", WallpaperCategory.ARTISTIC, "Studio Atelier", ProceduralStyle.AURORA_RIBBONS, listOf(0xFF0D111A, 0xFF1E40AF, 0xFF10B981, 0xFFFDE047), listOf("kinetic", "ribbons", "artistic"))
    )

    val items: List<WallpaperItem> = specs.mapIndexed { index, spec ->
        WallpaperItem(
            id = spec.id,
            name = spec.name,
            category = spec.category,
            collection = spec.collection,
            isPremium = false,
            resolution = spec.resolution,
            fileSizeLabel = spec.sizeLabel,
            tags = spec.tags,
            license = if (spec.drawableResName != null) {
                "Android Launcher Studio Original (Bundled Asset)"
            } else {
                "Android Launcher Procedural Studio Engine (Original)"
            },
            sourceType = if (spec.drawableResName != null) {
                WallpaperSourceType.BUNDLED_RASTER
            } else {
                WallpaperSourceType.PROCEDURAL_STUDIO
            },
            drawableResName = spec.drawableResName,
            paletteHexes = spec.palette,
            proceduralStyle = spec.style,
            seed = (index + 1) * 7919L + spec.id.hashCode().toLong(),
            sortOrder = index
        )
    }
}
