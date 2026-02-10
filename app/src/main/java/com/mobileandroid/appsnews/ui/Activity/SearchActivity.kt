package com.mobileandroid.appsnews.ui.Activity

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.api.FetchResponse
import com.mobileandroid.appsnews.databinding.ActivitySearchBinding
import com.mobileandroid.appsnews.repository.NewsRepository
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import com.mobileandroid.appsnews.ui.Fragment.PostsFragment
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel
import com.mobileandroid.appsnews.viewmodels.NewsItemViewModel
import com.mobileandroid.appsnews.viewmodels.NewsVMFactory
import java.io.File


class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: DatabaseViewModel
    private lateinit var newsItemViewModel: NewsItemViewModel


    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null

    private var audioRecorder: android.media.AudioRecord? = null
    private var isRecordingAudio = false
    private var recordingThread: Thread? = null

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showVoiceSearchBottomSheet()
        } else {
            Toast.makeText(this, "Microphone permission is required for voice search", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]

        val fetchResponse = FetchResponse()

        val newsRepository = NewsRepository(fetchResponse)

        newsItemViewModel = ViewModelProvider(
            this,
            NewsVMFactory(newsRepository)
        )[NewsItemViewModel::class.java]

        setupBackPressedHandler()

        // 2. Observe Voice Result from ViewModel 👈 (YE ZAROORI HAI)
        newsItemViewModel.voiceResult.observe(this) { result ->
            if (!result.isNullOrEmpty()) {
                handleVoiceSearchResult(result)
            } else {
                // Agar null aata hai matlab error ya voice samajh nahi aayi
                Toast.makeText(this, "Voice not recognized, try again!", Toast.LENGTH_SHORT).show()
            }
        }

        val searchQuery = intent.getStringExtra("search_query") ?: ""
        if (searchQuery.isNotEmpty()) {
            binding.searchEditText.setText(searchQuery)
            showSearchResults(searchQuery)
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveSearchQuery(query)
                    showSearchResults(query)
                }
                true
            } else false
        }

        // 👉 Search icon click पर भी search
        binding.ivSearch.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                saveSearchQuery(query)
                showSearchResults(query)
            }
        }

        // Voice icon click
        binding.ivVoiceSearch.setOnClickListener {
            checkPermissionAndShowBottomSheet()
        }

        loadRecentSearches()
    }

