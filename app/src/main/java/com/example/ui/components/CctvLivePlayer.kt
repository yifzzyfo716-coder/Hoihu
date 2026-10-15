package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CctvCamera

class CctvPlayerBridge(
    private val onReady: () -> Unit,
    private val onPlaying: () -> Unit,
    private val onPaused: () -> Unit,
    private val onError: (String) -> Unit
) {
    @JavascriptInterface
    fun onVideoReady() {
        onReady()
    }

    @JavascriptInterface
    fun onVideoPlaying() {
        onPlaying()
    }

    @JavascriptInterface
    fun onVideoPaused() {
        onPaused()
    }

    @JavascriptInterface
    fun onVideoError(code: String) {
        onError(code)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CctvLivePlayer(
    camera: CctvCamera,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var reloadCount by remember { mutableStateOf(0) }

    val bridge = remember(camera.id) {
        CctvPlayerBridge(
            onReady = {
                isLoading = false
            },
            onPlaying = {
                isLoading = false
                isPlaying = true
                hasError = false
            },
            onPaused = {
                isPlaying = false
            },
            onError = { _ ->
                isLoading = false
                hasError = true
            }
        )
    }

    val playerHtml = remember(camera.youtubeVideoId, autoPlay, reloadCount) {
        generateYouTubePlayerHtml(camera.youtubeVideoId, autoPlay)
    }

    Column(modifier = modifier) {
        // Player Video Box with 16:9 Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            androidx.compose.runtime.key(camera.id, reloadCount) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("cctv_webview_player"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowContentAccess = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                // Use standard mobile Chrome user agent to prevent YouTube embed restrictions
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                    android.util.Log.d("CctvLivePlayer", "JS Console: ${consoleMessage?.message()} (line ${consoleMessage?.lineNumber()})")
                                    return true
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript("if (typeof startPlay === 'function') startPlay();", null)
                                }
                            }
                            addJavascriptInterface(bridge, "AndroidBridge")
                            webViewInstance = this
                            loadDataWithBaseURL("https://www.youtube.com", playerHtml, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                    },
                    onRelease = { webView ->
                        webView.stopLoading()
                        webView.onPause()
                        webView.destroy()
                    }
                )
            }

            // Top Badges & Controls (LIVE, Audio toggle, Reload, Fullscreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red LIVE indicator
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC0B131E),
                    border = BorderStroke(1.dp, Color(0x4000D2FF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFFFF3B30) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "LIVE ถ่ายทอดสด" else "กำลังเชื่อมต่อสด...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Controls row: Mute/Unmute + Reload + Fullscreen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Audio Mute/Unmute Button
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            if (isMuted) {
                                webViewInstance?.evaluateJavascript("if (player && player.mute) player.mute();", null)
                            } else {
                                webViewInstance?.evaluateJavascript("if (player && player.unMute) player.unMute();", null)
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0B131E))
                            .testTag("audio_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "เปิดเสียง" else "ปิดเสียง",
                            tint = if (isMuted) Color(0xFF94A3B8) else Color(0xFF00E676),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Reload Stream Button
                    IconButton(
                        onClick = {
                            isLoading = true
                            hasError = false
                            reloadCount++
                            webViewInstance?.loadDataWithBaseURL(
                                "https://www.youtube.com",
                                playerHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0B131E))
                            .testTag("reload_stream_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "รีโหลดสตรีม",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Fullscreen Button
                    IconButton(
                        onClick = onFullScreenClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00D2FF))
                            .testTag("fullscreen_stream_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "เต็มจอ",
                            tint = Color(0xFF0B131E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Fallback Play Button if autoplay was blocked or paused
            if (!isPlaying && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            webViewInstance?.evaluateJavascript("startPlay();", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("play_video_overlay_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "เล่นวิดีโอ",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "เริ่มเล่นสตรีมสด",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Loading spinner
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color(0xFF00D2FF),
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // Camera Details and Action Strip below Player
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = camera.thaiName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${camera.area} • สตรีมสดความละเอียด 1080p FHD",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // External YouTube button
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(camera.originalUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .testTag("open_external_yt_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "เปิดใน YouTube",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    LaunchedEffect(camera.id) {
        isLoading = true
        isPlaying = false
        hasError = false
    }
}

fun generateYouTubePlayerHtml(videoId: String, autoPlay: Boolean): String {
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
            html, body { width: 100%; height: 100%; background: #000000; overflow: hidden; }
            #player-container { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
            iframe { width: 100%; height: 100%; border: none; }
        </style>
    </head>
    <body>
        <div id="player-container">
            <div id="yt-player"></div>
        </div>

        <script>
            var player;
            var isReady = false;

            // Safe extraction helper to prevent any "Cannot read property 'data' of undefined"
            // Handles missing, null, primitive, object, or nested data keys from YouTube events
            function extractEventData(event) {
                if (event === undefined || event === null) {
                    return null;
                }
                if (typeof event === 'number') {
                    return event;
                }
                if (typeof event === 'object') {
                    if ('data' in event && event.data !== undefined && event.data !== null) {
                        return event.data;
                    }
                    if ('info' in event && event.info !== undefined && event.info !== null) {
                        return event.info;
                    }
                    if (event.detail && typeof event.detail === 'object' && ('data' in event.detail)) {
                        return event.detail.data;
                    }
                }
                return null;
            }

            window.onYouTubeIframeAPIReady = function() {
                if (!window.YT || !window.YT.Player) {
                    return;
                }
                player = new YT.Player('yt-player', {
                    width: '100%',
                    height: '100%',
                    videoId: '$videoId',
                    playerVars: {
                        'autoplay': ${if (autoPlay) 1 else 0},
                        'mute': 1,
                        'playsinline': 1,
                        'controls': 1,
                        'rel': 0,
                        'enablejsapi': 1,
                        'iv_load_policy': 3,
                        'origin': 'https://www.youtube.com'
                    },
                    events: {
                        'onReady': onPlayerReady,
                        'onStateChange': onPlayerStateChange,
                        'onError': onPlayerError
                    }
                });
            };

            function onPlayerReady(event) {
                isReady = true;
                if (${if (autoPlay) "true" else "false"}) {
                    if (event && event.target && typeof event.target.playVideo === 'function') {
                        event.target.playVideo();
                    } else if (player && typeof player.playVideo === 'function') {
                        player.playVideo();
                    }
                }
                if (window.AndroidBridge && typeof window.AndroidBridge.onVideoReady === 'function') {
                    window.AndroidBridge.onVideoReady();
                }
            }

            function onPlayerStateChange(event) {
                var state = extractEventData(event);
                if (state === null || state === undefined) {
                    return;
                }

                // 1 = YT.PlayerState.PLAYING, 2 = YT.PlayerState.PAUSED
                var isPlaying = (state === 1) || (window.YT && window.YT.PlayerState && state === window.YT.PlayerState.PLAYING);
                var isPaused = (state === 2) || (window.YT && window.YT.PlayerState && state === window.YT.PlayerState.PAUSED);

                if (isPlaying) {
                    if (window.AndroidBridge && typeof window.AndroidBridge.onVideoPlaying === 'function') {
                        window.AndroidBridge.onVideoPlaying();
                    }
                } else if (isPaused) {
                    if (window.AndroidBridge && typeof window.AndroidBridge.onVideoPaused === 'function') {
                        window.AndroidBridge.onVideoPaused();
                    }
                }
            }

            function onPlayerError(event) {
                var err = extractEventData(event);
                var errStr = (err !== null && err !== undefined) ? String(err) : 'unknown_error';
                if (window.AndroidBridge && typeof window.AndroidBridge.onVideoError === 'function') {
                    window.AndroidBridge.onVideoError(errStr);
                }
            }

            function startPlay() {
                if (player && typeof player.playVideo === 'function') {
                    player.playVideo();
                }
            }

            // Asynchronously load the YouTube IFrame API script AFTER handler is defined
            (function() {
                if (window.YT && window.YT.Player) {
                    window.onYouTubeIframeAPIReady();
                } else {
                    var tag = document.createElement('script');
                    tag.src = "https://www.youtube.com/iframe_api";
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
                }
            })();
        </script>
    </body>
    </html>
    """.trimIndent()
}
