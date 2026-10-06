package com.example.client

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.service.Book
import com.example.service.IBookManager
import com.example.service.IOnBookAddedListener
import java.util.concurrent.Executors

class ClientActivity : Activity() {
    // Mot hang doi FIFO cho register, request va unregister; khong chan UI thread.
    private val worker = Executors.newSingleThreadExecutor()
    private var visible = false
    private var binding: Binding? = null
    private var session: Session? = null
    private lateinit var status: TextView
    private lateinit var processInfo: TextView
    private lateinit var bookList: TextView
    private lateinit var callbackLog: TextView
    private lateinit var connect: Button
    private lateinit var disconnect: Button
    private lateinit var load: Button
    private lateinit var add: Button
    private lateinit var nameInput: EditText
    private lateinit var authorInput: EditText
    private var callbackCount = 0

    // Listener rieng cho tung phien: callback cu khong cap nhat UI phien moi.
    private inner class Session(val remote: IBookManager) {
        @Volatile var active = true
        val listener = object : IOnBookAddedListener.Stub() {
            override fun onBookAdded(newBook: Book) {
                Log.d(TAG, "Callback on ${Thread.currentThread().name}: ${newBook.name}")
                runOnUiThread {
                    if (active && visible && session === this@Session) {
                        callbackCount++
                        callbackLog.text = getString(R.string.callback_received, callbackCount, newBook.name)
                        refresh(this@Session)
                    }
                }
            }
        }
    }

    private inner class Binding : ServiceConnection {
        var requested = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (!visible || binding !== this) return
            retireSession()
            val current = Session(IBookManager.Stub.asInterface(service))
            session = current
            status.setText(R.string.registering)
            worker.execute {
                try {
                    current.remote.registerListener(current.listener)
                    val pid = current.remote.serverPid
                    val books = current.remote.bookList
                    // onStop co the xay ra trong khi register chua ket thuc.
                    // unregister da duoc xep sau register tren cung worker FIFO.
                    showIfCurrent(current) {
                        status.setText(R.string.connected)
                        processInfo.text = getString(R.string.process_pair, Process.myPid(), pid)
                        renderBooks(books)
                        updateButtons(ready = true)
                    }
                } catch (error: RemoteException) {
                    reportFailure(current, error)
                } catch (error: RuntimeException) {
                    reportFailure(current, error)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (binding !== this) return
            retireSession()
            status.setText(R.string.connection_lost)
            processInfo.text = getString(R.string.client_pid, Process.myPid())
            updateButtons(ready = false)
            // Binding van ton tai; Android co the goi onServiceConnected lai.
        }

        override fun onBindingDied(name: ComponentName) {
            if (binding !== this) return
            releaseBinding()
            status.setText(R.string.binding_died)
        }

        override fun onNullBinding(name: ComponentName) {
            if (binding !== this) return
            releaseBinding()
            status.setText(R.string.null_binding)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        status = findViewById(R.id.status)
        processInfo = findViewById(R.id.process_info)
        bookList = findViewById(R.id.book_list)
        callbackLog = findViewById(R.id.callback_log)
        connect = findViewById(R.id.connect)
        disconnect = findViewById(R.id.disconnect)
        load = findViewById(R.id.load)
        add = findViewById(R.id.add)
        nameInput = findViewById(R.id.book_name)
        authorInput = findViewById(R.id.book_author)
        processInfo.text = getString(R.string.client_pid, Process.myPid())
        connect.setOnClickListener {
            releaseBinding()
            bindToServer()
        }
        disconnect.setOnClickListener { releaseBinding() }
        load.setOnClickListener { session?.let(::refresh) }
        add.setOnClickListener { addBook() }
        updateButtons(ready = false)
        val root = findViewById<LinearLayout>(R.id.root)
        val padding = (20 * resources.displayMetrics.density).toInt()
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime()) else android.graphics.Insets.of(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        visible = true
        bindToServer()
    }

    private fun bindToServer() {
        if (!visible || binding != null) return
        val attempt = Binding()
        binding = attempt
        status.setText(R.string.connecting)
        try {
            attempt.requested = bindService(
                Intent().setComponent(ComponentName("com.example.server", "com.example.server.BookManagerService")),
                attempt, Context.BIND_AUTO_CREATE
            )
            if (!attempt.requested) {
                binding = null
                status.setText(R.string.server_missing)
            }
        } catch (error: SecurityException) {
            binding = null
            status.setText(R.string.permission_error)
            Log.e(TAG, "Check permission and signing certificate", error)
        }
        updateButtons(ready = false)
    }

    private fun addBook() {
        val current = session ?: return
        val name = nameInput.text.toString().trim()
        val author = authorInput.text.toString().trim()
        if (name.isEmpty()) { nameInput.error = getString(R.string.required); return }
        if (author.isEmpty()) { authorInput.error = getString(R.string.required); return }
        submit(current) {
            it.addBook(Book(0, name, author, 29.99))
            // addBook tra ve khong dong nghia callback da den: callback la oneway.
            it.bookList
        }
    }

    private fun refresh(current: Session) {
        submit(current) { it.bookList }
    }

    private fun submit(current: Session, request: (IBookManager) -> List<Book>) {
        worker.execute {
            if (!current.active) return@execute
            try {
                val books = request(current.remote)
                showIfCurrent(current) { renderBooks(books) }
            } catch (error: RemoteException) {
                reportFailure(current, error)
            } catch (error: RuntimeException) {
                reportFailure(current, error)
            }
        }
    }

    private fun reportFailure(current: Session, error: Exception) {
        Log.w(TAG, "Remote call failed", error)
        showIfCurrent(current) {
            releaseBinding()
            status.text = getString(R.string.request_failed, error.javaClass.simpleName)
        }
    }

    private fun showIfCurrent(current: Session, action: () -> Unit) {
        runOnUiThread {
            if (visible && current.active && session === current) action()
        }
    }

    private fun renderBooks(books: List<Book>) {
        bookList.text = if (books.isEmpty()) getString(R.string.empty_list) else
            getString(R.string.book_count, books.size) + "\n\n" + books.joinToString("\n\n") {
                getString(R.string.book_row, it.id, it.name, it.author, it.price)
            }
    }

    private fun updateButtons(ready: Boolean) {
        connect.isEnabled = visible
        disconnect.isEnabled = binding != null
        load.isEnabled = ready
        add.isEnabled = ready
    }

    private fun retireSession() {
        val previous = session ?: return
        previous.active = false
        session = null
        worker.execute {
            try { previous.remote.unregisterListener(previous.listener) }
            catch (error: RemoteException) { Log.d(TAG, "Server already disconnected", error) }
            catch (error: RuntimeException) { Log.w(TAG, "Unregister failed", error) }
        }
    }

    private fun releaseBinding() {
        retireSession()
        val previous = binding
        binding = null
        if (previous?.requested == true) {
            unbindService(previous)
            previous.requested = false
        }
        updateButtons(ready = false)
        status.setText(R.string.disconnected)
        processInfo.text = getString(R.string.client_pid, Process.myPid())
        bookList.setText(R.string.list_hint)
        callbackCount = 0
        callbackLog.setText(R.string.callback_hint)
    }

    override fun onStop() {
        visible = false
        releaseBinding()
        super.onStop()
    }

    override fun onDestroy() {
        // shutdown cho phep tac vu unregister da xep hang hoan tat.
        worker.shutdown()
        super.onDestroy()
    }

    companion object { private const val TAG = "AIDL.Client" }
}
