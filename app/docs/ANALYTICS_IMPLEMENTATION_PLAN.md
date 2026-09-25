# Implementation Plan: Data Visualization & Analytics

This document outlines the step-by-step technical plan to implement **Fuel Efficiency Trends** (Line Chart) and **Cost Breakdown Analysis** (Pie Chart) within the AutoCare application.

---

## 1. Technical Strategy & Library Selection

To keep the application highly performant, lightweight, and perfectly tailored to Material 3 Design, we will combine a modern, Compose-native library for complex charts with a custom Canvas approach for simple visualizations:
1.  **Line/Bar Chart (Fuel Efficiency)**: We will utilize **Vico**, a powerful, feature-rich, and fully Compose-native charting library. To handle space constraints and maintain a reliable tendency (min. 6-10 points):
    *   **Horizontal Scrolling**: Enable horizontal scrolling in the chart state to support a long history without overcrowding the viewport.
    *   **Axis Formatting**: Use custom value formatters to skip labels at specific intervals (e.g., every 2nd entry) and maintain consistent small text sizing (10.sp) for both axes to ensure a clean horizontal layout without text rotation.
    *   **Landscape UX**: In landscape mode, the TopBar and BottomBar will hide on tap to maximize chart visibility.
2.  **Pie Chart (Cost Breakdown)**: We will build a lightweight, custom `@Composable` using `Canvas`.
    *   **Distinct Visuals**: We will use high-contrast Material 3 colors (Primary, Error, Tertiary) to make the breakdown notable at first sight.

---

## 2. Step 1: Update Dependencies

Add Vico dependencies to `C:/dev/autocare/app/build.gradle.kts`:

```kotlin
dependencies {
    // Vico Chart Library for Jetpack Compose
    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.22")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.22")
    implementation("com.patrykandpatrick.vico:core:2.0.0-alpha.22")
}
```

---

## 3. Step 2: Data Architecture & ViewModel Logic

We need to update `DashboardUiState` and `DashboardViewModel` to process and deliver structured historical data.

### Data Models
Create a specific data model for fuel entries in analytics inside `DashboardViewModel.kt`:
```kotlin
data class FuelEfficiencyPoint(
    val dateLabel: String,
    val kmPerLiter: Double,
    val timestamp: Long
)

data class CostBreakdownState(
    val fuelTotal: Double,
    val maintenanceTotal: Double,
    val expenseTotal: Double,
    val totalAmount: Double
)
```

### UI State Integration
Add these fields to `DashboardUiState`:
```kotlin
data class DashboardUiState(
    // ...
    val fuelEfficiencyPoints: List<FuelEfficiencyPoint> = emptyList(),
    val costBreakdown: CostBreakdownState = CostBreakdownState(0.0, 0.0, 0.0, 0.0),
    // ...
)
```

### Business Logic in `DashboardViewModel.calculateUiState`
1.  **Calculating Point-to-Point Fuel Efficiency**:
    Instead of a single cumulative summary, sort `fuelLoads` chronologically by `odometer`. 
    For each consecutive pair of `FuelLoad` entries:
    $$\text{Km/L} = \frac{\text{Odometer}_{\text{current}} - \text{Odometer}_{\text{previous}}}{\text{Liters}_{\text{current}}}$$
    Format the `date` into a short readable string (e.g., `"dd MMM"`) for the X-axis label.
    *Note: We provide the full history to the UI as the chart supports horizontal scrolling.*

2.  **Aggregating Costs**:
    Calculate total lifetime or selected time range spending:
    *   `fuelTotal = fuelLoads.sumOf { it.totalCost }`
    *   `maintenanceTotal = maintenances.sumOf { it.cost }`
    *   `expenseTotal = expenses.sumOf { it.amount }`
    *   `totalAmount = fuelTotal + maintenanceTotal + expenseTotal`

---

## 4. Step 3: Building the Visual Components

### Component A: Custom Animated Pie Chart (`CostBreakdownPieChart.kt`)
Create a custom canvas-based pie chart with sleek entry animations using `Animatable`.

```kotlin
@Composable
fun CostBreakdownPieChart(
    breakdown: CostBreakdownState,
    modifier: Modifier = Modifier
) {
    // ... implementation using Canvas and drawArc ...
    // Includes LegendItem with percentage calculations
}
```

### Component B: Line Chart via Vico (`FuelEfficiencyChart.kt`)
Utilize the Vico library to map `fuelEfficiencyPoints` onto an interactive Cartesian line graph with horizontal scrolling enabled.

```kotlin
@Composable
fun FuelEfficiencyChart(
    points: List<FuelEfficiencyPoint>,
    modifier: Modifier = Modifier
) {
    // Uses CartesianChartModelProducer for dynamic data updates
    // rememberVicoScrollState(scrollEnabled = true) for infinite history
    // skip intervals (every 2nd label) on X-axis for readability
    // 10sp text size for all axis labels
}
```

---

## 5. Step 4: UI Screen Layout Integration

Modify `DashboardScreen.kt` to house these two components:

1.  **Landscape Optimization**:
    *   Use `LocalConfiguration.current.orientation` to detect landscape mode.
    *   Wrap `TopAppBar` and `NavigationBar` in `AnimatedVisibility`.
    *   Toggle visibility on screen tap using a clickable modifier on the main container to maximize chart area.

2.  **Analytics Section**:
    *   Add a section header `"Estadísticas e Insights"`.
    *   Place `FuelEfficiencyChart` and `CostBreakdownPieChart` within `Card` containers.
