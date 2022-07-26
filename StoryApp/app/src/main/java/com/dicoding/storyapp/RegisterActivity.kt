package com.dicoding.storyapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel : RegisterViewModel by viewModels{ ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setEditListener()
        setButtonListener()
        setButtonEnable()
        supportActionBar?.hide()
    }

    private fun setButtonListener() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            registerViewModel.register(name, email, password).observe(this){
                when (it) {
                    is ResultResponse.Loading -> {
                        setLoading(true)
                    }
                    is ResultResponse.Success -> {
                        setLoading(false)
                        setAlertDialog(true, getString(R.string.register_success))
                    }
                    is ResultResponse.Error -> {
                        setLoading(false)
                        setAlertDialog(false, it.error)
                    }
                }
            }
        }
    }

    private fun setEditListener() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {

            }
        })

        binding.tvPilihan.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, SignInActivity::class.java))
            finish()
        }
    }

    private fun setAlertDialog(param: Boolean, message: String) {
        if (param) {
            AlertDialog.Builder(this).apply {
                setTitle("Yeah!")
                setMessage("Akunnya sudah jadi nih. Yuk, login dan melihat story.")
                setPositiveButton("Lanjut") { _, _ ->
                    val intent = Intent(context, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        }
        else {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.register_failed)+", $message")
                setPositiveButton(getString(R.string.continue_)) { _, _ ->
                    setLoading(false)
                }
              create()
              show()
            }
        }
    }

    private fun setButtonEnable() {
        binding.signupButton.isEnabled =
              binding.emailEditText.text.toString().isNotEmpty() &&
              binding.passwordEditText.text.toString().isNotEmpty() &&
              binding.passwordEditText.text.toString().length >= 6 &&
              Helper.isEmailValid(binding.emailEditText.text.toString())
    }

    private fun setLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        }
        else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}