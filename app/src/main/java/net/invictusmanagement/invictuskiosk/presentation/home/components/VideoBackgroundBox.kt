package net.invictusmanagement.invictuskiosk.presentation.home.components

import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun UrlVideoPlayer(
    modifier: Modifier = Modifier,
    url: String
) {
    val context = LocalContext.current

    // 1) Build a DefaultHttpDataSource that will handle HTTPS (and redirects)
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory().apply {
            // allow redirects if the server sends you elsewhere
            setAllowCrossProtocolRedirects(true)
        }
    }

    // 2) Create a ProgressiveMediaSource.Factory from it
    val mediaSourceFactory = remember {
        ProgressiveMediaSource.Factory(httpDataSourceFactory)
    }

    // 3) Build & remember your ExoPlayer with that mediaSourceFactory
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

    // 4) Clean up when the Composable leaves composition
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp))) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            // DEBUG: give it a fixed height so we know it’s laid out
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
