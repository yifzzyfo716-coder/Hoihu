package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CctvCamera
import org.json.JSONArray
import org.json.JSONObject

enum class GoogleMapType(val id: String, val label: String) {
    DARK_CYBER("dark_cyber", "🌃 ไฮเทค ดาร์ก"),
    SATELLITE("satellite", "🛰️ ดาวเทียม HD"),
    ROADMAP("roadmap", "🗺️ แผนที่ถนน"),
    TERRAIN("terrain", "⛰️ ภูมิประเทศ")
}

class GoogleMapJsBridge(
    private val onMarkerClicked: (String) -> Unit
) {
    @JavascriptInterface
    fun onMarkerClick(cameraId: String) {
        onMarkerClicked(cameraId)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleMapSurfaceView(
    cameras: List<CctvCamera>,
    selectedCameraId: String?,
    mapType: GoogleMapType,
    showMarkers: Boolean,
    onCameraMarkerClicked: (CctvCamera) -> Unit,
    modifier: Modifier = Modifier
) {
    val jsBridge = remember(onCameraMarkerClicked) {
        GoogleMapJsBridge { cameraId ->
            cameras.find { it.id == cameraId }?.let { cam ->
                onCameraMarkerClicked(cam)
            }
        }
    }

    val htmlData = remember(cameras) {
        generateGoogleMapHtml(cameras)
    }

    var webViewInstance: WebView? = remember { null }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewInstance = this
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(true)
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("GoogleMapSurface", "JS Console: ${consoleMessage?.message()} (line ${consoleMessage?.lineNumber()})")
                            return true
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (selectedCameraId != null) {
                                view?.evaluateJavascript("window.focusCamera('$selectedCameraId', 15);", null)
                            }
                            view?.evaluateJavascript("window.setMapLayer('${mapType.id}');", null)
                            view?.evaluateJavascript("window.toggleMarkers($showMarkers);", null)
                        }
                    }
                    addJavascriptInterface(jsBridge, "AndroidBridge")
                    loadDataWithBaseURL("https://maps.google.com/", htmlData, "text/html", "UTF-8", null)
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

    // React to selected camera changes
    LaunchedEffect(selectedCameraId) {
        if (selectedCameraId != null) {
            webViewInstance?.evaluateJavascript("if (window.focusCamera) window.focusCamera('$selectedCameraId', 15);", null)
        }
    }

    // React to map type changes
    LaunchedEffect(mapType) {
        webViewInstance?.evaluateJavascript("if (window.setMapLayer) window.setMapLayer('${mapType.id}');", null)
    }

    // React to show/hide markers changes
    LaunchedEffect(showMarkers) {
        webViewInstance?.evaluateJavascript("if (window.toggleMarkers) window.toggleMarkers($showMarkers);", null)
    }
}

