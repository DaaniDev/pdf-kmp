import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import kotlin.math.min

actual class PDFGenerator actual constructor() {

    // A4 page dimensions in points (72 points per inch)
    private var PAGE_WIDTH = 595.0
    private var PAGE_HEIGHT = 842.0  // Adjusted to A4 size dynamically

    suspend fun createComposeViewController(content: @Composable () -> Unit): UIViewController? {
        return withContext(Dispatchers.Main) {
            println("⚠️ Initializing ComposeUIViewController...")

            val controller = ComposeUIViewController {
                LaunchedEffect(Unit) {
                    delay(2000) // Allow more time for Compose to settle
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    content()
                }
            }
            println("✅ ComposeUIViewController initialized successfully!")
            controller
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun createPdfFromComposable(
        fileName: String,
        content: @Composable () -> Unit
    ): String? {
        return withContext(Dispatchers.Main) {
            println("⚠️ Content is being called...")

            val uiViewController = createComposeViewController(content) ?: return@withContext null

            println("🎉 UIViewController successfully created!")

            val window = UIWindow(CGRectMake(0.0, 0.0, PAGE_WIDTH, PAGE_HEIGHT))
            window.rootViewController = uiViewController
            window.makeKeyAndVisible()

            val uiView = uiViewController.view ?: return@withContext null

            // Ensure UIKit has time to process layout updates
            repeat(10) { attempt ->
                delay(500)
                uiView.setNeedsLayout()
                uiView.layoutIfNeeded()

                val width = uiView.frame.useContents { size.width }
                val height = uiView.frame.useContents { size.height }

                println("📏 Attempt #$attempt: width=$width, height=$height")

                if (width > 0 && height > 0) {
                    println("✅ View fully rendered after $attempt attempts!")
                    return@repeat
                }
            }

            // Ensure everything is properly rendered
            delay(5000)
            uiView.setNeedsDisplay()
            uiView.layoutIfNeeded()

            // Get final height of the rendered view
            val fullHeight = uiView.frame.useContents { size.height }
            println("📏 Final Rendered Height: $fullHeight")

            // Dynamically adjust PAGE_HEIGHT if needed
            if (fullHeight > PAGE_HEIGHT) PAGE_HEIGHT = fullHeight

            // Split layout into multiple images
            val images = captureUIViewSections(uiView, fullHeight)

            // Generate the PDF
            val pdfFilePath = createMultiPagePdf(images, fileName)
            println("📄 PDF Generated at: $pdfFilePath")

            pdfFilePath
        }
    }

    actual fun sharePdf(filePath: String,authority:String) {
        val fileURL = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createMultiPagePdf(images: List<UIImage>, fileName: String): String? {
        println("📄 Creating PDF with ${images.size} pages.")

        // Create PDF context
        val pdfRenderer = UIGraphicsPDFRenderer(bounds = CGRectMake(0.0, 0.0, PAGE_WIDTH, PAGE_HEIGHT))

        // Generate PDF
        val pdfData = pdfRenderer.PDFDataWithActions { context ->
            images.forEach { image ->
                context?.beginPageWithBounds(CGRectMake(0.0, 0.0, PAGE_WIDTH, PAGE_HEIGHT), emptyMap<Any?, Any>())
                image.drawInRect(CGRectMake(0.0, 0.0, PAGE_WIDTH, PAGE_HEIGHT))
            }
        }

        // Save PDF to file
        val filePath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull()?.let { "$it/$fileName.pdf" }

        filePath?.let { path ->
            pdfData.writeToFile(path, true)
            return path
        }

        return null
    }

    /**
     * Splits a UIView into multiple UIImage sections
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun captureUIViewSections(view: UIView, fullHeight: Double): List<UIImage> {
        val images = mutableListOf<UIImage>()
        var offsetY = 0.0

        while (offsetY < fullHeight) {
            val captureHeight = min(PAGE_HEIGHT, fullHeight - offsetY)
            val snapshot = captureUIViewSectionAsImage(view, offsetY, captureHeight)
            if (snapshot != null) {
                images.add(snapshot)
            }
            offsetY += PAGE_HEIGHT
        }

        return images
    }

    /**
     * Captures a section of a UIView as an image
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun captureUIViewSectionAsImage(view: UIView, offsetY: Double, captureHeight: Double): UIImage? {
        val width = view.bounds.useContents { size.width }

        // Define the rect to capture
        val rect = CGRectMake(0.0, offsetY, width, captureHeight)

        // Use UIGraphicsImageRenderer for safer and more efficient rendering
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width, captureHeight))
        return renderer.imageWithActions { context ->
            // Fill with white background first
            UIColor.whiteColor.setFill()
            CGContextFillRect(context?.CGContext, CGRectMake(0.0, 0.0, width, captureHeight))

            // Then draw view
            view.drawViewHierarchyInRect(rect, afterScreenUpdates = true)
        }
    }
}