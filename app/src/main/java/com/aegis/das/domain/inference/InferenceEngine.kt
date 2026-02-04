package com.aegis.das.domain.inference

import com.aegis.das.domain.state.AppState

interface InferenceEngine {
    fun infer(appState: AppState): InferenceResult
}
