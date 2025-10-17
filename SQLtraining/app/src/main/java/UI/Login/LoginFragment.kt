package UI.Login

import Data.AppDatabase
import Utils.SessionManager
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.sqltraining.R
import com.example.sqltraining.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
        val text = "Don't have an account? Register"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Register")
        val endIndex = startIndex + "Register".length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // tanpa underline
                ds.color = Color.parseColor("#FF3700B3") // warna biru
            }
        }

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvRegisterText.text = spannableString
        binding.tvRegisterText.movementMethod = LinkMovementMethod.getInstance()
        binding.tvRegisterText.highlightColor = Color.TRANSPARENT

        return binding.root
    }

    private fun loginUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUserByUsername(username)

            withContext(Dispatchers.Main) {
                if (user == null) {
                    Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                } else if (user.password != password) {
                    Toast.makeText(requireContext(), "Password salah", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Login berhasil, selamat datang ${user.name}", Toast.LENGTH_SHORT).show()
                    val sessionManager = SessionManager(requireContext())
                    sessionManager.saveLoginSession(username)
                    findNavController().navigate(
                        R.id.dashboardFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true) // hapus login dari backstack
                            .build()
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}