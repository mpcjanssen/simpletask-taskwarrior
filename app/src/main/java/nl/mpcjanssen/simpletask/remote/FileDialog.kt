package nl.mpcjanssen.simpletask.remote

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Environment
import nl.mpcjanssen.simpletask.util.ListenerList
import java.io.File
import java.io.FilenameFilter
import java.util.*

class FileDialog
/**
 * @param activity
 * *
 * @param pathName
 */
(private val activity: Activity, pathName: String) {
    private var fileList: Array<String>? = null
    private var currentPath: File? = null
    private val PARENT_DIR = ".."

    private val fileListenerList = ListenerList<FileSelectedListener>()

    init {
        var path = File(pathName)
        if (!path.exists() || !path.isDirectory) path = Environment.getExternalStorageDirectory()
        loadFileList(path)
    }

    /**
     * @return file dialog
     */

    fun createFileDialog(): Dialog {
        val dialog: Dialog
        val builder = AlertDialog.Builder(activity)

        builder.setTitle(currentPath!!.path)

        builder.setItems(fileList) { thisDialog, which ->
            val fileChosen = fileList!![which]
            val chosenFile = getChosenFile(fileChosen)
            if (chosenFile.isDirectory) {
                loadFileList(chosenFile)
                thisDialog.cancel()
                thisDialog.dismiss()
                showDialog()
            } else
                fireFileSelectedEvent(chosenFile)
        }

        dialog = builder.show()
        return dialog
    }

    fun addFileListener(listener: FileSelectedListener) {
        fileListenerList.add(listener)
    }

    /**
     * Show file dialog
     */
    fun showDialog() {
        createFileDialog().show()
    }

    private fun fireFileSelectedEvent(file: File) {
        fileListenerList.fireEvent(object : ListenerList.FireHandler<FileSelectedListener> {
            override fun fireEvent(listener: FileSelectedListener) {
                listener.fileSelected(file.toString())
            }
        })
    }



    private fun loadFileList(path: File) {
        this.currentPath = path
        val r = ArrayList<String>()
        if (path.exists()) {
            if (path.parentFile != null) r.add(PARENT_DIR)
            val filter = FilenameFilter { dir, filename ->
                val sel = File(dir, filename)
                sel.canRead()
            }
            val fileList1 = path.list(filter)
            if (fileList1 != null) {
                Collections.addAll(r, *fileList1)
            } else {
                // Fallback to root
                r.add("/")
            }
        } else {
            // Fallback to root
            r.add("/")
        }
        Collections.sort(r)
        fileList = r.toArray(arrayOfNulls<String>(r.size))
    }

    private fun getChosenFile(fileChosen: String): File {
        if (fileChosen == PARENT_DIR)
            return currentPath!!.parentFile
        else if (fileChosen == "/") {
            return File("/")
        } else {
            return File(currentPath, fileChosen)
        }
    }

    companion object {
        private val PARENT_DIR = ".."
        fun browseForNewFile(act: Activity, path: String, listener: FileSelectedListener) {
            val dialog = FileDialog(act, path)
            dialog.addFileListener(listener)
            dialog.createFileDialog()
        }
    }
}

interface FileSelectedListener {
    fun fileSelected(fileName: String)
}
