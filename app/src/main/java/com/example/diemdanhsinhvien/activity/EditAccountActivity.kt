package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.example.diemdanhsinhvien.R

class EditAccountActivity : AppCompatActivity() {

    private lateinit var editTextDisplayName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var textInputDisplayName: TextInputLayout
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_edit_account)

        editTextDisplayName = findViewById(R.id.editTextDisplayName)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSave = findViewById(R.id.buttonSave)

        // Giả sử bạn có một hàm để lấy thông tin người dùng hiện tại
        loadCurrentUser()

        buttonBack = findViewById(R.id.buttonBack)

        buttonSave.setOnClickListener {
            saveChanges()
        }
        super.onCreate(savedInstanceState)
    }


    private fun loadCurrentUser() {
        // TODO: Lấy thông tin người dùng hiện tại từ nguồn dữ liệu (ví dụ: database, API)
        // Ví dụ:
        val displayName = "Tên người dùng"
        val email = "email@example.com"

        editTextDisplayName.setText(displayName)
        editTextEmail.setText(email)
    }
    
    private fun saveChanges() {
        // TODO: Lấy dữ liệu từ các EditText, kiểm tra tính hợp lệ, và cập nhật thông tin tài khoản
        val displayName = editTextDisplayName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()

        // TODO: Kiểm tra tính hợp lệ của dữ liệu
        if (displayName.isEmpty()) {
            editTextDisplayName.error = "Vui lòng nhập tên hiển thị"
        } else if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Cập nhật thông tin tài khoản (ví dụ: database, API)
        // Sau khi cập nhật thành công, hiển thị thông báo
        Toast.makeText(this, "Đã lưu thay đổi.", Toast.LENGTH_SHORT).show()
        finish()
    }
    fun back(view: View){
        finish()
    }
}
