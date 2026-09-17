package com.autocare.ui.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.autocare.data.VehicleData
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: VehicleViewModel,
    vehicleId: Int = -1,
    onVehicleAdded: () -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Auto") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isEditing = vehicleId != -1
    val coroutineScope = rememberCoroutineScope()
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val internalUri = saveImageToInternalStorage(context, it)
            imageUri = internalUri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            imageUri = tempPhotoUri
        }
    }

    LaunchedEffect(vehicleId) {
        if (isEditing) {
            val vehicle = viewModel.getVehicleById(vehicleId)
            vehicle?.let {
                brand = it.brand
                model = it.model
                year = it.year.toString()
                plate = it.plate
                km = it.currentKm.toString()
                imageUri = it.imageUri?.let { uriString -> Uri.parse(uriString) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Vehículo" else "Nuevo Vehículo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo Picker
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        showImageSourceDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Vehicle image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Seleccionar Imagen") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImageSourceDialog = false
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                                        showImageSourceDialog = false
                                        val uri = createTempImageUri(context)
                                        tempPhotoUri = uri
                                        cameraLauncher.launch(uri)
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
                        TextButton(onClick = { showImageSourceDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Text("Toca para añadir una foto", style = MaterialTheme.typography.labelSmall)

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("¿Eliminar vehículo?") },
                    text = { Text("Esta acción eliminará permanentemente el vehículo y todo su historial. ¿Deseas continuar?") },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmation = false
                                coroutineScope.launch {
                                    val vehicle = viewModel.getVehicleById(vehicleId)
                                    vehicle?.let {
                                        viewModel.delete(it)
                                        onVehicleAdded()
                                    }
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

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle Type Selector
            VehicleDropdown(
                label = "Tipo de Vehículo",
                options = VehicleData.vehicleTypes,
                selectedOption = type,
                onOptionSelected = { type = it }
            )

            // Brand Selector
            VehicleDropdown(
                label = "Marca",
                options = VehicleData.brands,
                selectedOption = brand,
                onOptionSelected = { 
                    brand = it
                    model = "" // Reset model when brand changes
                }
            )

            // Model Selector
            VehicleDropdown(
                label = "Modelo",
                options = VehicleData.modelsByBrand[brand] ?: emptyList(),
                selectedOption = model,
                onOptionSelected = { model = it },
                enabled = brand.isNotEmpty()
            )

            // Year Selector
            VehicleDropdown(
                label = "Año",
                options = VehicleData.years,
                selectedOption = year,
                onOptionSelected = { year = it }
            )

            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text("Placa") },
                keyboardOptions = KeyboardOptions(
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
                value = km,
                onValueChange = { km = it },
                label = { Text("Kilometraje Actual") },
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (brand.isNotBlank() && model.isNotBlank()) {
                        if (isEditing) {
                            viewModel.update(vehicleId, brand, model, year, plate, km, imageUri?.toString(), type)
                        } else {
                            viewModel.insert(brand, model, year, plate, km, imageUri?.toString(), type)
                        }
                        onVehicleAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Actualizar Vehículo" else "Guardar Vehículo")
            }
        }
    }
}

private fun saveImageToInternalStorage(context: android.content.Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "vehicle_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return Uri.fromFile(file)
}

private fun createTempImageUri(context: android.content.Context): Uri {
    val file = File(context.filesDir, "vehicle_camera_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
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

