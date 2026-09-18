package com.autocare.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.autocare.data.entity.calculateSeverity
import coil.compose.AsyncImage
import com.autocare.data.entity.Vehicle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (Int) -> Unit,
    onNavigateToAddFuel: (Int) -> Unit,
    onNavigateToAddMaintenance: (Int) -> Unit,
    onNavigateToAddExpense: (Int) -> Unit,
    onNavigateToHistory: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditReminder: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showVehicleSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { if (uiState.allVehicles.size > 1) showVehicleSelector = true }
                        ) {
                            Text("AutoCare")
                            if (uiState.allVehicles.size > 1) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        val pendingCount = uiState.reminders.count { !it.isCompleted }
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge {
                                        Text(pendingCount.toString())
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminders",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Dashboard") },
                        selected = true,
                        onClick = { },
                        colors = navItemColors
                    )
                    uiState.vehicle?.let { vehicle ->
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.EvStation, contentDescription = "Carga Gas") },
                            selected = false,
                            onClick = { onNavigateToAddFuel(vehicle.id) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Build, contentDescription = "Mantenimiento") },
                            selected = false,
                            onClick = { onNavigateToAddMaintenance(vehicle.id) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Gasto") },
                            selected = false,
                            onClick = { onNavigateToAddExpense(vehicle.id) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                            selected = false,
                            onClick = { onNavigateToHistory(vehicle.id) },
                            colors = navItemColors
                        )
                    }
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                        selected = false,
                        onClick = onNavigateToSettings,
                        colors = navItemColors
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddVehicle,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.vehicle == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes vehículos registrados", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToAddVehicle,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Registrar mi primer vehículo")
                    }
                }
            }
        } else {
            DashboardContent(
                vehicle = uiState.vehicle!!,
                monthlyExpense = uiState.monthlyExpense,
                nextOilChange = uiState.nextOilChangeKm,
                fuelPerformance = uiState.fuelPerformance,
                reminders = uiState.reminders,
                onEditVehicle = { onNavigateToEditVehicle(uiState.vehicle!!.id) },
                onEditReminder = onNavigateToEditReminder,
                modifier = Modifier.padding(padding)
            )

            if (showVehicleSelector) {
                AlertDialog(
                    onDismissRequest = { showVehicleSelector = false },
                    title = { Text("Seleccionar Vehículo") },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    text = {
                        Column {
                            uiState.allVehicles.forEach { vehicle ->
                                ListItem(
                                    headlineContent = { Text("${vehicle.brand} ${vehicle.model}") },
                                    supportingContent = { Text(vehicle.plate) },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.clickable {
                                        viewModel.selectVehicle(vehicle.id)
                                        showVehicleSelector = false
                                    }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showVehicleSelector = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    vehicle: Vehicle,
    monthlyExpense: Double,
    nextOilChange: Int?,
    fuelPerformance: Double,
    reminders: List<com.autocare.data.entity.Reminder>,
    onEditVehicle: () -> Unit,
    onEditReminder: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle Info Card with Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onEditVehicle,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vehicle.imageUri != null) {
                    AsyncImage(
                        model = vehicle.imageUri,
                        contentDescription = "Vehicle Image",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Placa: ${vehicle.plate}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${String.format("%,d", vehicle.currentKm)} km",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Stats Column (Apiladas)
        StatCard(
            title = "Rendimiento de Combustible",
            value = if (fuelPerformance > 0.0) "${String.format("%.2f", fuelPerformance)} km/L" else "N/D",
            icon = Icons.Default.EvStation,
            modifier = Modifier.fillMaxWidth()
        )
        StatCard(
            title = "Gasto Mensual",
            value = "$${String.format("%.2f", monthlyExpense)}",
            icon = Icons.Default.Payments,
            modifier = Modifier.fillMaxWidth()
        )
        StatCard(
            title = "Próximo Mantenimiento",
            value = nextOilChange?.let { if (it > 0) "en ${it} km" else "¡YA!" } ?: "N/D",
            icon = Icons.Default.Build,
            valueColor = if ((nextOilChange ?: 1000) < 500) Color.Red else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )

        // Alertas de Recordatorios Pendientes (Alertas de Mantenimiento por Categoría y Frecuencia)
        if (reminders.any { !it.isCompleted }) {
            Text(
                text = "Alertas de Recordatorios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            reminders.filter { !it.isCompleted }.forEach { reminder ->
                val severity = reminder.calculateSeverity(vehicle.currentKm)
                val isDark = isSystemInDarkTheme()
                
                val iconColor = when (severity) {
                    com.autocare.data.entity.AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                    com.autocare.data.entity.AlertSeverity.WARNING -> if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00) // Amber/Orange adaptivo
                    com.autocare.data.entity.AlertSeverity.NONE -> MaterialTheme.colorScheme.onBackground
                }
                
                val statusText = when (severity) {
                    com.autocare.data.entity.AlertSeverity.CRITICAL -> "¡VENCIDO / URGENTE!"
                    com.autocare.data.entity.AlertSeverity.WARNING -> "Próximo a vencer"
                    com.autocare.data.entity.AlertSeverity.NONE -> "Al día"
                }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onEditReminder(reminder.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (reminder.type) {
                                "MAINTENANCE" -> Icons.Default.Build
                                "DOCUMENT" -> Icons.Default.Receipt
                                else -> Icons.Default.Warning
                            },
                            contentDescription = "Alert Icon",
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = reminder.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(text = reminder.description, style = MaterialTheme.typography.bodyMedium)
                            
                            // Mileage condition
                            reminder.targetKm?.let { target ->
                                val diff = target - vehicle.currentKm
                                val color = if (diff <= 500) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                                Text(
                                    text = "Por Kilometraje: ${String.format("%,d", target)} km " + 
                                           (if (diff <= 0) "(Vencido hace ${-diff} km)" else "(Faltan ${diff} km)"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                            
                            // Date condition
                            reminder.targetDate?.let { target ->
                                val diff = target - System.currentTimeMillis()
                                val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
                                val color = if (diff <= sevenDaysMs) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(target))
                                
                                val daysDiff = diff / (24 * 60 * 60 * 1000L)
                                val timeDetail = if (diff <= 0) {
                                    "(Vencido hace ${-daysDiff} días)"
                                } else {
                                    "(Faltan ${daysDiff} días)"
                                }
                                
                                Text(
                                    text = "Por Fecha: $dateStr $timeDetail",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            color = iconColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
        }
    }
}
