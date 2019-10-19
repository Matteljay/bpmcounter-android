package eth.matteljay.bpmcounter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val keyCountdown = "countDownAmount"
    private lateinit var orientationModel: OrientationModel

    inner class GestureTap : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            printConsoleText(getString(R.string.consoleDefaultText))
            resetCount()
            updateButtonState()
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        orientationModel = ViewModelProviders.of(this).get(OrientationModel::class.java)
        val consoleBox = findViewById<TextView>(R.id.consoleBox)
        consoleBox.movementMethod = ScrollingMovementMethod()
        val gestureDetector = GestureDetector(this, GestureTap())
        consoleBox.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { buttonClicked() }

        if (savedInstanceState == null) {
            orientationModel.consoleOutput = getString(R.string.consoleDefaultText)
            orientationModel.countDownAmount = readCountPref()
            resetCount()
        } else {
            consoleBox.text = orientationModel.consoleOutput
        }
        updateButtonState()
    }

    private fun readCountPref(): Int {
        val sharedPref = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        return sharedPref.getInt(keyCountdown, 12)
    }

    private fun writeCountPref(value: Int) {
        val sharedPref = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(keyCountdown, value)
            commit()
        }
    }

    private fun updateButtonState() {
        button.text = orientationModel.currentCount.toString()
        if (orientationModel.currentCount >= orientationModel.countDownAmount) {
            button.setBackgroundResource(R.color.greenBtn)
        } else {
            button.setBackgroundResource(R.color.redBtn)
        }
    }

    private fun resetCount() {
        orientationModel.currentCount = orientationModel.countDownAmount
    }

    private fun printConsoleText(text: String) {
        orientationModel.consoleOutput = text
        consoleBox.text = orientationModel.consoleOutput
        consoleBox.scrollY = 0
    }

    private fun writeBPM() {
        val calcBPM = (orientationModel.countDownAmount * 60000) /
                (System.currentTimeMillis() - orientationModel.startTime)
        printConsoleText("$calcBPM BPM\n" + consoleBox.text.toString())
    }

    private fun buttonClicked() {
        if (orientationModel.currentCount >= orientationModel.countDownAmount) {
            orientationModel.startTime = System.currentTimeMillis()
        }
        orientationModel.currentCount--
        if (orientationModel.currentCount < 1) {
            writeBPM()
            resetCount()
        }
        updateButtonState()
    }

    private fun createSettingsDialog() {
        val lay = LinearLayout(this)
        lay.setPadding(22, 22, 22, 0)
        val editText = EditText(this)
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.setRawInputType(Configuration.KEYBOARD_12KEY)
        editText.hint = orientationModel.countDownAmount.toString()
        lay.addView(editText)
        AlertDialog.Builder(this)
            .setTitle("Countdown amount")
            .setView(lay)
            .setPositiveButton(android.R.string.ok) { _, _ -> settingsOkay(editText.text.toString()) }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }

    private fun settingsOkay(text: String) {
        val input = text.toIntOrNull() ?: 0
        if (input < 2) {
            return
        }
        writeCountPref(input)
        orientationModel.countDownAmount = input
        resetCount()
        updateButtonState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> createSettingsDialog()
        }
        return super.onOptionsItemSelected(item)
    }

}
