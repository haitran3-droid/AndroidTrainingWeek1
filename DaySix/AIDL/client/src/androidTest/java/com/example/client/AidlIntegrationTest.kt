package com.example.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.service.Book
import com.example.service.IBookManager
import com.example.service.IOnBookAddedListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AidlIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private var bound = false
    private val ready = CountDownLatch(1)
    private lateinit var remote: IBookManager
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            remote = IBookManager.Stub.asInterface(service)
            ready.countDown()
        }
        override fun onServiceDisconnected(name: ComponentName) = Unit
    }

    @Before fun bind() {
        instrumentation.runOnMainSync {
            bound = context.bindService(
                Intent().setComponent(ComponentName("com.example.server", "com.example.server.BookManagerService")),
                connection, Context.BIND_AUTO_CREATE
            )
        }
        assertTrue("Install server-debug.apk first", bound)
        assertTrue("Service connection timed out", ready.await(10, TimeUnit.SECONDS))
    }

    @After fun unbind() {
        if (bound) instrumentation.runOnMainSync { context.unbindService(connection) }
    }

    @Test fun parcelableRoundTripPreservesFields() {
        val expected = Book(7, "AIDL Tiếng Việt", "Author", 19.95)
        val parcel = Parcel.obtain()
        try {
            expected.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            assertEquals(expected, Book.CREATOR.createFromParcel(parcel))
        } finally { parcel.recycle() }
    }

    @Test fun remoteProcessAndSnapshotIsolation() {
        assertNotEquals(Process.myPid(), remote.serverPid)
        val before = remote.bookCount
        val snapshot = remote.bookList
        val name = "Snapshot-${System.nanoTime()}"
        remote.addBook(Book(0, name, "Integration test", 12.5))
        assertEquals(before + 1, remote.bookCount)
        assertEquals(before, snapshot.size)
        val received = remote.bookList.last()
        assertEquals(name, received.name)
        assertEquals(12.5, received.price, 0.0)
        assertTrue(received.id > 0)
    }

    @Test fun callbackIsDeliveredAndStopsAfterUnregister() {
        val delivered = CountDownLatch(1)
        val unwanted = CountDownLatch(1)
        val result = AtomicReference<Book>()
        val first = "Callback-${System.nanoTime()}"
        val second = "Unregistered-${System.nanoTime()}"
        val listener = object : IOnBookAddedListener.Stub() {
            override fun onBookAdded(newBook: Book) {
                if (newBook.name == first) { result.set(newBook); delivered.countDown() }
                if (newBook.name == second) unwanted.countDown()
            }
        }
        remote.registerListener(listener)
        try {
            remote.addBook(Book(0, first, "Integration test", 20.0))
            assertTrue("Callback timed out", delivered.await(5, TimeUnit.SECONDS))
            assertEquals(first, result.get().name)
            assertEquals(20.0, result.get().price, 0.0)
        } finally { remote.unregisterListener(listener) }
        remote.addBook(Book(0, second, "Integration test", 20.0))
        assertFalse("Callback continued after unregister", unwanted.await(1, TimeUnit.SECONDS))
    }

    @Test fun serverRejectsInvalidBookWithoutChangingState() {
        val before = remote.bookCount
        try {
            remote.addBook(Book(0, "", "Author", 1.0))
            fail("Server accepted a blank name")
        } catch (_: IllegalArgumentException) {
            assertEquals(before, remote.bookCount)
        }
    }
}
