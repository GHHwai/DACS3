package com.example.chatly.ui.ai

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatly.databinding.ActivityAiChatBinding
import com.example.chatly.data.remote.GeminiApiService
import com.example.chatly.BuildConfig
import com.example.chatly.data.repository.AiChatRepository
import com.example.chatly.viewmodel.AiChatViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatBinding
    private lateinit var adapter: AiChatAdapter
    
    // In a real production app, use Hilt for DI. Here we initialize manually for simplicity.
    private val viewModel: AiChatViewModel by viewModels {
        val apiService = GeminiApiService.create()
        val repository = AiChatRepository(apiService)
        AiChatViewModel.Factory(repository)
    }

    // GEMINI API KEY - In production, store this securely (e.g., BuildConfig or backend)
    // For this demonstration, you should replace this with your actual key or use a constant.
    // Sử dụng BuildConfig để bảo mật
    private val API_KEY = BuildConfig.GEMINI_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarAi)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI Trợ Lý Học Tập"
    }

    private fun setupRecyclerView() {
        val markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(LinkifyPlugin.create())
            .build()

        adapter = AiChatAdapter(markwon)
        binding.rvAiChat.apply {
            layoutManager = LinearLayoutManager(this@AiChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@AiChatActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.btnAiSend.setOnClickListener {
            val message = binding.etAiMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
                    Toast.makeText(this, "Vui lòng cấu hình API Key trong AiChatActivity", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.sendMessage(message, API_KEY)
                    binding.etAiMessage.setText("")
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.rvAiChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { errorMsg ->
                errorMsg?.let {
                    Toast.makeText(this@AiChatActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
