package com.example.diemdanhsinhvien.viewmodel

import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.repository.AttendanceRepository
import com.example.diemdanhsinhvien.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class QRDisplayViewModel(
    private val classId: Int,
    private val attendanceRepository: AttendanceRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Pair<AttendanceSession, String>>>(UiState.Loading)
    val uiState: StateFlow<UiState<Pair<AttendanceSession, String>>> = _uiState.asStateFlow()

    companion object {
        private const val ATTENDANCE_DURATION_MINUTES = 5L
    }

    init {
        loadAttendanceData()
    }

    fun loadAttendanceData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val classDetailsFlow = classRepository.getClassById(classId)
            val activeSessionFlow = attendanceRepository.getActiveSession(classId)
                .catch { e ->
                    _uiState.value = UiState.Error("Lỗi khi lấy phiên hoạt động: ${e.message}")
                    emit(null)
                }

            classDetailsFlow.combine(activeSessionFlow) { classDetails, activeSession ->
                Pair(classDetails, activeSession)
            }.catch { e ->
                _uiState.value = UiState.Error("Lỗi khi lấy dữ liệu lớp: ${e.message}")
            }.collect { (classDetails, activeSession) ->
                if (classDetails == null) {
                    _uiState.value = UiState.Error("Không tìm thấy thông tin lớp học.")
                    return@collect
                }

                // Giả định Class model có thuộc tính msFormUrl
                val formUrl = classDetails.msFormUrl
                if (formUrl.isNullOrBlank() || formUrl.contains("PLACEHOLDER")) {
                    _uiState.value = UiState.Error("Vui lòng cấu hình link MS Form cho lớp học này.")
                    return@collect
                }

                if (activeSession != null) {
                    processSession(activeSession, formUrl)
                } else {
                    createNewSession(formUrl)
                }
            }
        }
    }

    private fun createNewSession(formUrl: String) {
        viewModelScope.launch {
            attendanceRepository.createNewSession(classId, ATTENDANCE_DURATION_MINUTES)
                .catch { e ->
                    _uiState.value = UiState.Error("Không thể tạo phiên điểm danh mới: ${e.message}")
                }
                .collect { newSession ->
                    if (newSession != null) {
                        processSession(newSession, formUrl)
                    } else {
                        _uiState.value = UiState.Error("Không nhận được thông tin phiên mới từ server.")
                    }
                }
        }
    }

    private fun processSession(session: AttendanceSession, formUrl: String) {
        Log.d("QRDisplayViewModel", "Processing session: $session")

        val sessionCode = session.sessionCode
        if (sessionCode.isNullOrBlank()) {
            _uiState.value = UiState.Error("Server không trả về mã phiên (sessionCode).")
            return
        }

        val finalUrl = buildFinalUrl(formUrl, sessionCode)
        if (finalUrl == null) {
            _uiState.value = UiState.Error("Link Form cấu hình không hợp lệ.")
            return
        }

        _uiState.value = UiState.Success(Pair(session, finalUrl))
    }

    private fun buildFinalUrl(prefillUrl: String, sessionCode: String): String? {
        return try {
            val uri = Uri.parse(prefillUrl)
            val queryParams = uri.queryParameterNames
            val builder = uri.buildUpon().clearQuery()

            queryParams.forEach { key ->
                if (key.equals("id", ignoreCase = true)) {
                    builder.appendQueryParameter(key, uri.getQueryParameter(key))
                }
            }

            val prefillParamName = queryParams.firstOrNull { it.startsWith("entry.") || it.startsWith("rfb") }

            if (prefillParamName.isNullOrEmpty()) null else {
                builder.appendQueryParameter(prefillParamName, sessionCode).build().toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class QRDisplayViewModelFactory(
    private val classId: Int,
    private val attendanceRepository: AttendanceRepository,
    private val classRepository: ClassRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QRDisplayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QRDisplayViewModel(classId, attendanceRepository, classRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
