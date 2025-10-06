package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.appbar.MaterialToolbar
import android.util.Patterns
import com.example.diemdanhsinhvien.R

class EditAccountActivity : AppCompatActivity() {

    private lateinit var editTextDisplayName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextLecturerId: TextInputEditText
    private lateinit var editTextDepartment: TextInputEditText
    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextPhoneNumber: TextInputEditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var buttonSave: Button

    private val statusOptions = arrayOf("Active", "Inactive")

    companion object {
        const val EXTRA_DISPLAY_NAME = "displayName"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_LECTURER_ID = "lecturerId"
        const val EXTRA_DEPARTMENT = "department"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PHONE_NUMBER = "phoneNumber"
        const val EXTRA_STATUS = "status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarEditAccount)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        editTextDisplayName = findViewById(R.id.editTextDisplayName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextLecturerId = findViewById(R.id.editTextLecturerId)
        editTextDepartment = findViewById(R.id.editTextDepartment)
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        buttonSave = findViewById(R.id.buttonSave)

        setupSpinner()
        loadCurrentUser()

        buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }

    private fun loadCurrentUser() {
        val intentExtras = intent.extras
        if (intentExtras != null) {
            val displayName = intentExtras.getString(EXTRA_DISPLAY_NAME, "")
            val email = intentExtras.getString(EXTRA_EMAIL, "")
            val lecturerId = intentExtras.getString(EXTRA_LECTURER_ID, "")
            val department = intentExtras.getString(EXTRA_DEPARTMENT, "")
            val title = intentExtras.getString(EXTRA_TITLE, "")
            val phoneNumber = intentExtras.getString(EXTRA_PHONE_NUMBER, "")
            val status = intentExtras.getString(EXTRA_STATUS, "Active")

            editTextDisplayName.setText(displayName)
            editTextEmail.setText(email)
            editTextLecturerId.setText(lecturerId)
            editTextDepartment.setText(department)
            editTextTitle.setText(title)
            editTextPhoneNumber.setText(phoneNumber)

            val statusPosition = statusOptions.indexOf(status).coerceAtLeast(0)
            spinnerStatus.setSelection(statusPosition)
        }
    }

    private fun saveChanges() {
        val displayName = editTextDisplayName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val phoneNumber = editTextPhoneNumber.text.toString().trim()
        val selectedStatus = spinnerStatus.selectedItem.toString()

        if (displayName.isEmpty()) {
            editTextDisplayName.error = "Vui lòng nhập tên hiển thị"
            editTextDisplayName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            editTextEmail.error = "Vui lòng nhập email"
            editTextEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Vui lòng nhập địa chỉ email hợp lệ"
            editTextEmail.requestFocus()
            return
        }

        if (phoneNumber.isEmpty() || !Patterns.PHONE.matcher(phoneNumber).matches()) {
            editTextPhoneNumber.error = "Vui lòng nhập số điện thoại hợp lệ"
            editTextPhoneNumber.requestFocus()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_DISPLAY_NAME, displayName)
        resultIntent.putExtra(EXTRA_EMAIL, email)
        resultIntent.putExtra(EXTRA_LECTURER_ID, editTextLecturerId.text.toString())
        resultIntent.putExtra(EXTRA_DEPARTMENT, editTextDepartment.text.toString())
        resultIntent.putExtra(EXTRA_TITLE, editTextTitle.text.toString())
        resultIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        resultIntent.putExtra(EXTRA_STATUS, selectedStatus)

        setResult(RESULT_OK, resultIntent)

        Toast.makeText(this, "Đã lưu thay đổi.", Toast.LENGTH_SHORT).show()
        finish()
    }
}