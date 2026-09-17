package com.autocare.ui.maintenance

import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    viewModel: MaintenanceViewModel,
    vehicleId: Int,
    maintenanceId: Int = -1,
    onMaintenanceAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val maintenance by viewModel.maintenance.collectAsState()

    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(maintenanceId) {
        if (maintenanceId != -1) {
            viewModel.loadMaintenance(maintenanceId)
        }
    }

    LaunchedEffect(maintenance) {
        maintenance?.let {
            val savedType = it.type
            // Try to find if savedType matches any subcategory or category to pre-populate correctly
            var found = false
            for ((cat, subList) in com.autocare.data.VehicleData.maintenanceCategories) {
                if (subList.contains(savedType)) {
                    selectedCategory = cat
                    selectedSubCategory = savedType
                    found = true
                    break
                }
            }
            if (!found) {
                if (com.autocare.data.VehicleData.maintenanceCategories.containsKey(savedType)) {
                    selectedCategory = savedType
                    selectedSubCategory = ""
                } else {
                    // Fallback or custom text if any
                    selectedCategory = savedType
                    selectedSubCategory = ""
                }
            }
            km = it.km.toString()
            cost = it.cost.toString()
            notes = it.notes
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = maintenance?.date ?: System.currentTimeMillis()
    )
    val selectedDate = datePickerState.selectedDateMillis ?: (maintenance?.date ?: System.currentTimeMillis())
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(selectedDate))

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar registro?") },
            text = { Text("Esta acción eliminará permanentemente este mantenimiento. ¿Deseas continuar?") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        maintenance?.let {
                            viewModel.deleteMaintenance(it, onMaintenanceAdded)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (maintenanceId == -1) "Registrar Mantenimiento" else "Editar Mantenimiento") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Row / Field
            MaintenanceDropdown(
                label = "Categoría de Mantenimiento",
                selectedOption = selectedCategory,
                onOptionSelected = { cat ->
                    selectedCategory = cat
                    selectedSubCategory = "" // Reset subcategory when category changes
                },
                options = com.autocare.data.VehicleData.maintenanceCategories.keys.toList()
            )

            // SubCategory Row / Field
            val subCategories = com.autocare.data.VehicleData.maintenanceCategories[selectedCategory] ?: emptyList()
            MaintenanceDropdown(
                label = "Subcategoría de Mantenimiento",
                selectedOption = selectedSubCategory,
                onOptionSelected = { selectedSubCategory = it },
                options = subCategories,
                enabled = selectedCategory.isNotEmpty() && subCategories.isNotEmpty()
            )

            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("Kilometraje") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
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
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Costo Total") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
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
                value = dateStr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas / Detalles") },
                keyboardOptions = KeyboardOptions(
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            var applyReminder by remember { mutableStateOf(false) }
            var reminderKm by remember { mutableStateOf("") }
            var showReminderDatePicker by remember { mutableStateOf(false) }
            val reminderDatePickerState = rememberDatePickerState()
            val selectedReminderDate = reminderDatePickerState.selectedDateMillis
            val reminderDateStr = selectedReminderDate?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date(it))
            } ?: "Seleccionar fecha"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { applyReminder = !applyReminder }
            ) {
                Checkbox(checked = applyReminder, onCheckedChange = { applyReminder = it })
                Text("Programar recordatorio para este mantenimiento", style = MaterialTheme.typography.bodyMedium)
            }

            if (applyReminder) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reminderKm,
                        onValueChange = { reminderKm = it },
                        label = { Text("Próximo mantenimiento (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reminderDateStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Próxima fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showReminderDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showReminderDatePicker = true }
                    )

                    if (showReminderDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showReminderDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = { showReminderDatePicker = false }) { Text("Aceptar") }
                            }
                        ) {
                            DatePicker(state = reminderDatePickerState)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalType = if (selectedSubCategory.isNotBlank()) selectedSubCategory else selectedCategory
                    if (finalType.isNotBlank() && km.isNotBlank() && cost.isNotBlank()) {
                        viewModel.saveMaintenance(
                            id = if (maintenanceId == -1) null else maintenanceId,
                            vehicleId = vehicleId,
                            type = finalType,
                            km = km,
                            cost = cost,
                            notes = notes,
                            date = selectedDate,
                            applyReminder = applyReminder,
                            reminderKm = if (reminderKm.isBlank()) null else reminderKm,
                            reminderDate = selectedReminderDate,
                            onSuccess = onMaintenanceAdded
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (maintenanceId == -1) "Guardar Mantenimiento" else "Actualizar Mantenimiento")
            }

            if (maintenanceId != -1) {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Mantenimiento")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDropdown(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    options: List<String>,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
            enabled = enabled
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
