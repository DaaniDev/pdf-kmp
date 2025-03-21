# Composable to PDF Converter Library

A Kotlin Multiplatform library for converting Jetpack Compose UI into a PDF file, supporting both Android and iOS. The library ensures that composables like `LazyColumn` are handled properly by using a `Column` to capture the full content.

## Features
- Convert any Jetpack Compose UI into a PDF.
- Supports Android and iOS.
- Automatically adjusts layouts to avoid rendering issues.
- Option to share the generated PDF file.
- Dynamic PDF page width and height (Planned for future work).

## Installation
Add the dependency to your `build.gradle.kts` file:

```kotlin
implementation("io.github.daanidev:pdf-kmp:1.0.1")
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
```

## Usage
### Generating a PDF from a Composable
Use the following code inside a composable function to generate a PDF file:

```kotlin
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val scope = rememberCoroutineScope()
val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
val fileName = "test_pdf_${currentTime}"

Button(onClick = {
    scope.launch {
        PDFUtil.generatePdf(
            fileName = fileName,
            content = {
                ShowList() // Your composable function
            },
            shareAfterCreation = false // Set to true to share after creation
        )
    }
}) {
    Text("Click me!")
}
```

### Android Setup
For Android, initialize the context inside the `onCreate` method of your `Activity`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ActivityContext.initializeActivityContext(this)
}
```

Additionally, add the following permissions to `AndroidManifest.xml` to allow storage access:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

### Best Practices
- **Avoid LazyColumn:** Use `Column` instead to capture all items properly.
- **Ensure Context Initialization:** Required on Android to avoid runtime errors.
- **Test on Different Devices:** Ensure UI elements render properly across various screen sizes.

## Future Work
- Support for dynamic PDF page width and height.

## License
This project is licensed under the MIT License.

## Contributions
Contributions are welcome! Feel free to submit a pull request or report issues.

## Contact
Feel free to open issue if you have found any.

