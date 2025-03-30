package com.example.gamescatalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.gamescatalog.db.Game

class GamesAdapter(
    private val games: List<Game>,
    private val onBuyClick: (Game) -> Unit
) : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.game_image)
        val priceTextView: TextView = itemView.findViewById(R.id.gamePrice)
        val nameTextView: TextView = itemView.findViewById(R.id.gameName)
        val descTextView: TextView = itemView.findViewById(R.id.gameDesc)
        val buyButton: AppCompatButton = itemView.findViewById(R.id.buy_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_select, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.imageView.setImageResource(game.imageResId)
        holder.priceTextView.text = game.price
        holder.nameTextView.text = game.name
        holder.descTextView.text = game.description

        holder.buyButton.setOnClickListener {
            onBuyClick(game)
        }
    }

    override fun getItemCount(): Int = games.size
}