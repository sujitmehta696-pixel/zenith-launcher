package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.FreeThemeCatalog
import com.example.data.catalog.FreeWallpaperCatalog
import com.example.data.catalog.PremiumCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteAppEntity
import com.example.data.local.HiddenAppEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.ThemeEntity
import com.example.data.local.WallpaperEntity
import com.example.data.wallpaper.WallpaperRenderer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CatalogAndPersistenceTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `free wallpaper catalog has 50 unique entries`() {
        val items = FreeWallpaperCatalog.items
        assertEquals(50, items.size)
        assertEquals(50, items.map { it.id }.toSet().size)
        assertEquals(50, items.map { it.name }.toSet().size)
        assertTrue(items.none { it.isPremium })
    }

    @Test
    fun `free theme catalog has 50 unique entries`() {
        val themes = FreeThemeCatalog.items
        assertEquals(50, themes.size)
        assertEquals(50, themes.map { it.id }.toSet().size)
        assertEquals(50, themes.map { it.name }.toSet().size)
        assertTrue(themes.none { it.isPremium })
    }

    @Test
    fun `premium starter catalog has unique entries and valid manifest`() {
        val proWallpapers = PremiumCatalog.wallpaperItems
        val proThemes = PremiumCatalog.themeItems
        assertEquals(60, proWallpapers.size)
        assertEquals(60, proThemes.size)
        assertEquals(60, proWallpapers.map { it.id }.toSet().size)
        assertEquals(60, proThemes.map { it.id }.toSet().size)
        assertTrue(proWallpapers.all { it.isPremium })
        assertTrue(proThemes.all { it.isPremium })

        val manifest = PremiumCatalog.loadManifest(context)
        assertNotNull(manifest)
        assertEquals(50, manifest?.freeWallpapersBundled)
        assertEquals(50, manifest?.freeThemesBundled)
        assertEquals(2000, manifest?.premiumWallpapersTargetCapacity)
        assertEquals(2000, manifest?.premiumThemesTargetCapacity)
    }

    @Test
    fun `room wallpaper pagination search and favorites persist properly`() = runTest {
        val dao = db.wallpaperDao()
        val entities = (FreeWallpaperCatalog.items + PremiumCatalog.wallpaperItems)
            .map { WallpaperEntity.fromDomain(it) }
        dao.insertAllIgnore(entities)

        val pageOne = dao.queryWallpapers(null, null, "", 20).first()
        assertEquals(20, pageOne.size)

        val amoledFree = dao.queryWallpapers("AMOLED", false, "", 50).first()
        assertTrue(amoledFree.isNotEmpty())
        assertTrue(amoledFree.all { it.category == "AMOLED" && !it.isPremium })

        val targetId = FreeWallpaperCatalog.items.first().id
        dao.updateFavorite(targetId, true)
        val favs = dao.observeFavorites().first()
        assertTrue(favs.any { it.id == targetId })
    }

    @Test
    fun `room theme queries hidden apps and search history work`() = runTest {
        val themeDao = db.themeDao()
        themeDao.insertAllIgnore(FreeThemeCatalog.items.map { ThemeEntity.fromDomain(it) })

        val lightThemes = themeDao.queryThemes("LIGHT", false, "", 20).first()
        assertEquals(8, lightThemes.size)

        val metaDao = db.launcherMetaDao()
        metaDao.addFavoriteApp(FavoriteAppEntity("com.example.music"))
        metaDao.hideApp(HiddenAppEntity("com.example.secret"))
        metaDao.upsertSearchQuery(SearchHistoryEntity("AMOLED"))

        assertEquals(1, metaDao.observeFavoriteApps().first().size)
        assertEquals(1, metaDao.observeHiddenApps().first().size)
        assertEquals(listOf("AMOLED"), metaDao.observeSearchHistory().first().map { it.query })

        metaDao.clearSearchHistory()
        assertTrue(metaDao.observeSearchHistory().first().isEmpty())
    }

    @Test
    fun `wallpaper renderer produces valid non-empty bitmaps for procedural and raster items`() = runTest {
        val renderer = WallpaperRenderer(context)
        val rasterItem = FreeWallpaperCatalog.items.first()
        val proceduralItem = FreeWallpaperCatalog.items[10]

        val bmp1 = renderer.getThumbnailBitmap(rasterItem, autoDiskCache = false)
        val bmp2 = renderer.getThumbnailBitmap(proceduralItem, autoDiskCache = false)

        assertFalse(bmp1.isRecycled)
        assertFalse(bmp2.isRecycled)
        assertTrue(bmp1.width > 0 && bmp1.height > 0)
        assertTrue(bmp2.width > 0 && bmp2.height > 0)
    }
}
