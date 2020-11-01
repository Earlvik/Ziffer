package com.vikslop.ziffer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import androidx.preference.PreferenceManager
import com.vikslop.ziffer.R
import com.vikslop.ziffer.databinding.ActivityMainBinding
import com.vikslop.ziffer.tts.Pronouncer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var pronouncer: Pronouncer? = null
    private var currentNumber: Int? = null
    private var previousNumber: Int? = null
    private var totalScore = ObservableInt(0)
    private var timer = Timer()
    private var countDownTimer: CountDownTimer? = null
    private var remainingTime = 0L

    private var totalTime = 60.0f
    private var delay = 0.5f
    private var speed = 1.0f
    private var addOnCorrect = 1.0f
    private var highScore = ObservableInt(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        binding.totalScore = this.totalScore
        binding.highScore = this.highScore
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    override fun onResume() {
        super.onResume()

        pronouncer = Pronouncer(applicationContext)
        button_start.setOnClickListener { v -> start() }
        button_stop.setOnClickListener { v -> stop() }
        button_settings.setOnClickListener { v ->
            run {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        number_input.setOnEditorActionListener { v, actionId, event -> checkInput(actionId) }
        loadSettings()
    }

    override fun onPause() {
        stop()
        super.onPause()
    }

    private fun start() {
        this.timer = Timer()
        this.totalScore.set(0)
        setCountdown(1, 1)
        countdown_line.visibility = View.VISIBLE
        this.countDownTimer = createCountDown((totalTime * 1000).toLong())
        button_start.isEnabled = false
        button_stop.isEnabled = true
        number_input.visibility = View.VISIBLE
        number_input.isFocusableInTouchMode = true
        number_input.isFocusable = true
        answer_text.text = ""
        number_input.requestFocus()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
        pronounceRandom()
        this.countDownTimer?.start()
    }

    private fun stop() {
        timer.cancel()
        countDownTimer?.cancel()
        countdown_line.visibility = View.INVISIBLE
        button_start.isEnabled = true
        button_stop.isEnabled = false
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(number_input.windowToken, 0)
        number_input.visibility = View.INVISIBLE
        answer_text.text = ""
        if (this.totalScore.get() > this.highScore.get()) {
            this.highScore.set(this.totalScore.get())
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
            sharedPreferences.edit().putInt("highScore", this.highScore.get()).apply()
        }
    }

    private fun pronounceRandom() {
        while (currentNumber == previousNumber) {
            currentNumber = Random.nextInt(11, 99)
        }
        previousNumber = currentNumber;
        pronouncer?.pronounce(currentNumber!!, this.speed)
    }

    private fun checkInput(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val input = number_input.text.toString()
            if (input.isNotEmpty()) {
                currentNumber?.let {
                    if (input.toInt() == currentNumber) {
                        totalScore.set(totalScore.get() + 1)
                        answer_text.setTextColor(Color.GREEN)
                        answer_text.text = getString(R.string.label_correct)

                        addOnCorrect_text.text = "+${addOnCorrect}"
                        addOnCorrect_text.alpha = 1.0f
                        addOnCorrect_text.visibility = View.VISIBLE
                        addOnCorrect_text.animate()
                            .alpha(0.0f)
                            .setStartDelay(250)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation);
                                    addOnCorrect_text.visibility = View.INVISIBLE;
                                }
                            });

                        countDownTimer?.cancel()
                        countDownTimer =
                            createCountDown(remainingTime + (addOnCorrect * 1000).toLong())
                        countDownTimer?.start()
                    } else {
                        answer_text.setTextColor(Color.RED)
                        answer_text.text = getString(R.string.label_incorrect, currentNumber)
                    }
                }
                number_input.text.clear()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        pronounceRandom()
                    }
                }, (this.delay * 1000).toLong())
            }
        }
        return true
    }

    private fun setCountdown(msTotal: Long, msLeft: Long) {
        guideline_countdown.setGuidelinePercent(msLeft.toFloat() / msTotal.toFloat())
    }

    private fun createCountDown(time: Long): CountDownTimer {
        this.remainingTime = time
        val maxTime = (totalTime * 1000).toLong()
        return object : CountDownTimer(time, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                runOnUiThread { setCountdown(maxTime, millisUntilFinished) }
            }

            override fun onFinish() {
                stop()
            }
        }
    }

    private fun loadSettings() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        this.highScore.set(sharedPreferences.getInt("highScore", 0))
        this.totalTime = sharedPreferences.getString("time", "60.0")!!.toFloat()
        this.delay = sharedPreferences.getString("delay", "0.5")!!.toFloat()
        this.speed = sharedPreferences.getString("speed", "1.0")!!.toFloat()
        this.addOnCorrect = sharedPreferences.getString("addOnCorrect", "1.0")!!.toFloat()
    }
}