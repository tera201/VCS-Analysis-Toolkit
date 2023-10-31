package com.example.umldrawer.helpers

import javax.swing.ComboBoxModel
import javax.swing.ListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class SharedModel : ComboBoxModel<String>, ListModel<String> {
    private val items = mutableListOf<String>()
    private var selectedItem: Any? = null
    private val listeners = mutableListOf<ListDataListener>()

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = anItem
        notifyListeners()
    }

    override fun getSelectedItem(): Any? {
        return selectedItem
    }

    override fun getSize(): Int {
        return items.size
    }

    override fun getElementAt(index: Int): String {
        return items[index]
    }

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        listeners.remove(l)
    }

    fun add(element: String) {
        items.add(element)
        notifyListeners()
    }
    fun addAll(elements: Collection<String>) {
        items.addAll(elements)
        notifyListeners()
    }

    fun remove(element: String) {
        items.remove(element)
        notifyListeners()
    }

    fun removeAt(index: Int) {
        items.removeAt(index)
        notifyListeners()
    }

    fun clear() {
        items.clear()
        notifyListeners()
    }

    private fun notifyListeners() {
        val event = ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, items.size)
        listeners.forEach { it.contentsChanged(event) }
    }
}