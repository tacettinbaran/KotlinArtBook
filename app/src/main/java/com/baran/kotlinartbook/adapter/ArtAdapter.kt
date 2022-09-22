package com.baran.kotlinartbook.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baran.kotlinartbook.databinding.RecyclerRowBinding
import com.baran.kotlinartbook.model.Art
import com.baran.kotlinartbook.view.ArtDetailsActivity

class ArtAdapter(val artList: ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
       holder.binding.recyclerViewTextView.text = artList.get(position).artName

        holder.itemView.setOnClickListener(){
            val intent = Intent(holder.itemView.context, ArtDetailsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
       return artList.size
    }
}