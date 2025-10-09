package com.example.diemdanhsinhvien.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.activity.LoginActivity
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IPublicClientApplication
import android.provider.MediaStore
import android.net.Uri
import android.content.pm.PackageManager
import com.example.diemdanhsinhvien.activity.EditAccountActivity
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import java.io.IOException
import com.microsoft.identity.client.exception.MsalException

class AccountFragment : Fragment() {

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var imageViewAvatar: ImageView
    private val PICK_IMAGE_REQUEST = 123
    private lateinit var userIdTextView: TextView
    private lateinit var editAccountButton: Button
    private val EDIT_ACCOUNT_REQUEST_CODE = 124

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameTextView = view.findViewById(R.id.textViewUserName)
        userEmailTextView = view.findViewById(R.id.textViewUserEmail)
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar)
        userIdTextView = view.findViewById(R.id.textViewUserId)
        editAccountButton = view.findViewById(R.id.buttonEditAccount)

        PublicClientApplication.createSingleAccountPublicClientApplication(requireContext(),
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    loadAccountInfo()
                }
                override fun onError(exception: MsalException) {
                    Log.e("AccountFragment", "Error creating MSAL application: $exception")
                }
            })
            
        imageViewAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }

                override fun onError(exception: MsalException) {
                    Log.e("AccountFragment", "Error signing out: $exception")
                    Toast.makeText(requireContext(), "Đăng xuất thất bại.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        editAccountButton.setOnClickListener {
            val intent = Intent(requireActivity(), EditAccountActivity::class.java)
            startActivityForResult(intent, EDIT_ACCOUNT_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_ACCOUNT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val displayName = data?.getStringExtra("displayName")
            val email = data?.getStringExtra("email")
            val lecturerId = data?.getStringExtra("lecturerId")
            val department = data?.getStringExtra("department")
            val title = data?.getStringExtra("title")
            val phoneNumber = data?.getStringExtra("phoneNumber")
            val status = data?.getStringExtra("status")

            userNameTextView.text = displayName ?: ""
            userEmailTextView.text = email ?: "N/A"
            userIdTextView.text = lecturerId?.let { "Mã GV: $it" } ?: "N/A"

            Log.d("AccountFragment", "Status: $status")

            Toast.makeText(requireContext(), "Đã cập nhật thông tin tài khoản.", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)
                    imageViewAvatar.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadAccountInfo() {
        mSingleAccountApp?.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                activity?.runOnUiThread {
                    if (activeAccount != null) {
                        val displayName = activeAccount.claims?.get("name") as? String
                        val userId = activeAccount.claims?.get("oid") as? String

                        userNameTextView.text = displayName ?: getString(R.string.not_logged_in)
                        userEmailTextView.text = activeAccount.username
                        userIdTextView.text = if (userId != null) getString(R.string.account_id_label, userId) else ""

                        Log.d("AccountFragment", "User ID: $userId")
                    }
                }
            }

            override fun onError(exception: MsalException) {
                Log.e("AccountFragment", "Lỗi khi lấy thông tin tài khoản: $exception")
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                // Not implemented for this scenario
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Quyền đã được cấp, mở thư viện ảnh
                } else {
                    // Quyền bị từ chối, thông báo cho người dùng
                }
            }
        }
    }
}