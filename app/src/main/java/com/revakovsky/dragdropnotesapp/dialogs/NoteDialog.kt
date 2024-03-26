package com.revakovsky.dragdropnotesapp.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.revakovsky.dragdropnotesapp.R
import com.revakovsky.dragdropnotesapp.models.NoteItem
import java.time.Instant
import java.time.format.DateTimeFormatter

interface NoteCreationListener {
    fun editNote(id: Int, title: String, note: String)
    fun addNote(note: NoteItem)
}

class NoteDialog(
    private val noteCreationListener: NoteCreationListener,
    private val note: NoteItem?,
) : DialogFragment() {

    private var editMode: Boolean = false

    private lateinit var buttonSubmit: Button
    private lateinit var noteTitle: EditText
    private lateinit var noteText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return layoutInflater.inflate(R.layout.note_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        showCurrentNoteText()
        setTextForButton()
        setActionForButton()
    }

    private fun initViews(view: View) {
        buttonSubmit = view.findViewById(R.id.submit_but)
        noteTitle = view.findViewById(R.id.note_title_et)
        noteText = view.findViewById(R.id.note_et)
    }

    private fun showCurrentNoteText() {
        note?.let {
            editMode = true
            noteTitle.setText(it.title)
            noteText.setText(it.note)
        }
    }

    private fun setTextForButton() {
        if (editMode) buttonSubmit.text = getString(R.string.save)
    }

    private fun setActionForButton() {
        buttonSubmit.setOnClickListener {
            if (areNoteFieldsEmpty()) {
                showInstructions()
                return@setOnClickListener
            }

            if (shouldEditNote()) {
                editNote(note!!)
                dismiss()
                return@setOnClickListener
            }

            addNewNote()
            dismiss()
        }
    }

    private fun areNoteFieldsEmpty() =
        noteTitle.text.toString().isEmpty() || noteText.text.toString().isEmpty()

    private fun showInstructions() {
        Toast.makeText(
            requireContext(),
            getString(R.string.please_provide_a_note_title_and_note_body),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun shouldEditNote() = editMode && note != null

    private fun editNote(note: NoteItem) {
        noteCreationListener.editNote(
            note.id!!,
            noteTitle.text.toString(),
            noteText.text.toString(),
        )
    }

    private fun addNewNote() {
        noteCreationListener.addNote(
            NoteItem(
                title = noteTitle.text.toString(),
                note = noteText.text.toString(),
                timeStamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).toString()
            )
        )
    }


    companion object {
        fun instance(noteCreationListener: NoteCreationListener, note: NoteItem?): NoteDialog =
            NoteDialog(noteCreationListener, note)
    }

}
