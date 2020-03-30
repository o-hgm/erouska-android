package cz.covid19cz.app.ui.login

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentLoginBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.utils.*
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject


class LoginFragment :
    BaseFragment<FragmentLoginBinding, LoginVM>(R.layout.fragment_login, LoginVM::class) {

    private lateinit var views: List<View>
    private lateinit var requestQueue: RequestQueue

    override fun onStart() {
        super.onStart()
        viewModel.state.observe(this) {
            updateState(it)
        }
        requestQueue = Volley.newRequestQueue(this.context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val handled = onBackPressed()
                return if (handled) {
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views = listOf(
            login_image,
            login_progress,
            login_verif_activate_btn,
            login_verif_code_send_btn,
            error_message,
            error_image,
            login_verif_phone_input,
            login_verif_code_input,
            login_title,
            login_desc,
            login_verif_phone,
            login_verif_code,
            login_statement,
            error_button_back,
            phone_number_code
        )

        setupListeners()

        enableUpInToolbar(true)
    }

    private fun setupListeners() {
        login_verif_phone_input.addTextChangedListener(afterTextChanged = {
            login_verif_phone.isErrorEnabled = false
        })
        login_verif_phone_input.setOnDoneListener {
            login_verif_activate_btn.hideKeyboard()
            viewModel.phoneNumberEntered(login_verif_phone_input.text.toString())
        }
        login_verif_activate_btn.setOnClickListener {
            login_verif_activate_btn.hideKeyboard()
            viewModel.phoneNumberEntered(login_verif_phone_input.text.toString())
        }
        login_verif_code_input.addTextChangedListener(afterTextChanged = {
            login_verif_code.isErrorEnabled = false
        })
        login_verif_code_input.setOnDoneListener {
            login_verif_code_send_btn.hideKeyboard()
            viewModel.codeEntered(login_verif_code_input.text.toString())
        }
        login_verif_code_send_btn.setOnClickListener {
            login_verif_code_send_btn.hideKeyboard()
            viewModel.codeEntered(login_verif_code_input.text.toString())
        }
        error_button_back.setOnClickListener {
            viewModel.backPressed()
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            is EnterPhoneNumber -> {
                show(
                    login_image,
                    login_title,
                    login_desc,
                    login_verif_phone,
                    login_verif_phone_input,
                    login_statement,
                    login_verif_activate_btn
                )
                login_verif_phone.isErrorEnabled = state.invalidPhoneNumber
                if (state.invalidPhoneNumber) {
                    login_verif_phone.error = getString(R.string.login_phone_input_error)
                }
            }
            is EnterCode -> {
                show(
                    phone_number_code,
                    login_verif_code_input,
                    login_verif_code,
                    login_verif_code_send_btn
                )
                phone_number_code.text =
                    getString(R.string.login_phone_number_sms_sent, state.phoneNumber)
                login_verif_code.isErrorEnabled = state.invalidCode
                if (state.invalidCode) {
                    login_verif_code.error = getString(R.string.login_code_input_error)
                }
                login_verif_code_input.focusAndShowKeyboard()
            }
            is CodeReadAutomatically -> {
                show(
                    phone_number_code,
                    login_verif_code_input,
                    login_verif_code,
                    login_verif_code_send_btn
                )
                phone_number_code.setText(R.string.login_code_read_automatically)
                login_verif_code.isErrorEnabled = false
                login_verif_code_input.setText(state.code)
            }
            SigningProgress -> show(login_progress)
            is LoginError -> showError(state.text)
            is SignedIn -> showSignedIn()
            StartVerification -> verifyPhoneNumber()
        }

        if (state is EnterCode || state is CodeReadAutomatically) {
            activity?.setTitle(R.string.login_verification_code)
        } else {
            activity?.setTitle(R.string.login_toolbar_title)
        }
    }

    private fun showSignedIn() {
        if (navController().currentDestination?.id == R.id.nav_login) {
            navigate(
                R.id.action_nav_login_to_nav_dashboard, null,
                Builder()
                    .setPopUpTo(
                        R.id.nav_graph,
                        true
                    ).build()
            )
        }
    }

    private fun showError(text: Text?) {
        error_message.text = text?.toCharSequence(requireContext())
        show(error_message, error_button_back, error_image)
    }

    private fun verifyPhoneNumber() {
        L.e("verifyPhoneNumber")
        show(login_progress)
        val phoneNumber = login_verif_phone_input.text.toString()
        viewModel.registerPhone(requestQueue, phoneNumber)
    }

    private fun show(vararg views: View) {
        views.forEach {
            it.visibility = View.VISIBLE
        }
        this.views.subtract(views.toList()).forEach {
            it.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.state.value !is EnterPhoneNumber) {
            viewModel.backPressed()
            true
        } else false
    }
}
