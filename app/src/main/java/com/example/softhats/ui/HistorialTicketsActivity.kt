package com.example.softhats.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.adapter.TicketAdapter
import com.example.softhats.databinding.ActivityHistorialTicketsBinding
import com.example.softhats.model.TicketItem
import java.io.File

class HistorialTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialTicketsBinding
    private lateinit var adapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TicketAdapter { ticket ->
            abrirPdf(ticket.file)
        }

        binding.rvTickets.layoutManager = LinearLayoutManager(this)
        binding.rvTickets.adapter = adapter

        cargarTickets()
    }

    private fun cargarTickets() {
        val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        val archivos = dir?.listFiles()

        val tickets = archivos
            ?.filter { it.extension == "pdf" }
            ?.map {
                TicketItem(
                    file = it,
                    nombre = it.name,
                    fecha = it.lastModified()
                )
            }
            ?.sortedByDescending { it.fecha }
            ?: emptyList()

        adapter.setTickets(tickets)
    }

    private fun abrirPdf(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.fromFile(file),
            "application/pdf"
        )
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
    }
}
