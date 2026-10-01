package com.autocare.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.Reminder
import com.autocare.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    private val _reminder = MutableStateFlow<Reminder?>(null)
    val reminder: StateFlow<Reminder?> = _reminder

    fun loadReminder(id: Int) {
        viewModelScope.launch {
            _reminder.value = repository.getReminderById(id)
        }
    }

    fun saveReminder(
        id: Int,
        vehicleId: Int,
        title: String,
        description: String,
        type: String,
        targetKm: String?,
        targetDate: Long?,
        isCompleted: Boolean,
        uuid: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val updatedReminder = Reminder(
                id = id,
                vehicleId = vehicleId,
                title = title,
                description = description,
                type = type,
                targetKm = targetKm?.toIntOrNull(),
                targetDate = targetDate,
                isCompleted = isCompleted,
                uuid = uuid
            )
            repository.update(updatedReminder)
            onSuccess()
        }
    }

    fun deleteReminder(reminder: Reminder, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.delete(reminder)
            onSuccess()
        }
    }
}

class ReminderViewModelFactory(private val repository: ReminderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: Int,
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val reminderState by viewModel.reminder.collectAsState()
    
    LaunchedEffect(reminderId) {
        viewModel.loadReminder(reminderId)
    }

    reminderState?.let { reminder ->
        val focusManager = LocalFocusManager.current
        var title by remember { mutableStateOf(reminder.title) }
        var description by remember { mutableStateOf(reminder.description) }
        var targetKm by remember { mutableStateOf(reminder.targetKm?.toString() ?: "") }
        var targetDate by remember { mutableStateOf(reminder.targetDate) }
        var isCompleted by remember { mutableStateOf(reminder.isCompleted) }
        
        var showDatePicker by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Editar Recordatorio") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                viewModel.deleteReminder(reminder) {
                                    onNavigateBack()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            floatingActionButton = {
                // Removido para homologar con otras pantallas que usan botones al final del formulario
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(Icons.Default.KeyboardHide, contentDescription = "Ocultar teclado")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(Icons.Default.KeyboardHide, contentDescription = "Ocultar teclado")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = targetKm,
                    onValueChange = { targetKm = it.filter { char -> char.isDigit() } },
                    label = { Text("Kilometraje Objetivo (km)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(Icons.Default.KeyboardHide, contentDescription = "Ocultar teclado")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetDate?.let { 
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) 
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Fecha Objetivo") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Text("Marcar como completado")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.saveReminder(
                            id = reminder.id,
                            vehicleId = reminder.vehicleId,
                            title = title,
                            description = description,
                            type = reminder.type,
                            targetKm = targetKm,
                            targetDate = targetDate,
                            isCompleted = isCompleted,
                            uuid = reminder.uuid,
                            onSuccess = onNavigateBack
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = targetDate ?: System.currentTimeMillis()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        confirmButton = {
                            TextButton(onClick = {
                                targetDate = datePickerState.selectedDateMillis
                                showDatePicker = false
                            }) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
}
