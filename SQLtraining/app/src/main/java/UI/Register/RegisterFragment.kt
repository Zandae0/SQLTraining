package UI.Register

import Data.AppDatabase
import Model.UserEntity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sqltraining.R
import com.example.sqltraining.databinding.FragmentRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        return binding.root
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek username sudah ada atau belum
        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getUserByUsername(username)
            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Username sudah terdaftar", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newUser = UserEntity(
                    name = name,
                    username = username,
                    password = password
                )
                db.userDao().insertUser(newUser)

                withContext(Dispatchers.Main) {
                    clearForm()

                    val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Registrasi Berhasil 🎉")
                        .setMessage("Akun kamu sudah dibuat. Klik di mana saja untuk kembali ke halaman login.")
                        .setCancelable(true)
                        .create()

                    dialog.show()
                    dialog.window?.setBackgroundDrawable(
                        MaterialShapeDrawable().apply {
                            fillColor = ColorStateList.valueOf(Color.parseColor("#FF018786"))
                            shapeAppearanceModel = ShapeAppearanceModel.builder()
                                .setAllCornerSizes(32f)
                                .build()
                            elevation = 12f
                        }
                    )

                    dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.white)
                    )
                    // Tutup dialog kalau user klik di luar atau tombol back
                    dialog.setOnDismissListener {
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                }


            }
        }

    }

    private fun clearForm() {
        binding.etName.text?.clear()
        binding.etUsername.text?.clear()
        binding.etPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}