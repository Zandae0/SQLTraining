package UI.UserControl

import Data.AppDatabase
import Model.UserEntity
import UI.Adapter.UserAdapter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqltraining.R
import com.example.sqltraining.databinding.FragmentUsercontrolBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserControlFragment : Fragment() {

    private var _binding: FragmentUsercontrolBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: UserAdapter
    private var fullUserList = mutableListOf<UserEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("UserControlFragment", "onCreateView dipanggil")
        _binding = FragmentUsercontrolBinding.inflate(inflater, container, false)

        try {
            db = AppDatabase.getDatabase(requireContext())
            Log.d("UserControlFragment", "Database berhasil diinisialisasi")
        } catch (e: Exception) {
            Log.e("UserControlFragment", "Gagal inisialisasi database: ${e.message}")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("UserControlFragment", "onViewCreated dipanggil")

        try {
            // Inisialisasi adapter
            adapter = UserAdapter(mutableListOf(),
                onEditClick = { user ->
                    Log.d("UserControlFragment", "Klik edit pada user: ${user.username}")
                    showEditDialog(user)
                },
                onDeleteClick = { user ->
                    Log.d("UserControlFragment", "Klik delete pada user: ${user.username}")
                    confirmDelete(user)
                },
                showActions = true
            )

            // Setup RecyclerView
            binding.rvUserList.layoutManager = LinearLayoutManager(requireContext())
            binding.rvUserList.adapter = adapter
            Log.d("UserControlFragment", "RecyclerView dan adapter di-setup")

            // Load semua user
            loadUsers()

            // Listener pencarian
            binding.etSearchUser.addTextChangedListener { text ->
                val query = text.toString().trim()
                Log.d("UserControlFragment", "Pencarian user: $query")
                filterUsers(query)
            }
        } catch (e: Exception) {
            Log.e("UserControlFragment", "Error di onViewCreated: ${e.message}", e)
        }
    }

    private fun loadUsers() {
        Log.d("UserControlFragment", "loadUsers() dipanggil")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val users = db.userDao().getAllUsers()
                Log.d("UserControlFragment", "Jumlah user ditemukan: ${users.size}")

                withContext(Dispatchers.Main) {
                    fullUserList.clear()
                    fullUserList.addAll(users)
                    adapter.updateData(fullUserList)
                    Log.d("UserControlFragment", "Data user berhasil diupdate ke adapter")
                }
            } catch (e: Exception) {
                Log.e("UserControlFragment", "Error loadUsers: ${e.message}", e)
            }
        }
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isEmpty()) {
            fullUserList
        } else {
            fullUserList.filter { it.username.contains(query, ignoreCase = true) }.toMutableList()
        }
        Log.d("UserControlFragment", "filterUsers() hasil: ${filtered.size} data cocok")
        adapter.updateData(filtered)
    }

    private fun confirmDelete(user: UserEntity) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus User")
            .setMessage("Apakah kamu yakin ingin menghapus '${user.username}'?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        db.userDao().deleteUser(user)
                        Log.d("UserControlFragment", "User ${user.username} dihapus")
                        loadUsers()
                    } catch (e: Exception) {
                        Log.e("UserControlFragment", "Error delete user: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(Color.parseColor("#FF018786"))
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(32f) // rounded corner radius
                    .build()
                elevation = 12f
            }
        )
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.WHITE)
    }


    private fun showEditDialog(user: UserEntity) {
        Log.d("UserControlFragment", "showEditDialog() untuk user: ${user.username}")

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val etNewName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            R.id.etNewName
        )
        etNewName.setText(user.name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Nama Pengguna")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialogInterface, _ ->
                val newName = etNewName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val updatedUser = user.copy(name = newName)
                            db.userDao().updateUser(updatedUser)
                            Log.d("UserControlFragment", "User ${user.username} berhasil diupdate")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                        } catch (e: Exception) {
                            Log.e("UserControlFragment", "Error update user: ${e.message}")
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(
            MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(Color.parseColor("#FF018786"))
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(32f) // radius 32px, bisa disesuaikan (misal 16f untuk lebih kecil)
                    .build()
            }
        )
    }



    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvUserList.adapter = null
        _binding = null
    }
}