package com.example.util

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.lang.StringBuilder

object PdfExtractor {
    
    // Extracts raw text bytes from a PDF. LinkedIn PDFs have plain text segments inside brackets or parentheses
    fun extractTextFromPdf(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return "Error reading PDF: InputStream is null."
            
            val builder = StringBuilder()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            
            // Read PDF bytes and find ASCII text characters to construct basic plain text profile representations
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                var index = 0
                while (index < bytesRead) {
                    val b = buffer[index]
                    // Standard text block extractor in basic PDF structure (usually inside a BT ... ET block or (text) Tj)
                    if (b == '('.code.toByte()) {
                        // Start of text segment
                        index++
                        val currentText = java.lang.StringBuilder()
                        while (index < bytesRead) {
                            val nextByte = buffer[index]
                            if (nextByte == ')'.code.toByte()) {
                                break
                            }
                            if (nextByte >= 32 && nextByte <= 126) { // printable ascii
                                currentText.append(nextByte.toChar())
                            }
                            index++
                        }
                        if (currentText.length > 3) {
                            builder.append(currentText.toString()).append(" ")
                        }
                    }
                    index++
                }
            }
            inputStream.close()
            
            val rawExtracted = builder.toString().trim()
            if (rawExtracted.length < 50) {
                return "" // Return empty to trigger genuine empty exception in ViewModel
            }
            rawExtracted
        } catch (e: Exception) {
            "Error extracting PDF text: ${e.message}"
        }
    }
}
