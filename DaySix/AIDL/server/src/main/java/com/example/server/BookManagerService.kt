package com.example.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.example.service.Book
import com.example.service.IBookManager
import com.example.service.IOnBookAddedListener

class BookManagerService : Service() {
    private val lock = Any()
    private val books = mutableListOf(
        Book(1, "Kotlin in Action", "Dmitry Jemerov", 29.99),
        Book(2, "Android Programming", "Big Nerd Ranch", 39.99)
    )
    private val listeners = RemoteCallbackList<IOnBookAddedListener>()
    private val callbackHandler = Handler(Looper.getMainLooper())
    private var destroyed = false // Chi doc/ghi tren main thread.

    // Cac method remote co the chay dong thoi tren Binder thread pool.
    private val binder = object : IBookManager.Stub() {
        override fun getBookList(): List<Book> = synchronized(lock) { books.toList() }
        override fun getBookCount(): Int = synchronized(lock) { books.size }
        override fun getServerPid(): Int = Process.myPid()

        override fun addBook(book: Book) {
            require(book.name.isNotBlank()) { "Book name must not be blank" }
            require(book.author.isNotBlank()) { "Author must not be blank" }
            require(book.price.isFinite() && book.price >= 0) { "Invalid price" }
            synchronized(lock) {
                check(books.size < 100) { "Demo limit: 100 books" }
                // Server cap ID; nhieu client cung them khong trung ID.
                val stored = book.copy(id = (books.maxOfOrNull { it.id } ?: 0) + 1)
                books.add(stored)
                // Post trong lock giu thu tu callback trung voi thu tu them.
                callbackHandler.post { if (!destroyed) notifyBookAdded(stored) }
                Log.d(TAG, "addBook: ${stored.name}; PID=${Process.myPid()}; thread=${Thread.currentThread().name}")
            }
        }

        override fun registerListener(listener: IOnBookAddedListener) {
            listeners.register(listener)
        }

        override fun unregisterListener(listener: IOnBookAddedListener) {
            listeners.unregister(listener)
        }
    }

    private fun notifyBookAdded(book: Book) {
        // beginBroadcast/finishBroadcast chi chay tuan tu tren main thread.
        val count = listeners.beginBroadcast()
        try {
            for (index in 0 until count) {
                try {
                    listeners.getBroadcastItem(index).onBookAdded(book)
                } catch (error: RemoteException) {
                    Log.w(TAG, "Client callback unavailable", error)
                }
            }
        } finally {
            listeners.finishBroadcast()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created, remote PID=${Process.myPid()}")
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        destroyed = true
        callbackHandler.removeCallbacksAndMessages(null)
        listeners.kill()
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    companion object { private const val TAG = "AIDL.Server" }
}
