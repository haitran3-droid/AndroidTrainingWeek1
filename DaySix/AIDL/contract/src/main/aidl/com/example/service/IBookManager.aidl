package com.example.service;
import com.example.service.Book;
import com.example.service.IOnBookAddedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    int getBookCount();
    int getServerPid();
    void registerListener(IOnBookAddedListener listener);
    void unregisterListener(IOnBookAddedListener listener);
}
