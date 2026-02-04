package com.aegis.das.ui

import androidx.lifecycle.ViewModel
import com.aegis.das.domain.inference.InferenceEngine
import com.aegis.das.domain.inference.MockInferenceEngine
import com.aegis.das.domain.scenario.ScenarioPreset
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.InferenceSnapshot
import com.aegis.das.domain.state.ProcessorType
import com.aegis.das.domain.state.ToolDefaults
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolId
import com.aegis.das.domain.tools.ToolKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val inferenceEngine: InferenceEngine = MockInferenceEngine()
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun setDebugMode(enabled: Boolean) {
        _state.update { it.copy(debugMode = enabled) }
    }

    fun setProcessor(processor: ProcessorType) {
        _state.update { it.copy(processor = processor) }
    }

    fun updateTool(toolName: String, partial: Map<String, Any?>) {
        val toolId = ToolId.fromName(toolName) ?: return
        updateTool(toolId, partial)
    }

    fun updateTool(toolId: ToolId, partial: Map<String, Any?>) {
        when (toolId.kind) {
            ToolKind.CONTEXT -> updateContextTool(toolId, partial)
            ToolKind.ACTION -> applyActionCall(toolId, partial)
        }
    }

    fun updateContextTool(toolName: String, partial: Map<String, Any?>) {
        val toolId = ToolId.fromName(toolName) ?: return
        updateContextTool(toolId, partial)
    }

    fun updateContextTool(toolId: ToolId, partial: Map<String, Any?>) {
        if (!ToolDefaults.isValidKind(toolId, ToolKind.CONTEXT)) return
        val sanitized = sanitizePayload(toolId, partial)
        if (sanitized.isEmpty()) return
        _state.update { appState ->
            val current = appState.contextStates[toolId] ?: ToolDefaults.defaultToolState(toolId)
            val nextPayload = current.payload.toMutableMap().apply { putAll(sanitized) }
            appState.copy(
                contextStates = appState.contextStates + (toolId to current.withPayload(nextPayload))
            )
        }
    }

    fun applyScenario(preset: ScenarioPreset) {
        _state.update { appState ->
            val updatedContexts = preset.contextOverrides.entries.fold(appState.contextStates) { acc, entry ->
                val toolId = entry.key
                val sanitized = sanitizePayload(toolId, entry.value)
                val current = acc[toolId] ?: ToolDefaults.defaultToolState(toolId)
                val nextPayload = current.payload.toMutableMap().apply { putAll(sanitized) }
                acc + (toolId to current.withPayload(nextPayload))
            }
            appState.copy(contextStates = updatedContexts)
        }
        runInference()
    }

    fun runInference() {
        val result = inferenceEngine.infer(_state.value)
        _state.update { appState ->
            val updatedActions = result.actionCalls.fold(appState.actionStates) { acc, call ->
                val sanitized = sanitizePayload(call.toolId, call.args)
                if (sanitized.isEmpty()) {
                    acc
                } else {
                    val current = acc[call.toolId] ?: ToolDefaults.defaultToolState(call.toolId)
                    val nextPayload = current.payload.toMutableMap().apply { putAll(sanitized) }
                    acc + (call.toolId to current.withPayload(nextPayload))
                }
            }
            appState.copy(
                actionStates = updatedActions,
                lastInference = InferenceSnapshot(
                    thought = result.thought,
                    summary = result.summary,
                    severity = result.severity,
                    actionToolNames = result.actionCalls.map { it.toolId.toolName },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun applyActionCall(toolName: String, args: Map<String, Any?>) {
        val toolId = ToolId.fromName(toolName) ?: return
        applyActionCall(toolId, args)
    }

    fun applyActionCall(toolId: ToolId, args: Map<String, Any?>) {
        if (!ToolDefaults.isValidKind(toolId, ToolKind.ACTION)) return
        val sanitized = sanitizePayload(toolId, args)
        if (sanitized.isEmpty()) return
        _state.update { appState ->
            val current = appState.actionStates[toolId] ?: ToolDefaults.defaultToolState(toolId)
            val nextPayload = current.payload.toMutableMap().apply { putAll(sanitized) }
            appState.copy(
                actionStates = appState.actionStates + (toolId to current.withPayload(nextPayload))
            )
        }
    }

    private fun sanitizePayload(toolId: ToolId, payload: Map<String, Any?>): Map<String, Any?> {
        val allowed = ToolDefaults.allowedKeys(toolId)
        return payload.filterKeys { it in allowed }
    }

    private fun ToolState.withPayload(payload: Map<String, Any?>): ToolState = copy(
        payload = payload,
        updatedAt = System.currentTimeMillis()
    )
}
