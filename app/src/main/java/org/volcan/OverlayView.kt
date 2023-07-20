/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.volcan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.LinkedList
import kotlin.math.max
import org.tensorflow.lite.task.vision.detector.Detection

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<Detection> = LinkedList<Detection>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f

    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (result in results) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // Draw bounding box around detected objects
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

//          Definimos texto a mostrar cuando detecte especificamente al volcan de izalco

            // texto original con el salto de línea incluido
            var drawableText = "Volcán de Izalco\nElevación: 1,950m\nSuperficie: 1,225 hectáreas"

            // Divide el texto en líneas
            val lines = drawableText.split("\n")

            // Calcula la altura total necesaria para mostrar todo el texto
            val textHeight = textPaint.fontSpacing
            val totalHeight = textHeight * lines.size

            // Calcula el ancho necesario para el rectángulo de fondo
            var textWidth = 0f
            for (line in lines) {
                val lineWidth = textPaint.measureText(line)
                if (lineWidth > textWidth) {
                    textWidth = lineWidth
                }
            }

            // Calcula las coordenadas para el rectángulo de fondo (Fondo negro)
            val rectLeft = left - Companion.BOUNDING_RECT_TEXT_PADDING // Ajuste para posicionar el rectángulo afuera del cuadro
            val rectTop = top - totalHeight - 2 * Companion.BOUNDING_RECT_TEXT_PADDING // Ajuste para posicionar el rectángulo arriba del texto
            val rectRight = left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING
            val rectBottom = top - Companion.BOUNDING_RECT_TEXT_PADDING // Ajuste para posicionar el rectángulo afuera del cuadro

            // Fondo negro
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, textBackgroundPaint)

            // Dibuja todas las líneas de texto arriba del cuadrado (Texto dentro del fondo negro)
            val fontMetrics = textPaint.fontMetrics
            var currentHeight = top - fontMetrics.ascent - 2 * Companion.BOUNDING_RECT_TEXT_PADDING - totalHeight // Ajuste para posicionar el texto afuera del cuadro
            for (line in lines) {
                canvas.drawText(line, left, currentHeight, textPaint)
                currentHeight += textHeight
            }
        }
    }

    fun setResults(
      detectionResults: MutableList<Detection>,
      imageHeight: Int,
      imageWidth: Int,
    ) {
        results = detectionResults

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
