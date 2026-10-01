package com.autocare.ui.fuel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.autocare.util.ReceiptOcrParser
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelScreen(
    viewModel: FuelViewModel,
    vehicleId: Int,
    fuelId: Int = -1,
    isProUser: Boolean = false,
    onFuelAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val fuelLoad by viewModel.fuelLoad.collectAsState()
    
    var liters by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var showOcrSourceDialog by remember { mutableStateOf(false) }
    var showUnreadableTicketDialog by remember { mutableStateOf(false) }
    var isOcrProcessing by remember { mutableStateOf(false) }
    var tempOcrUri by remember { mutableStateOf<Uri?>(null) }
    var showUpsellDialog by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fuelLoad?.date ?: System.currentTimeMillis()
    )

    val ocrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                isOcrProcessing = true
                val data = ReceiptOcrParser.processReceiptImage(context, it)
                isOcrProcessing = false
                if (data.isSuccess) {
                    data.liters?.let { valLit -> liters = String.format(Locale.US, "%.2f", valLit) }
                    data.totalCost?.let { valCost -> totalCost = String.format(Locale.US, "%.2f", valCost) }
                    data.dateMillis?.let { millis -> datePickerState.selectedDateMillis = millis }
                    Toast.makeText(context, "Ticket procesado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    showUnreadableTicketDialog = true
                }
            }
        }
    }

    val ocrCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempOcrUri != null) {
            coroutineScope.launch {
                isOcrProcessing = true
                val data = ReceiptOcrParser.processReceiptImage(context, tempOcrUri!!)
                isOcrProcessing = false
                if (data.isSuccess) {
                    data.liters?.let { valLit -> liters = String.format(Locale.US, "%.2f", valLit) }
                    data.totalCost?.let { valCost -> totalCost = String.format(Locale.US, "%.2f", valCost) }
                    data.dateMillis?.let { millis -> datePickerState.selectedDateMillis = millis }
                    Toast.makeText(context, "Ticket procesado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    showUnreadableTicketDialog = true
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempOcrUri(context)
            tempOcrUri = uri
            ocrCameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para escanear tickets", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(fuelId) {
        if (fuelId != -1) {
            viewModel.loadFuelLoad(fuelId)
        }
    }

    LaunchedEffect(fuelLoad) {
        fuelLoad?.let {
            liters = it.liters.toString()
            totalCost = it.totalCost.toString()
            odometer = it.odometer.toString()
            datePickerState.selectedDateMillis = it.date
        }
    }

    val selectedDate = datePickerState.selectedDateMillis ?: (fuelLoad?.date ?: System.currentTimeMillis())
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(selectedDate))

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar registro?") },
            text = { Text("Esta acción eliminará permanentemente esta carga de combustible. ¿Deseas continuar?") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        fuelLoad?.let {
                            viewModel.deleteFuelLoad(it, onFuelAdded)
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

    if (showUnreadableTicketDialog) {
        AlertDialog(
            onDismissRequest = { showUnreadableTicketDialog = false },
            title = { Text("Ticket no legible") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = {
                Column {
                    Text(
                        text = "No fue posible leer la información del ticket. Recomendaciones para mejorar la lectura:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Acerca la cámara y aplica zoom enfocado en el total y la fecha.\n• Asegura buena iluminación evitando sombras y reflejos brillantes.\n• Aplatana el ticket si está arrugado o doblado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnreadableTicketDialog = false
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            val uri = createTempOcrUri(context)
                            tempOcrUri = uri
                            ocrCameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Reintentar Cámara")
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            showUnreadableTicketDialog = false
                            ocrGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Text("Galería")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showUnreadableTicketDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    if (showUpsellDialog) {
        AlertDialog(
            onDismissRequest = { showUpsellDialog = false },
            title = { Text("Función Pro: Escáner OCR de Tickets") },
            text = { Text("Desbloquea el escaneo inteligente de tickets con IA para auto-completar cargas de combustible, mantenimientos y gastos.") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                Button(onClick = { showUpsellDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showOcrSourceDialog) {
        AlertDialog(
            onDismissRequest = { showOcrSourceDialog = false },
            title = { Text("Escanear Ticket") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showOcrSourceDialog = false
                                ocrGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Galería")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showOcrSourceDialog = false
                                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    val uri = createTempOcrUri(context)
                                    tempOcrUri = uri
                                    ocrCameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cámara")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOcrSourceDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (fuelId == -1) "Registrar Carga de Combustible" else "Editar Carga de Combustible") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isProUser) {
                        showOcrSourceDialog = true
                    } else {
                        showUpsellDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isOcrProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando Ticket...")
                } else {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear Ticket (OCR)")
                }
            }

            OutlinedTextField(
                value = liters,
                onValueChange = { liters = cleanDecimalInput(it) },
                label = { Text("Litros") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
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
                value = totalCost,
                onValueChange = { totalCost = cleanDecimalInput(it) },
                label = { Text("Costo Total") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
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
                value = odometer,
                onValueChange = { odometer = it.filter { char -> char.isDigit() } },
                label = { Text("Kilometraje actual (Odómetro)") },
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
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (liters.isNotBlank() && totalCost.isNotBlank() && odometer.isNotBlank()) {
                        viewModel.saveFuelLoad(
                            id = if (fuelId == -1) null else fuelId,
                            vehicleId = vehicleId,
                            liters = liters,
                            totalCost = totalCost,
                            odometer = odometer,
                            date = selectedDate,
                            onSuccess = onFuelAdded
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (fuelId == -1) "Guardar Carga" else "Actualizar Carga")
            }

            if (fuelId != -1) {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Carga")
                }
            }
        }
    }
}

private fun createTempOcrUri(context: Context): Uri {
    val file = File(context.filesDir, "ocr_ticket_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun cleanDecimalInput(input: String): String {
    val cleaned = input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    val firstDotIndex = cleaned.indexOf('.')
    return if (firstDotIndex != -1) {
        cleaned.substring(0, firstDotIndex + 1) + cleaned.substring(firstDotIndex + 1).replace(".", "")
    } else {
        cleaned
    }
}
