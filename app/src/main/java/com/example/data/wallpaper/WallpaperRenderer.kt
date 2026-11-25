package com.example.data.wallpaper

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.LruCache
import com.example.R
import com.example.data.model.ProceduralStyle
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}

class WallpaperRenderer(private val context: Context) {

    private val thumbCacheDir: File by lazy {
        File(context.cacheDir, "wp_thumbs").apply { mkdirs() }
    }

    private val downloadsDir: File by lazy {
        File(context.filesDir, "wallpapers").apply { mkdirs() }
    }

    private val memoryCache: LruCache<String, Bitmap> by lazy {
        val maxKb = (Runtime.getRuntime().maxMemory() / 1024L).toInt()
        val cacheSizeKb = (maxKb / 8).coerceIn(8192, 32768)
        object : LruCache<String, Bitmap>(cacheSizeKb) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 1024
            }
        }
    }

    suspend fun getThumbnailBitmap(item: WallpaperItem, autoDiskCache: Boolean = true): Bitmap =
        withContext(Dispatchers.IO) {
            val cacheKey = "thumb_${item.id}"
            memoryCache.get(cacheKey)?.let { if (!it.isRecycled) return@withContext it }

            val diskFile = File(thumbCacheDir, "${item.id}.jpg")
            if (autoDiskCache && diskFile.exists()) {
                BitmapFactory.decodeFile(diskFile.absolutePath)?.let { decoded ->
                    memoryCache.put(cacheKey, decoded)
                    return@withContext decoded
                }
            }

            val bitmap = renderBitmap(item, targetWidth = 360, targetHeight = 640)
            memoryCache.put(cacheKey, bitmap)

            if (autoDiskCache) {
                runCatching {
                    FileOutputStream(diskFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
                    }
                }
            }
            bitmap
        }

    suspend fun getFullPreviewBitmap(item: WallpaperItem): Bitmap = withContext(Dispatchers.IO) {
        val cacheKey = "preview_${item.id}"
        memoryCache.get(cacheKey)?.let { if (!it.isRecycled) return@withContext it }
        val bitmap = if (item.sourceType == WallpaperSourceType.REMOTE_IMAGE) {
            decodeRemoteBitmap(item.remoteMediaUrl ?: item.remoteThumbnailUrl, 900, 1600)
                ?: renderBitmap(item, targetWidth = 900, targetHeight = 1600)
        } else {
            renderBitmap(item, targetWidth = 900, targetHeight = 1600)
        }
        memoryCache.put(cacheKey, bitmap)
        bitmap
    }

    suspend fun getExportBitmap(item: WallpaperItem): Bitmap = withContext(Dispatchers.IO) {
        val wm = runCatching { WallpaperManager.getInstance(context) }.getOrNull()
        val desiredW = (wm?.desiredMinimumWidth ?: 0).takeIf { it in 720..1440 } ?: 1080
        val desiredH = (wm?.desiredMinimumHeight ?: 0).takeIf { it in 1280..2560 } ?: 2340
        if (item.sourceType == WallpaperSourceType.REMOTE_IMAGE) {
            decodeRemoteBitmap(item.remoteMediaUrl ?: item.remoteThumbnailUrl, desiredW, desiredH)
                ?.let { return@withContext it }
        }
        renderBitmap(item, targetWidth = desiredW, targetHeight = desiredH)
    }

    private fun decodeRemoteBitmap(urlString: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        val url = urlString?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 20_000
            }
            val bytes = conn.inputStream.use { it.readBytes() }
            conn.disconnect()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight)
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }.getOrNull()
    }

    suspend fun downloadWallpaper(item: WallpaperItem): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = getExportBitmap(item)
            try {
                val safeName = item.name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                val fileName = "${safeName}_${item.id}.png"

                // Save app-scoped local copy for offline access & downloaded gallery
                val internalFile = File(downloadsDir, fileName)
                FileOutputStream(internalFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                // Also save to public Pictures directory via MediaStore on API 29+ (zero storage permission)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    runCatching {
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                            put(
                                MediaStore.Images.Media.RELATIVE_PATH,
                                "${Environment.DIRECTORY_PICTURES}/Android 3D Rolling Launcher"
                            )
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        if (uri != null) {
                            resolver.openOutputStream(uri)?.use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            values.clear()
                            values.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(uri, values, null, null)
                        }
                    }
                }
                internalFile.absolutePath
            } finally {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }
    }

    suspend fun applySystemWallpaper(item: WallpaperItem, target: WallpaperTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = getExportBitmap(item)
                try {
                    val manager = WallpaperManager.getInstance(context)
                    val flags = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    val applied = runCatching {
                        manager.setBitmap(bitmap, null, true, flags)
                        true
                    }.getOrDefault(false)

                    // Some OEM ROMs (MIUI/HyperOS Lock Carousel) block FLAG_LOCK; safely fall back to HOME when BOTH was requested
                    if (!applied) {
                        if (target == WallpaperTarget.BOTH) {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        } else {
                            manager.setBitmap(bitmap)
                        }
                    }
                } finally {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
        }

    fun calculateCacheBytes(): Long {
        val thumbSize = thumbCacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        val downloadSize = downloadsDir.listFiles()?.sumOf { it.length() } ?: 0L
        return thumbSize + downloadSize
    }

    fun clearCache() {
        memoryCache.evictAll()
        thumbCacheDir.listFiles()?.forEach { it.delete() }
        downloadsDir.listFiles()?.forEach { it.delete() }
    }

    private fun renderBitmap(item: WallpaperItem, targetWidth: Int, targetHeight: Int): Bitmap {
        if (item.sourceType == WallpaperSourceType.BUNDLED_RASTER && item.drawableResName != null) {
            val resId = resolveDrawableId(item.drawableResName)
            if (resId != 0) {
                decodeSampledResource(resId, targetWidth, targetHeight)?.let { return it }
            }
        }
        return try {
            drawProceduralWallpaper(item, targetWidth, targetHeight)
        } catch (_: Throwable) {
            val safeW = (targetWidth / 2).coerceAtLeast(180)
            val safeH = (targetHeight / 2).coerceAtLeast(320)
            try {
                drawProceduralWallpaper(item, safeW, safeH)
            } catch (_: Throwable) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }
    }

    private fun resolveDrawableId(name: String): Int = when (name) {
        "wp_amoled_eclipse" -> R.drawable.wp_amoled_eclipse
        "wp_liquid_obsidian" -> R.drawable.wp_liquid_obsidian
        "wp_alpine_twilight" -> R.drawable.wp_alpine_twilight
        "wp_cosmic_nebula" -> R.drawable.wp_cosmic_nebula
        else -> 0
    }

    private fun decodeSampledResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, bounds)
            val inSample = calculateInSampleSize(bounds, reqWidth, reqHeight)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = inSample
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val decoded = BitmapFactory.decodeResource(context.resources, resId, opts) ?: return null
            if (decoded.width > reqWidth * 1.4f) {
                val scaled = Bitmap.createScaledBitmap(decoded, reqWidth, reqHeight, true)
                if (scaled !== decoded) decoded.recycle()
                scaled
            } else {
                decoded
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun drawProceduralWallpaper(item: WallpaperItem, w: Int, h: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rng = Random(item.seed)
        val colors = item.paletteHexes.map { it.toInt() }.ifEmpty {
            listOf(0xFF06080D.toInt(), 0xFF152038.toInt(), 0xFF38BDF8.toInt(), 0xFFE2E8F0.toInt())
        }
        val c0 = colors.getOrElse(0) { 0xFF06080D.toInt() }
        val c1 = colors.getOrElse(1) { c0 }
        val c2 = colors.getOrElse(2) { c1 }
        val c3 = colors.getOrElse(3) { c2 }

        // Base atmospheric gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w * 0.3f, h.toFloat(),
                intArrayOf(c0, c1, c0),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        when (item.proceduralStyle) {
            ProceduralStyle.ECLIPSE_HALO -> drawEclipseHalo(canvas, w, h, c0, c1, c2, c3, rng)
            ProceduralStyle.LIQUID_WAVES -> drawLiquidWaves(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.ALPINE_PEAKS -> drawAlpinePeaks(canvas, w, h, c0, c1, c2, c3, rng)
            ProceduralStyle.NEBULA_SPIRAL -> drawNebulaSpiral(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.BAUHAUS_MONO -> drawBauhausMono(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.CYBER_GRID -> drawCyberGrid(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.AURORA_RIBBONS -> drawAuroraRibbons(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.ARCHITECTURAL_ARCH -> drawArchitecturalArch(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.ORBITAL_RINGS -> drawOrbitalRings(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.PRISM_SHARDS -> drawPrismShards(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.TOPOGRAPHIC_LINES -> drawTopographicLines(canvas, w, h, c1, c2, c3, rng)
            ProceduralStyle.CELLULAR_GLASS -> drawCellularGlass(canvas, w, h, c1, c2, c3, rng)
        }
        return bitmap
    }

    private fun drawEclipseHalo(
        canvas: Canvas, w: Int, h: Int,
        c0: Int, c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val cx = w * (0.45f + rng.nextFloat() * 0.1f)
        val cy = h * (0.38f + rng.nextFloat() * 0.08f)
        val radius = min(w, h) * 0.27f

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, radius * 2.1f,
                intArrayOf(withAlpha(c2, 170), withAlpha(c1, 90), 0x00000000),
                floatArrayOf(0.25f, 0.58f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius * 2.1f, glowPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.018f
            shader = LinearGradient(
                cx - radius, cy - radius, cx + radius, cy + radius,
                c3, c2, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius, ringPaint)

        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c0 }
        canvas.drawCircle(cx, cy, radius * 0.94f, corePaint)

        val thinRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.004f
            color = withAlpha(c3, 140)
        }
        canvas.drawCircle(cx, cy, radius * 1.28f, thinRing)
    }

    private fun drawLiquidWaves(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val waveCount = 7
        for (i in 0 until waveCount) {
            val t = i / waveCount.toFloat()
            val yBase = h * (0.18f + t * 0.72f)
            val amp = h * (0.04f + rng.nextFloat() * 0.06f)
            val phase = rng.nextFloat() * 6.28f
            val path = Path().apply {
                moveTo(0f, h.toFloat())
                lineTo(0f, yBase)
                val steps = 28
                for (s in 0..steps) {
                    val x = (s / steps.toFloat()) * w
                    val y = yBase + sin((s / 4.2f) + phase) * amp + cos((s / 2.5f) - phase) * (amp * 0.4f)
                    lineTo(x, y)
                }
                lineTo(w.toFloat(), h.toFloat())
                close()
            }
            val waveColor = if (i % 2 == 0) withAlpha(c1, 140) else withAlpha(c2, 95)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(0f, yBase - amp, w.toFloat(), yBase + amp * 2, waveColor, withAlpha(c3, 45), Shader.TileMode.CLAMP)
            }
            canvas.drawPath(path, fillPaint)

            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = w * 0.003f
                this.color = withAlpha(c3, 90)
            }
            canvas.drawPath(path, rimPaint)
        }
    }

    private fun drawAlpinePeaks(
        canvas: Canvas, w: Int, h: Int,
        c0: Int, c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.35f, w * 0.9f,
                withAlpha(c3, 95), 0x00000000, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

        val layers = 5
        for (layer in 0 until layers) {
            val baseY = h * (0.42f + layer * 0.11f)
            val path = Path().apply {
                moveTo(0f, h.toFloat())
                lineTo(0f, baseY + rng.nextFloat() * h * 0.05f)
                val ridges = 9
                for (r in 1..ridges) {
                    val rx = (r / ridges.toFloat()) * w
                    val peakOffset = (rng.nextFloat() - 0.45f) * h * 0.13f
                    lineTo(rx, baseY + peakOffset)
                }
                lineTo(w.toFloat(), h.toFloat())
                close()
            }
            val layerColor = when (layer) {
                0 -> withAlpha(c3, 75)
                1 -> withAlpha(c2, 120)
                2 -> withAlpha(c1, 185)
                3 -> withAlpha(c1, 230)
                else -> c0
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = layerColor }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawNebulaSpiral(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val cx = w * 0.5f
        val cy = h * 0.46f
        val maxR = min(w, h) * 0.55f

        val bloom = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, maxR * 1.3f,
                intArrayOf(withAlpha(c2, 130), withAlpha(c1, 80), 0x00000000),
                floatArrayOf(0.1f, 0.5f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, maxR * 1.3f, bloom)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (arm in 0 until 3) {
            val armOffset = arm * (2f * PI.toFloat() / 3f)
            for (step in 0..160) {
                val t = step / 160f
                val angle = t * 8.5f + armOffset
                val r = t * maxR + (rng.nextFloat() - 0.5f) * (w * 0.08f)
                val x = cx + cos(angle) * r
                val y = cy + sin(angle) * r * 1.3f
                dotPaint.color = if (step % 3 == 0) withAlpha(c3, 180) else withAlpha(c2, 130)
                canvas.drawCircle(x, y, w * (0.002f + rng.nextFloat() * 0.006f), dotPaint)
            }
        }
    }

    private fun drawBauhausMono(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        blockPaint.color = withAlpha(c1, 200)
        canvas.drawRoundRect(
            RectF(w * 0.14f, h * 0.16f, w * 0.86f, h * 0.46f),
            w * 0.08f, w * 0.08f, blockPaint
        )

        blockPaint.color = withAlpha(c2, 220)
        val cx = w * (0.35f + rng.nextFloat() * 0.3f)
        canvas.drawCircle(cx, h * 0.54f, w * 0.24f, blockPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(c3, 180)
            strokeWidth = w * 0.01f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(w * 0.5f, h * 0.54f, w * 0.36f, linePaint)
        canvas.drawLine(w * 0.14f, h * 0.78f, w * 0.86f, h * 0.78f, linePaint)
    }

    private fun drawCyberGrid(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(c2, 65)
            strokeWidth = w * 0.0025f
        }
        val cols = 12
        val rows = 22
        for (i in 1 until cols) {
            val x = (i / cols.toFloat()) * w
            canvas.drawLine(x, 0f, x, h.toFloat(), linePaint)
        }
        for (j in 1 until rows) {
            val y = (j / rows.toFloat()) * h
            canvas.drawLine(0f, y, w.toFloat(), y, linePaint)
        }

        val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (k in 0 until 18) {
            val gx = ((rng.nextInt(cols - 2) + 1) / cols.toFloat()) * w
            val gy = ((rng.nextInt(rows - 2) + 1) / rows.toFloat()) * h
            nodePaint.color = withAlpha(c3, 190)
            canvas.drawCircle(gx, gy, w * 0.01f, nodePaint)
            nodePaint.color = withAlpha(c1, 120)
            canvas.drawCircle(gx, gy, w * 0.032f, nodePaint)
        }
    }

    private fun drawAuroraRibbons(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        for (ribbon in 0 until 4) {
            val startX = w * (0.15f + ribbon * 0.22f)
            val path = Path().apply {
                moveTo(startX, 0f)
                cubicTo(
                    startX + w * (rng.nextFloat() - 0.5f) * 0.6f, h * 0.3f,
                    startX - w * (rng.nextFloat() - 0.5f) * 0.6f, h * 0.65f,
                    w * (0.2f + rng.nextFloat() * 0.6f), h.toFloat()
                )
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = w * (0.14f - ribbon * 0.02f)
                strokeCap = Paint.Cap.ROUND
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    intArrayOf(withAlpha(c1, 80), withAlpha(c2, 130), withAlpha(c3, 75)),
                    null, Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawArchitecturalArch(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val archCount = 5
        for (i in 0 until archCount) {
            val inset = w * (0.08f + i * 0.065f)
            val top = h * (0.14f + i * 0.05f)
            val rect = RectF(inset, top, w - inset, h * 0.92f)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    inset, top, w - inset, h * 0.92f,
                    withAlpha(if (i % 2 == 0) c1 else c2, 110 + i * 25),
                    withAlpha(c3, 35),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(rect, (w - inset * 2) / 2f, (w - inset * 2) / 2f, fill)

            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = w * 0.003f
                color = withAlpha(c3, 120)
            }
            canvas.drawRoundRect(rect, (w - inset * 2) / 2f, (w - inset * 2) / 2f, border)
        }
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(c3, 140) }
        canvas.drawCircle(w * (0.4f + rng.nextFloat() * 0.2f), h * 0.28f, w * 0.08f, sunPaint)
    }

    private fun drawOrbitalRings(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val cx = w * 0.5f
        val cy = h * 0.45f
        val baseR = min(w, h) * 0.14f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

        for (i in 1..6) {
            val r = baseR * i * 0.68f
            paint.strokeWidth = w * (0.012f / (i * 0.45f))
            paint.color = if (i % 2 == 0) withAlpha(c2, 180) else withAlpha(c3, 120)
            val startAngle = rng.nextFloat() * 360f
            val sweep = 190f + rng.nextFloat() * 140f
            canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), startAngle, sweep, false, paint)
        }

        val centerGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, baseR * 1.4f, withAlpha(c2, 180), 0x00000000, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, baseR * 1.4f, centerGlow)
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c1 }
        canvas.drawCircle(cx, cy, baseR * 0.35f, dot)
    }

    private fun drawPrismShards(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        for (i in 0 until 14) {
            val x1 = rng.nextFloat() * w
            val y1 = rng.nextFloat() * h
            val x2 = x1 + (rng.nextFloat() - 0.5f) * w * 0.7f
            val y2 = y1 + (rng.nextFloat() - 0.5f) * h * 0.5f
            val x3 = x1 + (rng.nextFloat() - 0.5f) * w * 0.6f
            val y3 = y1 + (rng.nextFloat() - 0.5f) * h * 0.6f

            val path = Path().apply {
                moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); close()
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    x1, y1, x2, y2,
                    withAlpha(if (i % 2 == 0) c1 else c2, 125),
                    withAlpha(c3, 55),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(path, paint)

            val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = w * 0.0025f
                color = withAlpha(c3, 110)
            }
            canvas.drawPath(path, edge)
        }
    }

    private fun drawTopographicLines(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val cx = w * (0.35f + rng.nextFloat() * 0.3f)
        val cy = h * (0.35f + rng.nextFloat() * 0.3f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.003f
        }
        for (ring in 1..18) {
            val baseR = ring * w * 0.045f
            val path = Path()
            val segments = 48
            for (s in 0..segments) {
                val theta = (s / segments.toFloat()) * 2f * PI.toFloat()
                val warp = sin(theta * 3f + ring * 0.3f) * (w * 0.035f) +
                    cos(theta * 5f - ring * 0.2f) * (w * 0.02f)
                val r = baseR + warp
                val px = cx + cos(theta) * r
                val py = cy + sin(theta) * r * 1.25f
                if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            paint.color = when {
                ring % 4 == 0 -> withAlpha(c3, 165)
                ring % 2 == 0 -> withAlpha(c2, 125)
                else -> withAlpha(c1, 140)
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawCellularGlass(
        canvas: Canvas, w: Int, h: Int,
        c1: Int, c2: Int, c3: Int, rng: Random
    ) {
        val cols = 4
        val rows = 7
        val cellW = w / cols.toFloat()
        val cellH = h / rows.toFloat()
        val pad = w * 0.02f
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.003f
            color = withAlpha(c3, 95)
        }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val rect = RectF(
                    c * cellW + pad,
                    r * cellH + pad,
                    (c + 1) * cellW - pad,
                    (r + 1) * cellH - pad
                )
                val base = if ((r + c + rng.nextInt(3)) % 2 == 0) c1 else c2
                fillPaint.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    withAlpha(base, 120), withAlpha(c3, 40), Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(rect, w * 0.04f, w * 0.04f, fillPaint)
                canvas.drawRoundRect(rect, w * 0.04f, w * 0.04f, strokePaint)
            }
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
    }
}
