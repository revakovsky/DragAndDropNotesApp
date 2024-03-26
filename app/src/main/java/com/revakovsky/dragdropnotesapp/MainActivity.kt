package com.revakovsky.dragdropnotesapp

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.graphics.Rect
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.revakovsky.dragdropnotesapp.adapter.NoteClickListener
import com.revakovsky.dragdropnotesapp.adapter.NotesAdapter
import com.revakovsky.dragdropnotesapp.database.DatabaseHandler
import com.revakovsky.dragdropnotesapp.dialogs.NoteCreationListener
import com.revakovsky.dragdropnotesapp.dialogs.NoteDialog
import com.revakovsky.dragdropnotesapp.models.NoteItem

class MainActivity : AppCompatActivity(), NoteCreationListener, NoteClickListener {

    private lateinit var dbHandler: DatabaseHandler
    private lateinit var decorator: RecyclerView.ItemDecoration
    private lateinit var bin: ImageView

    private val notes: MutableList<NoteItem> = emptyList<NoteItem>().toMutableList()
    private var isBinFull = false

    private val dragListener = View.OnDragListener { view, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> checkHasAppropriateMimeType(event)
            DragEvent.ACTION_DRAG_ENTERED -> invalidateView(view)
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> invalidateView(view)
            DragEvent.ACTION_DROP -> dropNoteIntoTheBin(event)
            DragEvent.ACTION_DRAG_ENDED -> true
            else -> false
        }
    }

    private fun checkHasAppropriateMimeType(event: DragEvent) =
        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)

    private fun invalidateView(view: View): Boolean {
        view.invalidate()
        return true
    }

    private fun dropNoteIntoTheBin(event: DragEvent): Boolean {
        val item = event.clipData.getItemAt(0)
        showDeleteDialog(item.text.toString().toInt())
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initObjects()
        setUpNotesListDecorator()
        setUpRecyclerView()
        getNotesFromLocalDB()
        setActionForTheAddButton()
        bin.setOnDragListener(dragListener)
    }

    private fun initObjects() {
        dbHandler = DatabaseHandler(this)
        bin = findViewById(R.id.bin_img)
    }

    override fun editNote(id: Int, title: String, note: String) {
        dbHandler.editNote(note, title, id)
        getNotesFromLocalDB(id)
    }

    override fun addNote(note: NoteItem) {
        dbHandler.addNote(note)
        getNotesFromLocalDB(note.id)
    }

    override fun onClick(noteItem: NoteItem) {
        val dialog = NoteDialog(this, noteItem)
        dialog.show(supportFragmentManager, "Edit note")
    }

    private fun setUpNotesListDecorator() {
        decorator = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                val space = 16
                outRect.apply {
                    left = space
                    right = space
                    bottom = space
                    top = space
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getNotesFromLocalDB(id: Int? = null) {
        val recyclerView = findViewById<RecyclerView>(R.id.notes_rv)
        notes.clear()
        notes.addAll(dbHandler.getNotes())

        if (id != null) {
            recyclerView.adapter?.notifyItemChanged(notes.indexOf(notes.first { it.id == id }))
        } else recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun setUpRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        val notesAdapter = NotesAdapter(notes, this)

        findViewById<RecyclerView>(R.id.notes_rv).apply {
            layoutManager = gridLayoutManager
            adapter = notesAdapter
            addItemDecoration(decorator)
        }
    }

    private fun setActionForTheAddButton() {
        findViewById<FloatingActionButton>(R.id.add_note_but).setOnClickListener {
            val dialog = NoteDialog.instance(
                noteCreationListener = this@MainActivity,
                note = null
            )
            dialog.show(supportFragmentManager, getString(R.string.add_a_new_note))
        }
    }

    private fun showDeleteDialog(noteId: Int) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_note))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> deleteNote(noteId) }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .create()
            .show()
    }

    private fun deleteNote(noteId: Int) {
        val deleteStatus = dbHandler.deleteNote(noteId)
        if (deleteStatus > 0) {
            getNotesFromLocalDB()
            showToast(getString(R.string.note_has_been_deleted))
            setBinIsFull()
        } else showToast(getString(R.string.error_deleting))
    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun setBinIsFull() {
        if (!isBinFull) {
            isBinFull = true
            bin.setImageResource(R.drawable.bin_2)
        }
    }

}