private fun generateGoogleMapHtml(cameras: List<CctvCamera>): String {
    val camerasJson = JSONArray().apply {
        cameras.forEach { cam ->
            put(JSONObject().apply {
                put("id", cam.id)
                put("name", cam.name)
                put("thaiName", cam.thaiName)
                put("location", cam.location)
                put("lat", cam.latitude)
                put("lng", cam.longitude)
                put("area", cam.area)
                put("category", cam.category.label)
            })
        }
    }.toString()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <title>Thai CCTV Google Map</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
        html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #070D14; }
        
        /* Custom Map Surface Styling */
        .leaflet-tile-pane {
            transition: filter 0.4s ease-in-out;
        }
        
        /* Dark Cyber Surveillance Custom Surface */
        .map-dark-cyber .leaflet-tile-pane {
            filter: invert(93%) hue-rotate(180deg) brightness(88%) contrast(125%) saturate(140%);
        }

        /* Custom Camera Marker */
        .cctv-marker-container {
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            cursor: pointer;
            transition: opacity 0.3s ease;
        }
        
        .cctv-pulse {
            position: absolute;
            width: 52px;
            height: 52px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(255, 59, 48, 0.45) 0%, rgba(255, 59, 48, 0.05) 70%, transparent 100%);
            animation: radar-pulse 2.2s infinite ease-out;
            pointer-events: none;
        }
        
        .cctv-circle {
            position: relative;
            width: 42px;
            height: 42px;
            border-radius: 50%;
            background: linear-gradient(135deg, #0F172A, #0284C7);
            border: 2.5px solid #00D2FF;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 16px rgba(0, 210, 255, 0.5), 0 2px 8px rgba(0,0,0,0.8);
            transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        
        .cctv-circle:hover, .cctv-marker-container.active .cctv-circle {
            background: linear-gradient(135deg, #FF3B30, #DC2626);
            border-color: #FFFFFF;
            transform: scale(1.18);
            box-shadow: 0 0 22px rgba(255, 59, 48, 0.9);
        }
        
        .cctv-circle svg {
            width: 22px;
            height: 22px;
            fill: #FFFFFF;
        }
        
        .cctv-live-tag {
            position: absolute;
            top: -2px;
            right: -2px;
            background: #FF3B30;
            color: #FFFFFF;
            font-size: 8px;
            font-weight: 900;
            padding: 1px 4px;
            border-radius: 6px;
            border: 1px solid #FFFFFF;
            letter-spacing: 0.5px;
            box-shadow: 0 1px 4px rgba(0,0,0,0.5);
        }

        .cctv-label {
            position: absolute;
            top: 45px;
            background: rgba(10, 17, 26, 0.95);
            color: #E2E8F0;
            font-size: 11px;
            font-weight: 700;
            font-family: system-ui, -apple-system, sans-serif;
            padding: 3px 9px;
            border-radius: 12px;
            white-space: nowrap;
            border: 1px solid rgba(0, 210, 255, 0.5);
            box-shadow: 0 3px 10px rgba(0,0,0,0.5);
            pointer-events: none;
            letter-spacing: 0.2px;
        }

        @keyframes radar-pulse {
            0% { transform: scale(0.6); opacity: 1; }
            100% { transform: scale(1.6); opacity: 0; }
        }

        /* Tooltip on Map */
        .leaflet-popup-content-wrapper {
            background: #0B131E;
            color: #FFFFFF;
            border-radius: 16px;
            border: 1.5px solid #00D2FF;
            box-shadow: 0 12px 28px rgba(0,0,0,0.7);
            padding: 4px;
        }
        .leaflet-popup-tip {
            background: #0B131E;
        }
        .popup-card {
            padding: 10px 12px;
            font-family: system-ui, -apple-system, sans-serif;
        }
        .popup-badge {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            background: rgba(255, 59, 48, 0.2);
            color: #FF5252;
            padding: 3px 9px;
            border-radius: 20px;
            font-size: 10px;
            font-weight: 800;
            margin-bottom: 6px;
        }
        .popup-badge::before {
            content: "";
            width: 7px;
            height: 7px;
            background: #FF3B30;
            border-radius: 50%;
            display: inline-block;
        }
        .popup-title {
            font-size: 13px;
            font-weight: 700;
            margin-bottom: 4px;
            color: #FFFFFF;
        }
        .popup-sub {
            font-size: 11px;
            color: #94A3B8;
            margin-bottom: 8px;
        }
        .popup-btn {
            background: #00D2FF;
            color: #0B131E;
            border: none;
            border-radius: 8px;
            padding: 7px 12px;
            font-size: 12px;
            font-weight: 800;
            width: 100%;
            cursor: pointer;
            text-align: center;
            box-shadow: 0 2px 8px rgba(0, 210, 255, 0.4);
        }
    </style>
</head>
<body class="map-dark-cyber">
    <div id="map"></div>

    <script>
        const cameras = $camerasJson;
        let currentLayer = null;
        let activeCameraId = null;
        const markersGroup = L.layerGroup();
        const markers = {};

        // Initialize Map centered on Koh Samui
        const map = L.map('map', {
            center: [9.5120, 100.0450],
            zoom: 12,
            zoomControl: false,
            attributionControl: false
        });

        // Real Google Maps tile layers & custom surfaces
        const googleLayers = {
            roadmap: L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            }),
            satellite: L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            }),
            terrain: L.tileLayer('https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            }),
            dark_cyber: L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            })
        };

        // Add default custom dark cyber surface
        currentLayer = googleLayers.dark_cyber.addTo(map);
        markersGroup.addTo(map);

        window.setMapLayer = function(type) {
            if (googleLayers[type]) {
                map.removeLayer(currentLayer);
                currentLayer = googleLayers[type].addTo(map);
                
                if (type === 'dark_cyber') {
                    document.body.className = 'map-dark-cyber';
                } else {
                    document.body.className = '';
                }
            }
        };

        window.toggleMarkers = function(show) {
            if (show) {
                if (!map.hasLayer(markersGroup)) {
                    markersGroup.addTo(map);
                }
            } else {
                if (map.hasLayer(markersGroup)) {
                    map.removeLayer(markersGroup);
                }
            }
        };

        // Add CCTV camera markers
        cameras.forEach(cam => {
            const iconHtml = `
                <div class="cctv-marker-container" id="marker-${'$'}{cam.id}">
                    <div class="cctv-pulse"></div>
                    <div class="cctv-circle">
                        <svg viewBox="0 0 24 24">
                            <path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4zM14 13h-3v3H9v-3H6v-2h3V8h2v3h3v2z"/>
                        </svg>
                        <div class="cctv-live-tag">LIVE</div>
                    </div>
                    <div class="cctv-label">${'$'}{cam.thaiName.split(' ')[0]}</div>
                </div>
            `;

            const customIcon = L.divIcon({
                className: '',
                html: iconHtml,
                iconSize: [52, 52],
                iconAnchor: [26, 26]
            });

            const marker = L.marker([cam.lat, cam.lng], { icon: customIcon });

            // Direct click triggers immediate autoplay in full-screen!
            marker.on('click', () => {
                selectAndPlayCamera(cam.id);
            });

            markers[cam.id] = marker;
            markersGroup.addLayer(marker);
        });

        window.selectAndPlayCamera = function(id) {
            activeCameraId = id;
            updateActiveMarkerClass();
            if (window.AndroidBridge && window.AndroidBridge.onMarkerClick) {
                window.AndroidBridge.onMarkerClick(id);
            }
        };

        function updateActiveMarkerClass() {
            cameras.forEach(cam => {
                const el = document.getElementById('marker-' + cam.id);
                if (el) {
                    if (cam.id === activeCameraId) {
                        el.classList.add('active');
                    } else {
                        el.classList.remove('active');
                    }
                }
            });
        }

        window.focusCamera = function(id, zoomLevel) {
            const cam = cameras.find(c => c.id === id);
            if (cam) {
                activeCameraId = id;
                updateActiveMarkerClass();
                map.flyTo([cam.lat, cam.lng], zoomLevel || 15, { duration: 1.2 });
            }
        };
    </script>
</body>
</html>
    """.trimIndent()
}
