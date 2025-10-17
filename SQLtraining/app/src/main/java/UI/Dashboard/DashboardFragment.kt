package UI.Dashboard

import Data.AppDatabase
import Data.UserRepository
import Model.UserEntity
import UI.Adapter.UserAdapter
import Utils.SessionManager
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqltraining.R
import com.example.sqltraining.databinding.FragmentDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserAdapter
    private lateinit var db: AppDatabase
    private var backPressedTime: Long = 0
    private val backToastDuration = 2000L
    private var isFabOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        adapter = UserAdapter(mutableListOf(), {}, {}, showActions = false)
        binding.rvUserList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserList.adapter = adapter

        loadUsers()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < backToastDuration) {
                requireActivity().finish() // keluar dari aplikasi
            } else {
                Toast.makeText(
                    requireContext(),
                    "Tekan sekali lagi untuk keluar",
                    Toast.LENGTH_SHORT
                ).show()
                backPressedTime = currentTime
            }
        }
        binding.btnLogout.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah kamu yakin ingin logout?")
                .setPositiveButton("Ya") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val sessionManager = SessionManager(requireContext())
                    sessionManager.logout()

                    Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

                    findNavController().navigate(
                        R.id.action_dashboardFragment_to_loginFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                }
                .setNegativeButton("Batal") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            dialog.show()

            // 🔹 Ubah background jadi teal + rounded corner
            dialog.window?.setBackgroundDrawable(
                MaterialShapeDrawable().apply {
                    fillColor = ColorStateList.valueOf(Color.parseColor("#FF018786"))
                    shapeAppearanceModel = ShapeAppearanceModel.builder()
                        .setAllCornerSizes(32f)
                        .build()
                    elevation = 12f
                }
            )

            // 🔹 Ubah warna teks jadi putih biar kontras
            dialog.findViewById<TextView>(android.R.id.message)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        binding.fabAddUser.setOnClickListener {
            toggleFabMenu()
        }

        // Add User
        binding.fabAddNewUser.setOnClickListener {
            showAddUserDialog()
            closeFabMenu()
        }

        // Go to User Control page
        binding.fabUserControl.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_UserControlFragment)
            closeFabMenu()
        }
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userList = db.userDao().getAllUsers()  // suspend function aman di sini
            adapter.updateData(userList)
        }
    }

    private fun toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu()
        } else {
            openFabMenu()
        }
    }

    private fun openFabMenu() {

        binding.fabAddNewUser.apply {
            visibility = View.VISIBLE
            translationY = 0f
            alpha = 0f
            animate()
                .translationY(-20f) // jarak ke atas
                .alpha(1f)
                .setInterpolator(OvershootInterpolator()) // efek mantul halus
                .setDuration(300)
                .start()
        }

        binding.fabUserControl.apply {
            visibility = View.VISIBLE
            translationY = 0f
            alpha = 0f
            animate()
                .translationY(-50f) // lebih tinggi sedikit
                .alpha(1f)
                .setInterpolator(OvershootInterpolator())
                .setDuration(350)
                .start()
        }

        binding.fabAddUser.animate()
            .rotation(45f) // efek plus jadi silang
            .setInterpolator(DecelerateInterpolator())
            .setDuration(250)
            .start()
        isFabOpen = true
    }

    private fun closeFabMenu() {
        val b = _binding ?: return // Cegah NullPointerException kalau fragment sudah dihancurkan

        b.fabAddNewUser.animate()
            .translationY(0f)
            .alpha(0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(200)
            .withEndAction {
                _binding?.fabAddNewUser?.visibility = View.GONE
            }
            .start()

        b.fabUserControl.animate()
            .translationY(0f)
            .alpha(0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(200)
            .withEndAction {
                _binding?.fabUserControl?.visibility = View.GONE
            }
            .start()

        // Animasi putar balik ikon utama ke posisi awal
        b.fabAddUser.animate()
            .rotation(0f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(250)
            .withEndAction {
                _binding?.fabAddUser?.setImageResource(R.drawable.ic_add)
            }
            .start()

        isFabOpen = false
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)
        val etUsername = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)

        val dialog = MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setView(dialogView)
            .setTitle("Add New User")
            .setPositiveButton("Simpan", null) // kita override nanti
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etName.text.toString().trim()
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    val existingUser = db.userDao().getUserByUsername(username)
                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val newUser =
                            UserEntity(name = name, username = username, password = password)
                        db.userDao().insertUser(newUser)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            loadUsers()
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(
            MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(Color.parseColor("#FF018786"))
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(32f) // radius 32px, bisa disesuaikan (misal 16f untuk lebih kecil)
                    .build()
                elevation = 12f
            }
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
