package net.invictusmanagement.invictuskiosk.presentation.home.components

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.*
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import net.invictusmanagement.invictuskiosk.util.VideoCache
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun UrlVideoPlayer(
    modifier: Modifier = Modifier,
    url: String
) {
    val context = LocalContext.current

    // Cache initialization
    val cache = remember { VideoCache.getInstance(context) }

    if(url.isEmpty()) return
    // DataSource.Factory with Cache
    val dataSourceFactory = remember(url) {
        if (url.startsWith("http")) {
            val upstreamFactory = DefaultHttpDataSource.Factory()
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            // default datasource factory for video from res/raw
            DefaultDataSource.Factory(context)
        }
    }


    val mediaSourceFactory = remember {
        ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    // ExoPlayer instance with cache-enabled media source
    val player = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(url))
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp))) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
