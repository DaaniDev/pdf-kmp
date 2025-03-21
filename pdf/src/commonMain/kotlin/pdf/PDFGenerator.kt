
import androidx.compose.runtime.Composable

expect class PDFGenerator() {
    /**
     * Creates a PDF from a composable
     * @param fileName Name of the output PDF file
     * @param content Composable content to render to PDF
     * @return Path to the created PDF file or null if generation failed
     */
    suspend fun createPdfFromComposable(
        fileName: String,
        content: @Composable () -> Unit
    ): String?

    /**
     * Share the generated PDF file
     * @param filePath Path to the PDF file
     */
    fun sharePdf(filePath: String)
}
