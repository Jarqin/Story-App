package com.dicoding.storyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.databinding.ActivitySigninBinding

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEditListener()
        setButtonEnable()
        setButtonListener()
        supportActionBar?.hide()
    }

    private fun setAlertDialog(param: Boolean, message: String) {
        if (param) {
            AlertDialog.Builder(this).apply {
                setTitle("Yeah!")
                setMessage("Anda berhasil login. Sudah tidak sabar untuk melihat story?")
                setPositiveButton("Lanjut") { _, _ ->
                    val intent = Intent(context, MainActivity::class.java)
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
                binding.passwordEditText.error = null
                setMessage(getString(R.string.sign_in_failed) + ", $message")
                setPositiveButton(getString(R.string.continue_)) { _, _ ->
                    setLoading(false)
                }
                create()
                show()
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
            startActivity(Intent(this@SignInActivity, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setButtonListener() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            loginViewModel.login(email, password).observe(this) {
                when (it) {
                    is ResultResponse.Loading -> {
                        setLoading(true)
                    }
                    is ResultResponse.Success -> {
                        setLoading(false)
                        val user = UserModel(
                            it.data.name,
                            email,
                            password,
                            it.data.userId,
                            it.data.token,
                            true
                        )
                        setAlertDialog(true, getString(R.string.sign_in_success))
                        val userPref = UserPreference.getInstance(dataStore)
                        lifecycleScope.launchWhenStarted {
                            userPref.saveUser(user)
                        }
                    }
                    is ResultResponse.Error -> {
                        setLoading(false)
                        setAlertDialog(false, it.error)
                    }
                }
            }
        }
    }

    private fun setButtonEnable() {
        val passwordResult = binding.passwordEditText.text
        val emailResult = binding.emailEditText.text

        binding.loginButton.isEnabled = passwordResult != null && emailResult != null &&
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