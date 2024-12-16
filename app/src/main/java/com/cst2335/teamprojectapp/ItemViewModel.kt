package com.cst2335.teamprojectapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class ItemViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

    var isDialogVisible = mutableStateOf(false)

    // as items i will use my name and nicknames
    private val defaultItems = listOf(
        Item(1, "Mikhail Senchenko"),
        Item(2, "Michelle Senchenko"),
        Item(3, "Mihan44"),
        Item(4, "Michelanjelo"),
        Item(5, "Michigun")
    )

    init {
        if (dbHelper.getAllItems().isEmpty()) {
            defaultItems.forEach { dbHelper.addItem(it.name) }
        }
        loadItemsFromDatabase()
    }

    private fun loadItemsFromDatabase() {
        _items.value = dbHelper.getAllItems()
    }

    fun addItem(name: String) {
        val newItemId = dbHelper.addItem(name).toInt()
        val newItem = Item(newItemId, name)
        _items.update { it + newItem }
    }

    fun deleteItem(id: Int) {
        dbHelper.deleteItem(id)
        _items.update { it.filterNot { item -> item.id == id } }
    }

    fun refreshItems(onComplete: () -> Unit) {
        viewModelScope.launch {
            loadItemsFromDatabase()
            onComplete()
        }
    }

    fun showAddItemDialog() {
        isDialogVisible.value = true
    }

    fun hideAddItemDialog() {
        isDialogVisible.value = false
    }
}