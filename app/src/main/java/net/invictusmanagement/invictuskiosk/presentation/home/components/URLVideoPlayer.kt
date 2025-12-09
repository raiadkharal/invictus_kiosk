package net.invictusmanagement.invictuskiosk.presentation.home.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import net.invictusmanagement.invictuskiosk.util.VideoCache

@OptIn(UnstableApi::class)
@Composable
fun UrlVideoPlayer(
    modifier: Modifier = Modifier,
    url: String
) {
    if (url.isEmpty()) return

    val context = LocalContext.current

    // Shared 100 MB LRU cache
    val cache = remember { VideoCache.getInstance(context) }

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


    val mediaSourceFactory = remember(url) {
        ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    val player = remember(url) {
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
            player.stop()
            player.clearMediaItems()
            player.release()
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp))) {
        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp)),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    this.player = player
                }
            },
            update = { view ->
                view.player = player
            }
        )
    }
}
