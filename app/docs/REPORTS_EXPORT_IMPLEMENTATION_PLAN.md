# Implementation Plan: Professional Reports & Resale Value Export

This document outlines the strategy and current implementation for the professional PDF export feature in AutoCare, allowing users to generate verified service history and fleet reports.

## 1. Overview
The goal is to provide users with a "Verified Service History" document in PDF format. This document aggregates maintenance records, fuel logs, operational cost breakdowns, graphical charts, and active maintenance reminders for single vehicles or the entire fleet into a clean, printable, and shareable format.

## 2. Technology Stack
*   **PDF Generation:** Standard Android `android.graphics.pdf.PdfDocument` API for native, lightweight, and precise canvas rendering.
*   **Vector/Image Header Branding:** `ContextCompat.getDrawable()` rendered directly onto the `PdfDocument` Canvas using `Drawable.draw(canvas)`, with layer bounds constraint logic to preserve native PNG/vector crispness without bitmap distortion or overflow.
*   **Canvas Charts:** Custom 2D graphics drawn directly onto the PDF Canvas via `Paint` and `Path` primitives:
    *   **Cost Breakdown Chart:** Horizontal stacked bar graph detailing Fuel, Maintenance, and Other Expense percentages with thousands separators (`$,.2f`).
    *   **Fuel Efficiency Trend Chart:** X/Y line graph plotting point-to-point km/L performance over time.
*   **File Sharing:** `FileProvider` (`${context.packageName}.fileprovider`) to securely share generated PDFs with external apps (Email, WhatsApp, Drive).
*   **Data Sources:** Room Database repositories (`Vehicle`, `FuelLoad`, `Maintenance`, `Expense`, and `Reminder`).

## 3. Report Content & Structure
The generated PDF report includes:

*   **Header:**
    *   AutoCare App Branding Logo (`ic_app_icon_content` / `ic_launcher_foreground_image`).
    *   Report Title ("AutoCare - Reporte de Vehículo" or "AutoCare - Reporte de Flotilla Completa").
    *   Generation timestamp ("Generado el: dd/MM/yyyy HH:mm").
*   **Fleet Summary (Multi-Vehicle Mode):**
    *   Total vehicles, total accumulated fleet expenses, and breakdown across Fuel, Maintenance, and Other Expenses.
*   **Vehicle Cards (Per Vehicle):**
    *   Key Specs: Brand, Model, Year, Plate, Current Odometer, Total Spent, and Average Fuel Efficiency (km/L).
    *   Graphical Charts: Side-by-side Cost Breakdown and Fuel Efficiency Trend charts.
    *   Active Maintenance Reminders Section: Itemized table of active reminders with target mileage/date and status indicators (`[CRÍTICO]`, `[PENDIENTE]`, `[PROGRAMADO]`).
    *   Chronological Maintenance History Log: Table with Date, Service Type, Cost, Mileage, and Notes.

## 4. Implementation & Selection Dialog
*   **Vehicle Export Dialog:** When triggering report generation, an `AlertDialog` presents the user with options:
    1. **Vehículo actual** (e.g., `Toyota Corolla`)
    2. **Todos los vehículos** (e.g., `3 registrados`)
*   **Asynchronous Processing:** `DashboardViewModel.generateAllVehiclesReportData()` queries vehicle, fuel, expense, maintenance, and reminder repositories concurrently using Kotlin Coroutines before building the PDF document.

## 5. Status & Completion
- [x] Defined `FileProvider` in `AndroidManifest.xml` and `res/xml/file_paths.xml`.
- [x] Implemented `ReportUtils` helper with Canvas chart rendering and multi-vehicle dataset handling.
- [x] Integrated SVG/PNG logo positioning with strict layer bounds to prevent text overlap.
- [x] Incorporated active maintenance reminders into per-vehicle report sections.
- [x] Integrated PDF export trigger and selection dialog into `DashboardScreen` BottomBar and `HistoryScreen` TopBar.
- [x] Verified build and layout rendering using `:app:assembleDebug`.
