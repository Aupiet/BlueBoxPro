package com.example.blueboxpro

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestResultLogger : TestWatcher() {

    companion object {
        const val REPORT_FILE_PATH = "../Result/test_report.txt"
        
        init {
            // Setup the file
            val file = File(REPORT_FILE_PATH)
            file.parentFile?.mkdirs() // Create Result dir if it doesn't exist
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            file.writeText("=== BLUEBOXPRO TEST REPORT ===\n")
            file.appendText("Generated: $timestamp\n\n")
        }
        
        fun appendLog(message: String) {
            val file = File(REPORT_FILE_PATH)
            file.appendText(message + "\n")
        }
    }

    override fun succeeded(description: Description?) {
        val message = "✅ PASSED: ${description?.methodName}"
        println(message)
        appendLog(message)
    }

    override fun failed(e: Throwable?, description: Description?) {
        val message = "❌ FAILED: ${description?.methodName} -> ${e?.message}"
        println(message)
        appendLog(message)
    }

    override fun starting(description: Description?) {
        super.starting(description)
        appendLog("Running: ${description?.className}.${description?.methodName}")
    }
}
