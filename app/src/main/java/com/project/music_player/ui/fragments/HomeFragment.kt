package com.project.music_player.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.project.music_player.R
import com.project.music_player.adapters.SongAdapter
import com.project.music_player.data.entities.Song
import com.project.music_player.other.Status
import com.project.music_player.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

private const val TAG: String = "FIRESTORE_SEARCH_LOG"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var songs: List<Song> = ArrayList()

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    private fun searchInFirestore(searchText: String) {
        firebaseFirestore.collection("songs").orderBy("searchKey1")
            .startAt(searchText).endAt("$searchText\uf8ff").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    songs = it.result!!.toObjects(Song::class.java)
                    songAdapter.songs = songs
                    songAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "Error: ${it.exception!!.message}")
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        subscribeToObservers()

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }

        search_field1.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText: String = search_field1.text.toString()

                searchInFirestore(searchText.toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar.isVisible =false
                    result.data?.let { songs ->
                        println(songs)
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }
}