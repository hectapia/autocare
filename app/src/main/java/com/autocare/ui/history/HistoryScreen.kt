package com.autocare.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vehicleId: Int,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onEditFuel: (Int) -> Unit,
    onEditMaint: (Int) -> Unit,
    onEditExp: (Int) -> Unit,
    onAddFuel: (Int) -> Unit,
    onAddMaint: (Int) -> Unit,
    onAddExp: (Int) -> Unit,
    onEditReminder: (Int) -> Unit
) {
    val uiState by viewModel.getHistory(vehicleId).collectAsState(initial = HistoryUiState())
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todo", "Gas", "Mantenimiento", "Gastos")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Historial") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        1 -> onAddFuel(vehicleId)
                        2 -> onAddMaint(vehicleId)
                        3 -> onAddExp(vehicleId)
                        else -> onAddFuel(vehicleId) // Default to fuel
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Registro"
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val filteredList = when (selectedTab) {
                1 -> uiState.items.filter { it is HistoryItem.Fuel }
                2 -> uiState.items.filter { it is HistoryItem.Maint }
                3 -> uiState.items.filter { it is HistoryItem.Exp }
                else -> uiState.items
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros aún", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { item ->
                        HistoryCard(
                            item = item,
                            onClick = {
                                when (item) {
                                    is HistoryItem.Fuel -> onEditFuel(item.data.id)
                                    is HistoryItem.Maint -> onEditMaint(item.data.id)
                                    is HistoryItem.Exp -> onEditExp(item.data.id)
                                }
                            },
                            onReminderClick = { reminderId ->
                                onEditReminder(reminderId)
                            }
                        )
                    }
                }
            }
        }
    }
}

data class HistoryDisplayData(
    val title: String,
    val amount: String,
    val date: Long,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit,
    onReminderClick: (Int) -> Unit
) {
    val data = when (item) {
        is HistoryItem.Fuel -> HistoryDisplayData(
            "Carga de Combustible",
            "$${String.format("%.2f", item.data.totalCost)}",
            item.data.date,
            Icons.Default.EvStation,
            MaterialTheme.colorScheme.primary
        )
        is HistoryItem.Maint -> HistoryDisplayData(
            item.data.type,
            "$${String.format("%.2f", item.data.cost)}",
            item.data.date,
            Icons.Default.Build,
            MaterialTheme.colorScheme.secondary
        )
        is HistoryItem.Exp -> HistoryDisplayData(
            item.data.category,
            "$${String.format("%.2f", item.data.amount)}",
            item.data.date,
            Icons.Default.Receipt,
            MaterialTheme.colorScheme.tertiary
        )
    }

    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(data.date))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(data.icon, contentDescription = null, tint = data.color)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(dateStr, style = MaterialTheme.typography.bodySmall)
            }
            if (item is HistoryItem.Maint && item.reminderId != null) {
                IconButton(
                    onClick = { onReminderClick(item.reminderId) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Tiene recordatorio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(data.amount, style = MaterialTheme.typography.titleMedium, color = data.color, fontWeight = FontWeight.Bold)
        }
    }
}

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList()
)

sealed class HistoryItem {
    data class Fuel(val data: com.autocare.data.entity.FuelLoad) : HistoryItem()
    data class Maint(val data: com.autocare.data.entity.Maintenance, val reminderId: Int? = null) : HistoryItem()
    data class Exp(val data: com.autocare.data.entity.Expense) : HistoryItem()
    val date: Long get() = when(this) {
        is Fuel -> data.date
        is Maint -> data.date
        is Exp -> data.date
    }
}
