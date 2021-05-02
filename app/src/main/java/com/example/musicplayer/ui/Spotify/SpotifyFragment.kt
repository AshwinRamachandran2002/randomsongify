package com.example.musicplayer.ui.Spotify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R

class SpotifyFragment : Fragment() {

    private lateinit var spotifyViewModel: SpotifyViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        spotifyViewModel =
                ViewModelProvider(this).get(SpotifyViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_spotify, container, false)
        val textView: TextView = root.findViewById(R.id.signout)
        spotifyViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Sign Out"
        })
        return root
    }
}