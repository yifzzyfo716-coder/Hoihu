package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CctvCamera
import com.example.ui.components.CameraCard
import com.example.ui.components.CctvLivePlayer
import com.example.ui.components.FullScreenPlayerDialog
import com.example.ui.components.GoogleMapSurfaceView
import com.example.ui.components.GoogleMapType
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAreaFilter by remember { mutableStateOf("ทั้งหมด") }

    val filteredCameras = remember(uiState.cameras, selectedAreaFilter) {
        when (selectedAreaFilter) {
            "หาดละไม" -> uiState.cameras.filter { it.area.contains("ละไม") }
            "บ่อผุด" -> uiState.cameras.filter { it.area.contains("บ่อผุด") }
            else -> uiState.cameras
        }
    }

    // Full screen live player dialog
    if (uiState.isFullscreen) {
        FullScreenPlayerDialog(
            camera = uiState.selectedCamera,
            allCameras = uiState.cameras,
            onSelectCamera = { viewModel.selectCamera(it) },
            onDismiss = { viewModel.setFullscreen(false) }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF3B30),
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "THAI CCTV LIVE",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF00D2FF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x3300D2FF)
                                ) {
                                    Text(
                                        text = "เกาะสมุย",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "สุราษฎร์ธานี • 4 จุดเรียลไทม์แท้ 1080p",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x2200E676),
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4 กล้องออนไลน์",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "แผนที่") },
                    label = { Text("แผนที่ Google Maps", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_map")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Videocam, contentDescription = "สตรีมสด") },
                    label = { Text("สตรีมสด", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_live")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "AI รอบกล้อง") },
                    label = { Text("Google Maps AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_ai")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                0 -> MapViewTab(
                    viewModel = viewModel,
                    cameras = filteredCameras,
                    selectedCamera = uiState.selectedCamera,
                    mapType = uiState.mapType,
                    showMarkers = uiState.showMapMarkers
                )
                1 -> LiveStreamTab(
                    viewModel = viewModel,
                    cameras = filteredCameras,
                    selectedCamera = uiState.selectedCamera,
                    selectedArea = selectedAreaFilter,
                    onSelectArea = { selectedAreaFilter = it }
                )
                2 -> MapsAiTab(
                    viewModel = viewModel,
                    selectedCamera = uiState.selectedCamera,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun MapViewTab(
    viewModel: MainViewModel,
    cameras: List<CctvCamera>,
    selectedCamera: CctvCamera,
    mapType: GoogleMapType,
    showMarkers: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Interactive Google Map surface with Leaflet & real Google Maps tiles
        GoogleMapSurfaceView(
            cameras = cameras,
            selectedCameraId = selectedCamera.id,
            mapType = mapType,
            showMarkers = showMarkers,
            onCameraMarkerClicked = { cam ->
                // คลิกที่ไอคอนกล้องวงจรปิดบนแผนที่ วิดีโอเริ่มเล่นทันทีในโหมดเต็มหน้าจอ
                viewModel.selectCameraAndPlayFullscreen(cam)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Controls: Layer Switcher & Show/Hide Camera Icons
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Layer Switcher (Roadmap / Satellite / Dark Cyber / Terrain)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xF20B131E),
                border = BorderStroke(1.dp, Color(0x4000D2FF)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier
                            .padding(start = 6.dp, end = 2.dp)
                            .size(16.dp)
                    )

                    GoogleMapType.values().forEach { type ->
                        val isSelected = type == mapType
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF00D2FF) else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setMapType(type) }
                        ) {
                            Text(
                                text = type.label,
                                color = if (isSelected) Color(0xFF0B131E) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Show/Hide CCTV Markers Toggle Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (showMarkers) Color(0xD90F172A) else Color(0xCC334155),
                border = BorderStroke(1.dp, if (showMarkers) Color(0xFF00E676) else Color(0xFF94A3B8)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.toggleMapMarkers() }
                    .testTag("toggle_markers_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showMarkers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (showMarkers) Color(0xFF00E676) else Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showMarkers) "ไอคอนกล้อง: แสดงอยู่ (แตะเพื่อซ่อน)" else "ไอคอนกล้อง: ซ่อนอยู่ (แตะเพื่อแสดง)",
                        color = if (showMarkers) Color.White else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Selected Camera Card with Live Preview & Fullscreen
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xF20F172A),
            border = BorderStroke(1.5.dp, Color(0xFF00D2FF)),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header with live dot and camera details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3B30))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ตำแหน่งกล้องบน Google Maps",
                                color = Color(0xFF00D2FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = selectedCamera.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "พิกัด: ${selectedCamera.latitude}°N, ${selectedCamera.longitude}°E",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.setFullscreen(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("map_view_fullscreen_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "เต็มจอ",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ดูเต็มจอ",
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Embedded live mini player
                CctvLivePlayer(
                    camera = selectedCamera,
                    onFullScreenClick = { viewModel.setFullscreen(true) },
                    autoPlay = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LiveStreamTab(
    viewModel: MainViewModel,
    cameras: List<CctvCamera>,
    selectedCamera: CctvCamera,
    selectedArea: String,
    onSelectArea: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Active Live Stream Player
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Color(0xFF00D2FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ถ่ายทอดสดออนไลน์ (เล่นอัตโนมัติ)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3300E676)
                        ) {
                            Text(
                                text = "สตรีมเรียลไทม์",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CctvLivePlayer(
                        camera = selectedCamera,
                        onFullScreenClick = { viewModel.setFullscreen(true) },
                        autoPlay = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Area Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ทั้งหมด", "หาดละไม", "บ่อผุด").forEach { area ->
                    FilterChip(
                        selected = selectedArea == area,
                        onClick = { onSelectArea(area) },
                        label = { Text(area, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Camera list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการกล้องวงจรปิด เกาะสมุย (${cameras.size} จุด)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "แตะเพื่อสลับกล้อง",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cameras list
        items(cameras, key = { it.id }) { cam ->
            CameraCard(
                camera = cam,
                isSelected = cam.id == selectedCamera.id,
                onSelect = { viewModel.selectCamera(cam) },
                onFullScreen = {
                    viewModel.selectCamera(cam)
                    viewModel.setFullscreen(true)
                },
                onViewOnMap = {
                    viewModel.selectCamera(cam)
                    viewModel.setTab(0)
                }
            )
        }
    }
}

@Composable
private fun MapsAiTab(
    viewModel: MainViewModel,
    selectedCamera: CctvCamera,
    uiState: com.example.ui.viewmodel.MainUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color(0x3300D2FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Maps AI Grounding",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FF)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ค้นหาข้อมูลล่าสุด สถานที่ท่องเที่ยว ร้านอาหารยอดนิยม และสภาพแวดล้อมจริงรอบพิกัดกล้องวงจรปิดด้วยโมเดล Gemini 3.8 Flash และเครื่องมือ Google Maps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "กล้องที่กำลังโฟกัส: ${selectedCamera.thaiName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Quick explore button
        Button(
            onClick = { viewModel.quickExploreCurrentCamera() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_explore_ai_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "สำรวจไฮไลท์รอบกล้องนี้ด้วย Google Maps",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
        }

        // Search Input
        OutlinedTextField(
            value = uiState.aiQueryPrompt,
            onValueChange = { viewModel.updateAiPrompt(it) },
            label = { Text("พิมพ์คำถามเกี่ยวกับพื้นที่รอบกล้องนี้") },
            placeholder = { Text("เช่น ร้านอาหารอร่อยใกล้ Crystal Bay, สภาพอากาศ, จุดดำน้ำ") },
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.queryMapsGrounding() },
                    modifier = Modifier.testTag("send_ai_query_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "ส่งคำถาม",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D2FF),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Suggestion Chips
        Text(
            text = "คำถามยอดนิยมรอบเกาะสมุย:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val sampleQueries = listOf(
            "🏖️ ชายหาดและจุดดำน้ำใกล้ Crystal Bay",
            "🍹 บาร์และอาหารค่ำรอบถนนคนเดินหาดละไม",
            "🛍️ ถนนคนเดิน Fisherman's Village บ่อผุด เปิดวันไหน",
            "☀️ ข้อมูลสภาพอากาศและคลื่นลมทะเลเกาะสมุยวันนี้"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sampleQueries.forEach { q ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.updateAiPrompt(q)
                            viewModel.queryMapsGrounding(q)
                        }
                ) {
                    Text(
                        text = q,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Loading or AI Results
        if (uiState.isAiLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF00D2FF),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "กำลังค้นหาข้อมูลและพิกัดจาก Google Maps AI...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        uiState.aiResponse?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF00D2FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ผลลัพธ์จาก Google Maps Grounding",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D2FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = res.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    if (res.groundedPlaces.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "สถานที่อ้างอิงบน Google Maps:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        res.groundedPlaces.forEach { place ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF00D2FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = place.title,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
