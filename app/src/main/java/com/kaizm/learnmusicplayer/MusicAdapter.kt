package com.kaizm.learnmusicplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaizm.learnmusicplayer.databinding.ItemMusicBinding

class MusicAdapter(val onClick: (Music) -> Unit) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    var listMusic: List<Music> = mutableListOf()

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(music: Music) {
            binding.musicName.text = music.name
            binding.musicAuthor.text = music.author
            binding.musicAvt.setImageBitmap(music.img)
            binding.musicContainer.setOnClickListener {
                onClick(music)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music =  listMusic[position]
        holder.bind(music)
    }

    override fun getItemCount() = listMusic.size
}