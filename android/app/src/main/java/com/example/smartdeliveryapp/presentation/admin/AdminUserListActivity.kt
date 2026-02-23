package com.example.smartdeliveryapp.presentation.admin

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartdeliveryapp.databinding.ActivityAdminUserListBinding
import com.example.smartdeliveryapp.databinding.ItemUserAdminBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class AdminUserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserListBinding
    @Inject lateinit var sharedPreferences: SharedPreferences
    
    private val usersList = mutableListOf<JSONObject>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = UserAdapter(usersList) { userId, action ->
            if (action == "DELETE") deleteUser(userId)
        }
        
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sharedPreferences.getString("jwt_token", "") ?: ""
                val baseUrl = "http://192.168.100.6:5001"

                val request = Request.Builder()
                    .url("$baseUrl/api/admin/users")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val jsonArray = JSONArray(body)
                    usersList.clear()
                    for (i in 0 until jsonArray.length()) {
                        usersList.add(jsonArray.getJSONObject(i))
                    }
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminUserListActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sharedPreferences.getString("jwt_token", "") ?: ""
                val baseUrl = "http://192.168.100.6:5001"

                val request = Request.Builder()
                    .url("$baseUrl/api/admin/users/$userId")
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminUserListActivity, "Utilisateur supprimé", Toast.LENGTH_SHORT).show()
                        fetchUsers()
                    }
                }
            } catch (e: Exception) { }
        }
    }

    inner class UserAdapter(private val users: List<JSONObject>, private val onAction: (String, String) -> Unit) :
        RecyclerView.Adapter<UserAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemUserAdminBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemUserAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.itemBinding.apply {
                tvUsername.text = user.optString("username")
                tvEmail.text = user.optString("email")
                tvRoleBadge.text = user.optString("role").uppercase()
                
                btnDelete.setOnClickListener {
                    onAction(user.optString("_id"), "DELETE")
                }
            }
        }

        override fun getItemCount() = users.size
    }
}
