package com.example

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.File

/**
 * Looping MP4 video Live Wallpaper Engine rendering synced local video files
 * (`filesDir/active_live_wallpaper.mp4`) natively onto the Android system Wallpaper surface.
 */
class LiveOrbitVideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            startPlayback(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            runCatching {
                val player = mediaPlayer
                if (visible) {
                    if (player == null) {
                        surfaceHolder?.let { startPlayback(it) }
                    } else if (!player.isPlaying) {
                        player.start()
                    }
                } else if (player?.isPlaying == true) {
                    player.pause()
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releasePlayer()
        }

        override fun onDestroy() {
            super.onDestroy()
            releasePlayer()
        }

        private fun startPlayback(holder: SurfaceHolder) {
            releasePlayer()
            val file = activeVideoFile(applicationContext)
            if (!file.exists() || file.length() <= 0L) return

            runCatching {
                val player = MediaPlayer().apply {
                    setSurface(holder.surface)
                    setDataSource(file.absolutePath)
                    isLooping = true
                    setVolume(0f, 0f)
                    setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    setOnPreparedListener { mp ->
                        runCatching {
                            if (isVisible) mp.start()
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        releasePlayer()
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
            }
        }

        private fun releasePlayer() {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
            mediaPlayer = null
        }
    }

    companion object {
        private const val ACTIVE_VIDEO_FILE = "active_live_wallpaper.mp4"

        fun activeVideoFile(context: Context): File =
            File(context.applicationContext.filesDir, ACTIVE_VIDEO_FILE)

        fun createSetLiveWallpaperIntent(context: Context): Intent {
            val component = ComponentName(context, LiveOrbitVideoWallpaperService::class.java)
            return Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
