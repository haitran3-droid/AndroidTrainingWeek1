package com.example.service;
import com.example.service.Book;

// Callback remote khong doi ket qua. Client nhan tren Binder thread.
oneway interface IOnBookAddedListener {
    void onBookAdded(in Book newBook);
}
