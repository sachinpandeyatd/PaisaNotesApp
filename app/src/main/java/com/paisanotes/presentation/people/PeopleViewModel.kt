package com.paisanotes.presentation.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.Person
import com.paisanotes.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PeopleState(
    val people: List<Person> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val newPersonName: String = "",
    val newPersonPhone: String = ""
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: PersonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeopleState())
    val state: StateFlow<PeopleState> = _state.asStateFlow()

    init {
        // Reactively listen to the database!
        viewModelScope.launch {
            repository.getAllPeople().collect { peopleList ->
                _state.update { it.copy(people = peopleList) }
            }
        }
    }

    // Dialog Visibility Handlers
    fun showAddDialog() { _state.update { it.copy(isAddDialogVisible = true) } }
    fun hideAddDialog() { _state.update { it.copy(isAddDialogVisible = false, newPersonName = "", newPersonPhone = "") } }

    // Input Handlers
    fun onNameChange(name: String) { _state.update { it.copy(newPersonName = name) } }
    fun onPhoneChange(phone: String) { _state.update { it.copy(newPersonPhone = phone) } }

    // Save Action
    fun savePerson() {
        val currentState = _state.value
        if (currentState.newPersonName.isBlank()) return // Don't save empty names

        viewModelScope.launch {
            val newPerson = Person(
                id = UUID.randomUUID().toString(),
                name = currentState.newPersonName.trim(),
                phoneNumber = currentState.newPersonPhone.trim().takeIf { it.isNotBlank() }
            )
            repository.savePerson(newPerson)
            hideAddDialog() // Close dialog and clear inputs
        }
    }
}