package com.example.androidtrainingexample.minigallery.model


enum class SortOrder(val label: String) {
    DATE_DESC("Mới nhất trước"),
    DATE_ASC("Cũ nhất trước"),
    NAME_ASC("Tên (A → Z)"),
    NAME_DESC("Tên (Z → A)")
}
