package com.app.mealplanner

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ImageCropActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var cropOverlay: CropOverlayView
    private lateinit var btnCrop: Button
    private lateinit var btnCancel: Button

    private var originalBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)

        imageView = findViewById(R.id.imageView)
        cropOverlay = findViewById(R.id.cropOverlay)
        btnCrop = findViewById(R.id.btnCrop)
        btnCancel = findViewById(R.id.btnCancel)

        // Get image URI from intent
        imageUri = intent.getParcelableExtra("imageUri")
        if (imageUri == null) {
            Toast.makeText(this, "Fehler beim Laden des Bildes", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        loadImage()

        btnCrop.setOnClickListener {
            cropImage()
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun loadImage() {
        try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                imageView.setImageBitmap(originalBitmap)
                cropOverlay.setBitmap(originalBitmap!!)
            } else {
                Toast.makeText(this, "Fehler beim Laden des Bildes", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler beim Laden des Bildes: ${e.message}", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun cropImage() {
        val bitmap = originalBitmap ?: return
        val cropRect = cropOverlay.getCropRect()

        try {
            // Calculate scale factors
            val imageViewWidth = imageView.width.toFloat()
            val imageViewHeight = imageView.height.toFloat()
            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()

            val scaleX = bitmapWidth / imageViewWidth
            val scaleY = bitmapHeight / imageViewHeight

            // Apply scale to crop rectangle
            val scaledLeft = (cropRect.left * scaleX).toInt()
            val scaledTop = (cropRect.top * scaleY).toInt()
            val scaledWidth = ((cropRect.right - cropRect.left) * scaleX).toInt()
            val scaledHeight = ((cropRect.bottom - cropRect.top) * scaleY).toInt()

            // Ensure crop rectangle is within bitmap bounds
            val left = max(0, scaledLeft)
            val top = max(0, scaledTop)
            val width = min(scaledWidth, bitmap.width - left)
            val height = min(scaledHeight, bitmap.height - top)

            if (width > 0 && height > 0) {
                val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
                saveCroppedImage(croppedBitmap)
            } else {
                Toast.makeText(this, "Ungültiger Zuschnittbereich", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler beim Zuschneiden: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCroppedImage(croppedBitmap: Bitmap) {
        try {
            val tempFile = File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            val resultIntent = Intent()
            resultIntent.data = Uri.fromFile(tempFile)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler beim Speichern: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
