package com.example.softhats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.databinding.ItemTicketBinding
import com.example.softhats.model.TicketItem
import java.text.SimpleDateFormat
import java.util.*

class TicketAdapter(
    private val onClick: (TicketItem) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    private val tickets = mutableListOf<TicketItem>()

    fun setTickets(lista: List<TicketItem>) {
        tickets.clear()
        tickets.addAll(lista)
        notifyDataSetChanged()
    }

    inner class TicketViewHolder(
        val binding: ItemTicketBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]

        holder.binding.tvNombre.text = ticket.nombre

        val fecha = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(ticket.fecha))

        holder.binding.tvFecha.text = fecha

        holder.binding.root.setOnClickListener {
            onClick(ticket)
        }
    }

    override fun getItemCount() = tickets.size
}