//    private fun showSearchResults(query: String) {
//        binding.recentSearchesChipGroup.visibility = View.GONE
//        binding.tvRecentSearches.visibility = View.GONE
//
//        val fragment = PostsFragment.newInstance(query)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.search_fragment_container, fragment)
//            .addToBackStack(null)
//            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//            .commit()
//    }

    // 3. Permission check karne ka function
    private fun checkPermissionAndShowBottomSheet() {
        when {
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                // Permission pehle se hai
                showVoiceSearchBottomSheet()
            }
            else -> {
                // Permission nahi hai, mango
                requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
       }


//    private fun showVoiceSearchBottomSheet() {
//        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
//        val view = layoutInflater.inflate(R.layout.layout_voice_search, null)
//        bottomSheetDialog.setContentView(view)
//
//        val btnMicContainer = view.findViewById<FrameLayout>(R.id.btnMicContainer)
//        val ivMicIcon = view.findViewById<ImageView>(R.id.ivMicIcon)
//        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
//
//        var isRecording = false
//
//        btnMicContainer.setOnClickListener {
//            if (!isRecording) {
//                // START RECORDING
//                isRecording = true
//                btnMicContainer.setBackgroundResource(R.drawable.bg_circle_red)
//                ivMicIcon.setImageResource(R.drawable.ic_mic) // 👈 ic_stop add karein
//                tvStatus.text = "Listening..."
//                tvStatus.setTextColor(resources.getColor(R.color.search_voice_color))
//
//                startRecording()
//            } else {
//                // STOP RECORDING
//                isRecording = false
//                btnMicContainer.setBackgroundResource(R.drawable.bg_circle_blue)
//                ivMicIcon.setImageResource(R.drawable.ic_mic)
//                tvStatus.text = "Processing..."
//                tvStatus.setTextColor(resources.getColor(R.color.black))
//
//                stopRecording() // Iske andar ViewModel call hoga
//                bottomSheetDialog.dismiss()
//            }
//        }
//        bottomSheetDialog.show()
//    }



    private fun showVoiceSearchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_voice_search, null)
        bottomSheetDialog.setContentView(view)

        val btnMicContainer = view.findViewById<FrameLayout>(R.id.btnMicContainer)
        val ivMicIcon = view.findViewById<ImageView>(R.id.ivMicIcon)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        tvStatus.text = "Tap mic and speak clearly" // ✅ Initial instruction

        var isRecording = false

        btnMicContainer.setOnClickListener {
            if (!isRecording) {
                isRecording = true
                btnMicContainer.setBackgroundResource(R.drawable.bg_circle_red)
                ivMicIcon.setImageResource(R.drawable.ic_mic)
                tvStatus.text = "🎤 Listening... Speak now!"
                tvStatus.setTextColor(resources.getColor(R.color.search_voice_color))
                startRecording()
            } else {
                isRecording = false
                btnMicContainer.setBackgroundResource(R.drawable.bg_circle_blue)
                ivMicIcon.setImageResource(R.drawable.ic_mic)
                tvStatus.text = "Processing your voice..."
                tvStatus.setTextColor(resources.getColor(R.color.black))
                stopRecording()
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
    }

    private fun handleVoiceSearchResult(query: String) {
        // Search bar update
        binding.searchEditText.setText(query)

        // 2. Database mein professional tarike se save karein
        val displayQuery = "Voice Search: $query"
        saveSearchQuery(displayQuery)

        // 3. Search Results dikhayen
        showSearchResults(query)
    }

//    private fun startRecording() {
//        audioFilePath = "${externalCacheDir?.absolutePath}/voice_search.wav"
//        mediaRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            // Note: Wit.ai ke liye WAV behtar hai, lekin aapka format bhi chal sakta hai
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            setOutputFile(audioFilePath)
//            prepare()
//            start()
//        }
//    }


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
        try {
            val sampleRate = 16000  // ✅ Wit.ai recommends 16kHz
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
            )

            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioFilePath = "${externalCacheDir?.absolutePath}/voice_search.wav"

            isRecordingAudio = true
            audioRecorder?.startRecording()

            // Background thread mein recording karo
            recordingThread = Thread {
                writeAudioDataToFile(bufferSize)
            }
            recordingThread?.start()

            Log.d("SearchActivity", "Recording started: $audioFilePath")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeAudioDataToFile(bufferSize: Int) {
        val data = ByteArray(bufferSize)
        var outputStream: java.io.FileOutputStream? = null
        var totalAudioLen = 0L

        try {
            outputStream = java.io.FileOutputStream(audioFilePath)

            // WAV Header placeholder (44 bytes) - baad mein update karenge
            val header = ByteArray(44)
            outputStream.write(header)

            while (isRecordingAudio) {
                val read = audioRecorder?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    outputStream.write(data, 0, read)
                    totalAudioLen += read.toLong()
                }
            }

            outputStream.close()

            // ✅ Ab proper WAV header add karo
            updateWavHeader(audioFilePath!!, totalAudioLen)

            Log.d("SearchActivity", "Recording saved: ${File(audioFilePath!!).length()} bytes")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }


    private fun updateWavHeader(filePath: String, audioDataLen: Long) {
        val file = java.io.RandomAccessFile(filePath, "rw")
        val totalDataLen = audioDataLen + 36
        val sampleRate = 16000L
        val channels = 1
        val byteRate = sampleRate * channels * 2 // 16-bit = 2 bytes

        file.seek(0)

        // RIFF header
        file.writeBytes("RIFF")
        // java.lang prefix zaroori hai bytes reverse karne ke liye (Little Endian format)
        file.writeInt(java.lang.Integer.reverseBytes(totalDataLen.toInt()))
        file.writeBytes("WAVE")

        // fmt subchunk
        file.writeBytes("fmt ")
        file.writeInt(java.lang.Integer.reverseBytes(16)) // Subchunk1Size
        file.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat (PCM = 1)
        file.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
        file.writeInt(java.lang.Integer.reverseBytes(sampleRate.toInt()))
        file.writeInt(java.lang.Integer.reverseBytes(byteRate.toInt()))
        file.writeShort(java.lang.Short.reverseBytes((channels * 2).toShort()).toInt()) // BlockAlign
        file.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt()) // BitsPerSample

        // data subchunk
        file.writeBytes("data")
        file.writeInt(java.lang.Integer.reverseBytes(audioDataLen.toInt()))

        file.close()
    }

    private fun stopRecording() {
        try {
            isRecordingAudio = false
            audioRecorder?.apply {
                stop()
                release()
            }
            audioRecorder = null

            recordingThread?.join() // Wait for thread to finish
            recordingThread = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        audioFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 100) { // ✅ At least 100 bytes
                Log.d("SearchActivity", "Audio file created: ${file.name}, Size: ${file.length()} bytes")
                newsItemViewModel.processVoiceSearch(file)
            } else {
                Log.e("SearchActivity", "Audio file too small or doesn't exist!")
                Toast.makeText(this, "Recording too short - please speak longer", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showSearchResults(query: String) {
        // Search bar update
        binding.searchEditText.setText(query)
        binding.searchEditText.setSelection(query.length)

        // Hide recent searches
        binding.recentSearchesChipGroup.visibility = View.GONE
        binding.tvRecentSearches.visibility = View.GONE

        // Show loading
        binding.searchLoading.visibility = View.VISIBLE
        binding.searchFragmentContainer.visibility = View.GONE

        val fragmentTag = "PostsFragment"
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment != null && existingFragment is PostsFragment) {
            existingFragment.updateSearchQuery(query)
            existingFragment.setOnDataLoadedListener {
                binding.searchLoading.visibility = View.GONE
                binding.searchFragmentContainer.visibility = View.VISIBLE
            }
        } else {
            val newFragment = PostsFragment.newInstance(query)
            newFragment.setOnDataLoadedListener {
                binding.searchLoading.visibility = View.GONE
                binding.searchFragmentContainer.visibility = View.VISIBLE
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.search_fragment_container, newFragment, fragmentTag)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }


//    private fun showSearchResults(query: String) {
//
//        //  Search bar me bhi query set karo
//        binding.searchEditText.setText(query)
//        binding.searchEditText.setSelection(query.length) // cursor end pe
//
//        binding.recentSearchesChipGroup.visibility = View.GONE
//        binding.tvRecentSearches.visibility = View.GONE
//
//
//        val fragmentTag = "PostsFragment"
//        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
//
//        if (existingFragment != null && existingFragment is PostsFragment) {
//            //  Agar fragment pehle se loaded hai → update query
//            existingFragment.updateSearchQuery(query)
//        } else {
//            //  Agar pehli baar search ho rahi hai → naya fragment load karo
//            val newFragment = PostsFragment.newInstance(query)
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.search_fragment_container, newFragment, fragmentTag)
//                .addToBackStack(null)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit()
//        }
//    }


    private fun loadRecentSearches() {
        viewModel.getAllRecentSearches().observe(this) { searches ->
            binding.recentSearchesChipGroup.removeAllViews()
            if (searches.isNotEmpty()) {
                binding.tvRecentSearches.visibility = View.VISIBLE
                searches.forEach { savedSearch ->
                    val chip = Chip(this).apply {
                        // History mein pura text dikhayen (Voice Search: Cricket)
                        text = savedSearch.searchTopic

                        setOnClickListener {
                            val fullText = savedSearch.searchTopic

                            val cleanQuery = if (fullText.startsWith("Voice Search: ")) {
                                fullText.replace("Voice Search: ", "")
                            } else {
                                fullText
                            }

                            showSearchResults(cleanQuery)
                        }
                    }
                    binding.recentSearchesChipGroup.addView(chip)
                }
            } else {
                binding.tvRecentSearches.visibility = View.GONE
            }
        }
    }

//    private fun loadRecentSearches() {
//        viewModel.getAllRecentSearches().observe(this) { searches ->
//            binding.recentSearchesChipGroup.removeAllViews()
//            if (searches.isNotEmpty()) {
//                binding.tvRecentSearches.visibility = View.VISIBLE
//                searches.forEach { savedSearch ->
//                    val chip = Chip(this).apply {
//                        text = savedSearch.searchTopic
//                        setOnClickListener { showSearchResults(savedSearch.searchTopic) }
//                    }
//                    binding.recentSearchesChipGroup.addView(chip)
//                }
//            } else {
//                binding.tvRecentSearches.visibility = View.GONE
//            }
//        }
//    }

    private fun saveSearchQuery(query: String) {
        val savedSearch = SavedSearches(query)
        viewModel.addRecentSearch(savedSearch)
    }

//    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            supportFragmentManager.popBackStack()
//            binding.recentSearchesChipGroup.visibility = View.VISIBLE
//            binding.tvRecentSearches.visibility = View.VISIBLE
//        } else {
//            super.onBackPressed()
//        }
//    }

    //  Back Press ko handle karne ka function
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    binding.recentSearchesChipGroup.visibility = View.VISIBLE
                    binding.tvRecentSearches.visibility = View.VISIBLE
                } else {
                    finish() // default behavior → Activity close
                }
            }
        })
    }
}

