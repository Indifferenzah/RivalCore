package com.riluttante.rivalcore.models;

public enum GamePhase {
    INITIAL,      // 0-30 min elapsed
    PHASE_TWO,    // 30-60 min elapsed
    PHASE_THREE,  // 60-90 min elapsed
    FINAL,        // 90-120 min elapsed
    ENDED         // 120+ min
}
