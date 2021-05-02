package com.example.musicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import android.util.Log
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import java.util.HashMap

val database = Firebase.database
val myRef = database.getReference("UserListing")

data class User(val username: String? = null, val email: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

fun writeNewUser(userId: String, name:String, email: String) {
    val user = User(name, email)
    myRef.child("users").child(userId).setValue(user)
}

fun addSongToPlaylist(userId: String, playListId: String, songId: String) {
    val sizeOfPlaylist = myRef.child("users").child(userId).child(playListId).child("playListSize").get().toString()
    val intSizeOfPlaylist = sizeOfPlaylist.toInt()
    myRef.child("users").child(userId).child(playListId).child(sizeOfPlaylist.toString()).setValue(songId)
    myRef.child("users").child(userId).child(playListId).child("playListSize").setValue((intSizeOfPlaylist+1).toString())
}

fun createPlaylistEmpty(userId: String, playListNumber: Int, playListName: String) {
    val numOfPlaylist = myRef.child("users").child(userId).child("num_playlists").get().toString()
    val intNumOfPlaylist = numOfPlaylist.toInt()
    myRef.child("users").child(userId).child(numOfPlaylist.toString()).child("name").setValue(playListName)
    myRef.child("users").child(userId).child(numOfPlaylist.toString()).child("playListSize").setValue("0")
    myRef.child("users").child(userId).child(numOfPlaylist).setValue((intNumOfPlaylist+1).toString())
}

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tv_login.setOnClickListener {
            val intent =
                Intent(this@RegisterActivity, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btn_register.setOnClickListener {
            when {
                TextUtils.isEmpty(et_register_email.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_password.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_name.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter name.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    val email: String = et_register_email.text.toString().trim {it <= ' '}
                    val password: String = et_register_password.text.toString().trim {it <= ' '}
                    val name: String = et_register_name.text.toString().trim {it <= ' '}

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                writeNewUser(firebaseUser.uid, name, email)

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "You were registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent =
                                    Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText (
                                    this@RegisterActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }
}