package com.example.musicplayer.ui.Player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

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
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.example.musicplayer.R
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import kotlin.concurrent.schedule
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
//import com.google.firebase.auth


//val user = Firebase.auth.currentUser
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
    Log.d("Main","oooooo")
    val sizeOfPlaylist = myRef.child("users").child(userId).child(playListId).child("playListSize").get().toString()
    val intSizeOfPlaylist = sizeOfPlaylist.toInt()
    myRef.child("users").child(userId).child(playListId).child(sizeOfPlaylist.toString()).setValue(songId)
    myRef.child("users").child(userId).child(playListId).child("playListSize").setValue((intSizeOfPlaylist+1).toString())
}

fun createPlaylistEmpty(userId: String, playListNumber: Int, playListName: String) {
    Log.d("Main","oooooo")
    val numOfPlaylist = myRef.child("users").child(userId).child("num_playlists").get().toString()
    val intNumOfPlaylist = numOfPlaylist.toInt()
    myRef.child("users").child(userId).child("username").setValue("As")
    myRef.child("users").child(userId).child(numOfPlaylist.toString()).child("name").setValue(playListName)
    myRef.child("users").child(userId).child(numOfPlaylist.toString()).child("playListSize").setValue("0")
    myRef.child("users").child(userId).child(numOfPlaylist).setValue((intNumOfPlaylist+1).toString())
}




class PlayerFragment : Fragment() {


