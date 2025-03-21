package com.daanidev.kmp_pdf_converter.pdf

import PDFGenerator
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PDFUtil {

    suspend fun generatePdf(
        fileName: String,
        content: @Composable () -> Unit,
        shareAfterCreation: Boolean = false,
        authority:String=""
    ): String? = withContext(Dispatchers.Default) {
        val generator = PDFGenerator()
        val pdfPath = generator.createPdfFromComposable(fileName, content)

        if (shareAfterCreation && pdfPath != null) {
            generator.sharePdf(pdfPath,authority)
        }

        return@withContext pdfPath
    }
}