package com.revakovsky.dragdropnotesapp.adapter

import android.content.ClipData
import android.content.ClipDescription
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.revakovsky.dragdropnotesapp.R
import com.revakovsky.dragdropnotesapp.models.NoteItem

class ListViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.note_item, parent, false))

interface NoteClickListener {
    fun onClick(noteItem: NoteItem)
}

class NotesAdapter(
    private var notes: MutableList<NoteItem>,
    private var clickListener: NoteClickListener,
) : RecyclerView.Adapter<ListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ListViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val noteItem = notes[position]

        holder.itemView
            .apply {
                findViewById<TextView>(R.id.item_title).apply { text = noteItem.title }
                findViewById<TextView>(R.id.item_note).apply { text = noteItem.note }
            }
            .also { view ->
                view.setOnClickListener { clickListener.onClick(noteItem) }

                view.setOnLongClickListener {
                    val idString = noteItem.id.toString()
                    val item = ClipData.Item(idString)
                    val mimeType = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    val clippedData = ClipData(idString, mimeType, item)
                    val dragShadowBuilder = View.DragShadowBuilder(view)

                    view.startDragAndDrop(clippedData, dragShadowBuilder, view, 0)

                    true
                }
            }
    }

    override fun getItemCount(): Int = notes.size

}
