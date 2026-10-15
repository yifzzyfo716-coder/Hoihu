package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CctvCamera
import com.example.data.network.GeminiGroundedResponse
import com.example.data.network.GeminiMapsService
import com.example.data.repository.CctvRepository
import com.example.ui.components.GoogleMapType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val cameras: List<CctvCamera> = CctvRepository.cameras,
    val selectedCamera: CctvCamera = CctvRepository.cameras.first(),
    val currentTab: Int = 0, // 0: แผนที่ Google Map, 1: สตรีมสด, 2: Google Maps AI
    val mapType: GoogleMapType = GoogleMapType.DARK_CYBER,
    val showMapMarkers: Boolean = true,
    val isFullscreen: Boolean = false,
    val aiQueryPrompt: String = "",
    val aiResponse: GeminiGroundedResponse? = null,
    val isAiLoading: Boolean = false
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun selectCamera(camera: CctvCamera) {
        _uiState.update { it.copy(selectedCamera = camera) }
    }

    fun selectCameraAndPlayFullscreen(camera: CctvCamera) {
        _uiState.update { it.copy(selectedCamera = camera, isFullscreen = true) }
    }

    fun selectCameraById(id: String) {
        val found = _uiState.value.cameras.find { it.id == id }
        if (found != null) {
            _uiState.update { it.copy(selectedCamera = found) }
        }
    }

    fun toggleMapMarkers() {
        _uiState.update { it.copy(showMapMarkers = !it.showMapMarkers) }
    }

    fun setMapMarkersVisibility(visible: Boolean) {
        _uiState.update { it.copy(showMapMarkers = visible) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun setMapType(type: GoogleMapType) {
        _uiState.update { it.copy(mapType = type) }
    }

    fun setFullscreen(isFullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = isFullscreen) }
    }

    fun updateAiPrompt(prompt: String) {
        _uiState.update { it.copy(aiQueryPrompt = prompt) }
    }

    fun queryMapsGrounding(customPrompt: String? = null) {
        val prompt = customPrompt ?: _uiState.value.aiQueryPrompt
        if (prompt.isBlank()) return

        val selectedCam = _uiState.value.selectedCamera
        val fullPrompt = "พิกัดกล้อง CCTV: ${selectedCam.name} (${selectedCam.latitude}, ${selectedCam.longitude}) ใน ${selectedCam.location}. $prompt ค้นหาข้อมูลล่าสุดด้วย Google Maps Grounding เกี่ยวกับสถานที่ สภาพแวดล้อม และสิ่งอำนวยความสะดวกใกล้เคียง"

        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiResponse = null) }
            val response = GeminiMapsService.queryMapsGrounding(fullPrompt)
            _uiState.update { it.copy(isAiLoading = false, aiResponse = response) }
        }
    }

    fun quickExploreCurrentCamera() {
        val cam = _uiState.value.selectedCamera
        val prompt = "แนะนำสถานที่ท่องเที่ยว ชายหาด ร้านอาหารชื่อดัง และบรรยากาศสดรอบๆ ${cam.thaiName} (${cam.area} เกาะสมุย จ.สุราษฎร์ธานี)"
        _uiState.update { it.copy(aiQueryPrompt = prompt) }
        queryMapsGrounding(prompt)
    }
}
