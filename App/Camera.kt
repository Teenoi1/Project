package com.example.app1952

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app1952.databinding.FragmentCameraBinding
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.IOException

class Camera : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    lateinit var bitmap: Bitmap
    lateinit var imgview: ImageView
    lateinit var townList: List<String>
    private var interpreter: Interpreter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgview = binding.imageView
        val tv: TextView = binding.textView
        val select: Button = binding.button
        val predict: Button = binding.button2

        try {
            val fileName = "label.txt"
            val inputString = requireActivity().assets.open(fileName).bufferedReader().use { it.readText() }
            townList = inputString.split("\n").filter { it.isNotBlank() }

            interpreter = loadModelFile("model.tflite")

        } catch (e: Exception) {
            tv.text = "Error reading labels or model: ${e.message}"
            return
        }

        //กดเลือกภาพ
        select.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        //ทำนาย
        predict.setOnClickListener {
            if (::bitmap.isInitialized) {
                try {
                    val resized: Bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                    val tensorImage = TensorImage(DataType.FLOAT32)
                    tensorImage.load(resized)
                    val byteBuffer = tensorImage.buffer

                    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224,  224, 3), DataType.FLOAT32)
                    inputFeature0.loadBuffer(byteBuffer)

                    val outputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, townList.size), DataType.FLOAT32)

                    interpreter?.run(inputFeature0.buffer, outputFeature0.buffer.rewind())

                    val probabilities = outputFeature0.floatArray
                    val maxIndex = getMax(probabilities)

                    if (maxIndex < townList.size) {
                        val result = "Prediction: ${townList[maxIndex]} (${probabilities[maxIndex] * 100}%)\n\n"
                        val allProbabilities = probabilities.mapIndexed { index, probability ->
                            "${townList.getOrNull(index) ?: "Unknown"}: ${probability * 100}%"
                        }.joinToString("\n")
                        tv.text = "$result\n"
                    } else {
                        tv.text = "Unknown label"
                    }

                } catch (e: Exception) {
                    tv.text = "Error during prediction: ${e.message}"
                }
            } else {
                tv.text = "Please select an image first."
            }
        }
    }

    private fun loadModelFile(modelPath: String): Interpreter {
        val assetFileDescriptor = requireContext().assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            try {
                if (uri != null) {
                    imgview.setImageURI(uri)
                    bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMax(arr: FloatArray): Int {
        var ind = 0
        var max = Float.MIN_VALUE
        for (i in arr.indices) {
            if (arr[i] > max) {
                ind = i
                max = arr[i]
            }
        }
        return ind
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        interpreter?.close()
    }
}
