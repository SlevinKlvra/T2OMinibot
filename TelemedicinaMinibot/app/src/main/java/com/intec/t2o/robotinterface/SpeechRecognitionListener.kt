package com.intec.t2o.robotinterface

interface SpeechRecognitionListener {
    fun onSpeechPartialResult(result: String)
    fun onSpeechFinalResult(result: String)
    // Añadir más métodos según sea necesario
}