    private val clientId = "109ee11122e44c06ba78172353286959"
    private val redirectUri = "https://com.example.musicplayer/callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    var paused: Boolean=true
    var trackid:String="spotify:track:4fgJe7zBjVXvs8HxtE21Y9"
    //lateinit var bitmap:Bitmap
    var fn: Boolean=true
    private lateinit var playerViewModel: PlayerViewModel
    var artistname:String=""
    var songname:String=""
    val playlist = listOf("37i9dQZF1DX2sUQwD7tbmL", "37i9dQZF1E4s222TTyN7hT", "37i9dQZF1DX0XUfTFmNBRM", "37i9dQZF1DXd8cOUiye1o2", "37i9dQZF1DXdpQPPZq3F7n")
    var currenttrack=""
    private fun hi() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this.getContext(), connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
               // Log.d("MainActivity", "Connected! Yay!")

            }
            override fun onFailure(throwable: Throwable) {
               // Log.e("MainActivity", throwable.message, throwable)
                // Something went wrong when attempting to connect! Handle errors here
            }
        })
    }

    private fun connected() {
        if(fn){
            spotifyAppRemote?.let {
                it.playerApi.play("spotify:track:4fgJe7zBjVXvs8HxtE21Y9")
               // Log.d("MainActivity","inside fn")
            }
            fn=false
        }
        if(paused){
            spotifyAppRemote?.let {
                it.playerApi.resume()
              //  Log.d("MainActivity","played")
            }
            paused=false
        }
        else{
            spotifyAppRemote?.let {
                it.playerApi.pause()
                //Log.d("MainActivity","paused")
            }
            paused=true
        }


    }



    private fun next(){
        spotifyAppRemote?.let {
            val nexttrack=getnewtrack(true)
            it.playerApi.queue(nexttrack)
            Log.d("MainActivity","ok done next queue")
        }
    }

    //generates a new track id and returnsit and changes trackid
    private fun getnewtrack(rand:Boolean):String{
        if(rand){
            Log.d("MainActivity","sent new id ")
            return "spotify:track:1gwO79MdYdumgIjxq8eCxB"
        }
        else{

            return "spotify:track:4fgJe7zBjVXvs8HxtE21Y9"
        }

    }

    //pressing skip button plays next song in queue if it is there or adds one to queue and plays and then changes detaisl
    private fun skip(){
        spotifyAppRemote?.let {

            val nexttrack=getnewtrack(true)
            it.playerApi.queue(nexttrack)
            it.playerApi.skipNext()

        }
    }
    // change the artist names and album names and image uri and change trackid
    private fun changedetails(){
       // super.onCreate(savedInstanceState)
        spotifyAppRemote?.let {
           // var imageUri: ImageUri ="spotify:image:mmmm"
            it.playerApi.subscribeToPlayerState().setEventCallback {
                trackid=it.track.name
                //println(it)
                val track: Track = it.track
               // imageUri=track.imageUri
//                view?.findViewById<TextView>(R.id.song_artist).text=track.artist.name
//                view?.findViewById<TextView>(R.id.song_title)?.text ?: =track.name
                //song_title.text=songname
                artistname=track.artist.name
                songname=track.name
                //Log.d("MainActivity", track.name + " by " + track.artist.name+track.imageUri)

                //Log.d("MainActivity","changed details here e")
            }
           // val imageView = ImageView(this.context)
//            it.playerApi.subscribeToPlayerState().setEventCallback{playerState -> it.imagesApi
//                    .getImage(
//                            playerState.track.imageUri)
//                    .setResultCallback { bitmape ->
//
//
//
//
//                    }}
    }}
    var uri:String=" "
    private fun store(){

        spotifyAppRemote?.let {
            Log.d("main","llll")

            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d(track.uri,"")

            }
            createPlaylistEmpty("dWjx2y05rXMVUsyYxkwUIqq4ewf2",0,"New")
            //addSongToPlaylist("dWjx2y05rXMVUsyYxkwUIqq4ewf2","0","${uri}")

        }

    }


    lateinit var mainHandler: Handler
    //keep checking if details changes and if it is then the next queue song would have been played so we add another one to queue
    private val updateTextTask = object : Runnable {
         override fun run() {
            var oldid=trackid
            changedetails()
            //Log.d("MainActivity","checking details")
            if(oldid!=trackid) next()
            mainHandler.postDelayed(this, 3000)
        }
    }
    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }

    }
    @SuppressLint("WrongConstant")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        playerViewModel =
                ViewModelProvider(this).get(PlayerViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_player, container, false)
        val textView: TextView = root.findViewById(R.id.song_artist)
        playerViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Artist Name"
        val visible : AppCompatButton=root.findViewById(R.id.playBtn)
            visible.visibility=View.VISIBLE
            val visible2 : AppCompatButton=root.findViewById(R.id.pauseBtn)
            visible2.visibility=View.INVISIBLE
            hi()
           mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post(updateTextTask)
        })
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.playBtn).setOnClickListener {
            view.findViewById<Button>(R.id.playBtn).visibility=View.INVISIBLE
            view.findViewById<Button>(R.id.pauseBtn).visibility=View.VISIBLE
            connected()

        }
        view.findViewById<Button>(R.id.pauseBtn).setOnClickListener {
            view.findViewById<Button>(R.id.pauseBtn).visibility=View.INVISIBLE
            view.findViewById<Button>(R.id.playBtn).visibility=View.VISIBLE
            connected()

        }
        view.findViewById<Button>(R.id.next).setOnClickListener {

            skip()

        }
        view.findViewById<Button>(R.id.addPlaylist).setOnClickListener {
            store()
        }
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object: Runnable{
            override fun run() {


                view.findViewById<TextView>(R.id.song_artist).text=artistname
                view.findViewById<TextView>(R.id.song_title).text=songname
               // Log.d("MainActivity","ok done")
                //view.findViewById<ImageView>(R.id.coverImageView).setImageBitmap(bitmap)


                spotifyAppRemote?.let {
                    it.playerApi.subscribeToPlayerState().setEventCallback{playerState -> it.imagesApi
                            .getImage(
                                    playerState.track.imageUri)
                            .setResultCallback { bitmape ->
                                view.findViewById<ImageView>(R.id.coverImageView).setImageBitmap(bitmape)



                            }}
                }
                mainHandler.postDelayed(this, 3000)
            }
        })



    }

}