package com.redbubble.redbubblehomework.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.redbubble.redbubblehomework.databinding.ItemProductBinding
import com.redbubble.redbubblehomework.databinding.MainFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

data class HomeModel(
    val type: String,
    val id: String,
    val title: String,
    val safeForWork: Boolean,
    val thumbnailUrl: String,
    val amount: Double?,
    val currency: String?,
    val artist: String?
)

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchData()
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val data = URL("https://take-home-test.herokuapp.com/bff/explore.json").readText()
                val items = parseResponse(data)
                GlobalScope.launch(Dispatchers.Main) ui@{
                    binding.rvHome.adapter =
                        HomeAdapter(items, glide = Glide.with(context ?: return@ui))
                    binding.rvHome.layoutManager = GridLayoutManager(context, 2)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseResponse(data: String): List<HomeModel> {
        val items = arrayListOf<HomeModel>()
        val json = JSONObject(data)
        val home: JSONArray = json.getJSONArray("home") ?: return emptyList()

        for (i in 0 until home.length()) {
            val item = home.getJSONObject(i)

            try {
                items.add(
                    HomeModel(
                        type = item.getString("type"),
                        id = item.getString("id"),
                        title = item.getString("title"),
                        safeForWork = item.getBoolean("safeForWork"),
                        thumbnailUrl = item.getString("thumbnailUrl"),
                        amount = item.optJSONObject("price")?.optDouble("amount"),
                        currency = item.optJSONObject("price")?.getString("currency"),
                        artist = item.optString("artist")
                    )
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return items
    }
}

class HomeAdapter(private val list: List<HomeModel>, val glide: RequestManager) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].let {
            holder.binding.title1.text = it.title
            holder.binding.title2.text = if (it.type == "PRODUCT") {
                "${it.currency} ${it.amount}"
            } else {
                "by ${it.artist}"
            }
            glide.load(it.thumbnailUrl)
                .centerCrop()
                .into(holder.binding.image)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
