package UI.Adapter

import Model.UserEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltraining.databinding.ItemUserBinding

class UserAdapter(
    private var userList: MutableList<UserEntity>,
    private val onEditClick: (UserEntity) -> Unit,
    private val onDeleteClick: (UserEntity) -> Unit,
    private val showActions: Boolean = true // default: true
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUsername.text = user.username
        holder.binding.tvName.text = user.name

        if (showActions) {
            holder.binding.btnEdit.visibility = View.VISIBLE
            holder.binding.btnDelete.visibility = View.VISIBLE
            holder.binding.btnEdit.setOnClickListener { onEditClick(user) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        } else {
            holder.binding.btnEdit.visibility = View.GONE
            holder.binding.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount() = userList.size

    fun updateData(newList: List<UserEntity>) {
        if (userList === newList) return // mencegah bug re-assign sama
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

}


