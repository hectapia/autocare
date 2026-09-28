package com.autocare.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import com.autocare.ui.analytics.CostBreakdownPieChart
import com.autocare.ui.analytics.FuelEfficiencyChart
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.autocare.ui.theme.AutoCareTheme
import com.autocare.ui.components.AdBanner
import com.autocare.util.ReportUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    isProUser: Boolean,
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
    DashboardScreenContent(
        uiState = uiState,
        isProUser = isProUser,
        onNavigateToAddVehicle = onNavigateToAddVehicle,
        onNavigateToEditVehicle = onNavigateToEditVehicle,
        onNavigateToAddFuel = onNavigateToAddFuel,
        onNavigateToAddMaintenance = onNavigateToAddMaintenance,
        onNavigateToAddExpense = onNavigateToAddExpense,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToEditReminder = onNavigateToEditReminder,
        onSelectVehicle = { viewModel.selectVehicle(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    isProUser: Boolean = false,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (Int) -> Unit,
    onNavigateToAddFuel: (Int) -> Unit,
    onNavigateToAddMaintenance: (Int) -> Unit,
    onNavigateToAddExpense: (Int) -> Unit,
    onNavigateToHistory: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditReminder: (Int) -> Unit,
    onSelectVehicle: (Int) -> Unit,
    initialShowVehicleSelector: Boolean = false
) {
    var showVehicleSelector by remember { mutableStateOf(initialShowVehicleSelector) }
    var showUpsellDialog by remember { mutableStateOf(false) }
    var areBarsVisible by remember { mutableStateOf(true) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    val exportReport = {
        if (isProUser) {
            uiState.vehicle?.let { vehicle ->
                val file = ReportUtils.generateVehicleReport(
                    context,
                    vehicle,
                    uiState.fuelLoads,
                    uiState.maintenances,
                    uiState.expenses
                )
                file?.let { ReportUtils.shareFile(context, it) }
            }
        } else {
            showUpsellDialog = true
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = areBarsVisible || !isLandscape,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = areBarsVisible || !isLandscape,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    AdBanner(isProUser = isProUser)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Reportes") },
                            selected = true,
                            onClick = { exportReport() },
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
                vehicle = uiState.vehicle,
                monthlyExpense = uiState.monthlyExpense,
                nextOilChange = uiState.nextOilChangeKm,
                fuelPerformance = uiState.fuelPerformance,
                reminders = uiState.reminders,
                fuelEfficiencyPoints = uiState.fuelEfficiencyPoints,
                costBreakdown = uiState.costBreakdown,
                onEditVehicle = { onNavigateToEditVehicle(uiState.vehicle.id) },
                onEditReminder = onNavigateToEditReminder,
                otherVehiclesWithReminders = uiState.otherVehiclesWithReminders,
                onToggleBars = { if (isLandscape) areBarsVisible = !areBarsVisible },
                modifier = Modifier.padding(
                    top = if (areBarsVisible || !isLandscape) padding.calculateTopPadding() else 0.dp,
                    bottom = if (areBarsVisible || !isLandscape) padding.calculateBottomPadding() else 0.dp
                )
            )

            if (showVehicleSelector) {
                AlertDialog(
                    onDismissRequest = { showVehicleSelector = false },
                    title = { Text("Seleccionar Vehículo") },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    text = {
                        Column {
                            uiState.allVehicles.forEach { vehicle ->
                                val isSelected = vehicle.id == uiState.vehicle?.id
                                ListItem(
                                    headlineContent = { Text("${vehicle.brand} ${vehicle.model}") },
                                    supportingContent = { Text(vehicle.plate) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent
                                    ),
                                    modifier = Modifier.clickable {
                                        onSelectVehicle(vehicle.id)
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

            if (showUpsellDialog) {
                AlertDialog(
                    onDismissRequest = { showUpsellDialog = false },
                    title = { Text("Función Pro") },
                    text = { Text("La exportación a PDF es una función Pro. ¡Actualiza para desbloquearla y remover anuncios!") },
                    confirmButton = {
                        Button(onClick = { showUpsellDialog = false }) {
                            Text("Ver Planes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUpsellDialog = false }) {
                            Text("Más tarde")
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
    fuelEfficiencyPoints: List<FuelEfficiencyPoint>,
    costBreakdown: CostBreakdownState,
    onEditVehicle: () -> Unit,
    onEditReminder: (Int) -> Unit,
    otherVehiclesWithReminders: List<Vehicle>,
    onToggleBars: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onToggleBars() }
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alerta para otros vehículos
        if (otherVehiclesWithReminders.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Otros vehículos requieren atención",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Tienes recordatorios pendientes en: ${otherVehiclesWithReminders.joinToString { it.brand }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Vehicle Info Card with Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onEditVehicle,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

        // Analytics Section
        Text(
            text = "Estadísticas e Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tendencia de Eficiencia (km/L)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FuelEfficiencyChart(points = fuelEfficiencyPoints)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Desglose de Gastos Totales",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                CostBreakdownPieChart(breakdown = costBreakdown)
            }
        }

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
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val sampleVehicle = Vehicle(
        id = 1,
        brand = "Toyota",
        model = "Corolla",
        year = 2022,
        plate = "ABC-1234",
        currentKm = 15000
    )
    
    val sampleReminder = com.autocare.data.entity.Reminder(
        id = 1,
        vehicleId = 1,
        title = "Cambio de Aceite",
        description = "Cambio sintético 5W-30",
        type = "MAINTENANCE",
        targetKm = 15500
    )

    val sampleUiState = DashboardUiState(
        vehicle = sampleVehicle,
        allVehicles = listOf(sampleVehicle),
        monthlyExpense = 150.0,
        nextOilChangeKm = 500,
        fuelPerformance = 14.2,
        reminders = listOf(sampleReminder),
        fuelEfficiencyPoints = listOf(
            FuelEfficiencyPoint("Ene", 14.0, 0L),
            FuelEfficiencyPoint("Feb", 14.5, 0L)
        ),
        costBreakdown = CostBreakdownState(100.0, 50.0, 20.0, 170.0),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = sampleUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun DashboardScreenDarkPreview() {
    DashboardScreenPreview()
}

@Preview(showBackground = true, name = "Empty Dashboard")
@Composable
fun DashboardScreenEmptyPreview() {
    val emptyUiState = DashboardUiState(
        vehicle = null,
        allVehicles = emptyList(),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = emptyUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, name = "Other Vehicles Alert")
@Composable
fun DashboardScreenOtherVehiclesPreview() {
    val sampleVehicle = Vehicle(id = 1, brand = "Toyota", model = "Corolla", year = 2022, plate = "ABC-1234", currentKm = 15000)
    val otherVehicle = Vehicle(id = 2, brand = "Honda", model = "Civic", year = 2020, plate = "XYZ-9876", currentKm = 30000)

    val sampleUiState = DashboardUiState(
        vehicle = sampleVehicle,
        allVehicles = listOf(sampleVehicle, otherVehicle),
        monthlyExpense = 150.0,
        nextOilChangeKm = 500,
        fuelPerformance = 14.2,
        reminders = emptyList(),
        otherVehiclesWithReminders = listOf(otherVehicle),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = sampleUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DashboardContentPreview() {
    val sampleVehicle = Vehicle(
        id = 1,
        brand = "Toyota",
        model = "Corolla",
        year = 2022,
        plate = "ABC-1234",
        currentKm = 15000
    )
    
    val reminders = listOf(
        com.autocare.data.entity.Reminder(
            id = 1,
            vehicleId = 1,
            title = "Cambio de Aceite",
            description = "Cambio sintético 5W-30",
            type = "MAINTENANCE",
            targetKm = 15500
        ),
        com.autocare.data.entity.Reminder(
            id = 2,
            vehicleId = 1,
            title = "Seguro Anual",
            description = "Renovación póliza",
            type = "DOCUMENT",
            targetDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L // 5 days from now
        ),
        com.autocare.data.entity.Reminder(
            id = 3,
            vehicleId = 1,
            title = "Revisión Técnica",
            description = "Vencida",
            type = "DOCUMENT",
            targetDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L // 2 days ago
        )
    )

    AutoCareTheme {
        DashboardContent(
            vehicle = sampleVehicle,
            monthlyExpense = 2450.50,
            nextOilChange = 500,
            fuelPerformance = 12.5,
            reminders = reminders,
            fuelEfficiencyPoints = listOf(
                FuelEfficiencyPoint("01 Ene", 12.0, 0L),
                FuelEfficiencyPoint("15 Ene", 13.0, 0L),
                FuelEfficiencyPoint("01 Feb", 12.5, 0L)
            ),
            costBreakdown = CostBreakdownState(
                fuelTotal = 1500.0,
                maintenanceTotal = 800.0,
                expenseTotal = 150.5,
                totalAmount = 2450.5
            ),
            onEditVehicle = {},
            onEditReminder = {},
            otherVehiclesWithReminders = emptyList(),
            onToggleBars = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun DashboardScreenLoadingPreview() {
    val loadingUiState = DashboardUiState(
        isLoading = true
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = loadingUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480, name = "Landscape")
@Composable
fun DashboardScreenLandscapePreview() {
    val sampleVehicle = Vehicle(
        id = 1,
        brand = "Toyota",
        model = "Corolla",
        year = 2022,
        plate = "ABC-1234",
        currentKm = 15000
    )
    
    val sampleUiState = DashboardUiState(
        vehicle = sampleVehicle,
        allVehicles = listOf(sampleVehicle),
        monthlyExpense = 150.0,
        nextOilChangeKm = 500,
        fuelPerformance = 14.2,
        reminders = emptyList(),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = sampleUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, name = "Stat Card")
@Composable
fun StatCardPreview() {
    AutoCareTheme {
        StatCard(
            title = "Kilometraje",
            value = "15,000 km",
            icon = Icons.Default.DirectionsCar,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Critical Alerts")
@Composable
fun DashboardScreenCriticalPreview() {
    val sampleVehicle = Vehicle(
        id = 1,
        brand = "Ford",
        model = "F-150",
        year = 2018,
        plate = "CRD-9999",
        currentKm = 85000
    )
    
    val criticalReminder = com.autocare.data.entity.Reminder(
        id = 1,
        vehicleId = 1,
        title = "Cambio de Frenos",
        description = "Pastillas delanteras desgastadas",
        type = "MAINTENANCE",
        targetKm = 84500 // Vencido por 500km
    )

    val expiredDocument = com.autocare.data.entity.Reminder(
        id = 2,
        vehicleId = 1,
        title = "Seguro Obligatorio",
        description = "Venció el mes pasado",
        type = "DOCUMENT",
        targetDate = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L // Hace 30 días
    )

    val sampleUiState = DashboardUiState(
        vehicle = sampleVehicle,
        allVehicles = listOf(sampleVehicle),
        monthlyExpense = 1200.0,
        nextOilChangeKm = -500, // ¡YA!
        fuelPerformance = 8.5,
        reminders = listOf(criticalReminder, expiredDocument),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = sampleUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = false
        )
    }
}

@Preview(showBackground = true, name = "Vehicle Selector Dialog")
@Composable
fun DashboardScreenVehicleSelectorPreview() {
    val sampleVehicle1 = Vehicle(id = 1, brand = "Toyota", model = "Corolla", year = 2022, plate = "ABC-1234", currentKm = 15000)
    val sampleVehicle2 = Vehicle(id = 2, brand = "Honda", model = "Civic", year = 2020, plate = "XYZ-9876", currentKm = 30000)

    val sampleUiState = DashboardUiState(
        vehicle = sampleVehicle1,
        allVehicles = listOf(sampleVehicle1, sampleVehicle2),
        isLoading = false
    )

    AutoCareTheme {
        DashboardScreenContent(
            uiState = sampleUiState,
            onNavigateToAddVehicle = {},
            onNavigateToEditVehicle = {},
            onNavigateToAddFuel = {},
            onNavigateToAddMaintenance = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToEditReminder = {},
            onSelectVehicle = {},
            initialShowVehicleSelector = true
        )
    }
}
