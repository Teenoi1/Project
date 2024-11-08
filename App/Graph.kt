package com.example.app1952

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Graph : Fragment() {

    private lateinit var tflite: Interpreter
    private lateinit var resultTextView: TextView
    private lateinit var input1: EditText
    private lateinit var input2: EditText
    private lateinit var input3: EditText
    private lateinit var runModelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // โหลดโมเดล
        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graph, container, false)

        // เชื่อมTextView
        resultTextView = view.findViewById(R.id.resultTextView)
        input1 = view.findViewById(R.id.input1)
        input2 = view.findViewById(R.id.input2)
        input3 = view.findViewById(R.id.input3)
        runModelButton = view.findViewById(R.id.runModelButton)

        //กดปุ่มแล้วรันโมเดล
        runModelButton.setOnClickListener {
            runModel()
        }

        return view
    }

    //โหลดโมเดล
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = requireContext().assets.openFd("tree_growth_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun runModel() {

        val value1 = input1.text.toString().toFloatOrNull() ?: 0.0f
        val value2 = input2.text.toString().toFloatOrNull() ?: 0.0f
        val value3 = input3.text.toString().toFloatOrNull() ?: 0.0f

        //Input
        val input = arrayOf(floatArrayOf(value1, value2, value3))
        val output = Array(1) { FloatArray(1) }


        tflite.run(input, output)


        val result = output[0][0]
        if (result > 0.5) {
            resultTextView.text = "result: ต้นไม้เจริญเติบโตได้ดี"
        } else {
            resultTextView.text = "result: ต้นไม้เจริญเติบโตไม่ดี"
        }
    }
}
