package com.autocare.data

object VehicleData {
    val brands = listOf(
        "Toyota", "Ford", "Volkswagen", "Chevrolet", "Honda", "Nissan", "Fiat", 
        "Peugeot", "Renault", "Hyundai", "Kia", "Mazda", "Mercedes-Benz", "BMW", 
        "Audi", "Jeep", "Mitsubishi", "Suzuki", "Citroën", "Volvo"
    )
    
    val modelsByBrand = mapOf(
        "Toyota" to listOf("Corolla", "Hilux", "Yaris", "Etios", "SW4", "RAV4", "Camry", "Prius", "Land Cruiser"),
        "Ford" to listOf("Ranger", "Fiesta", "Focus", "EcoSport", "Ka", "Kuga", "Mustang", "Territory", "F-150"),
        "Volkswagen" to listOf("Gol", "Amarok", "Vento", "Polo", "T-Cross", "Taos", "Nivus", "Virtus", "Tiguan", "Golf"),
        "Chevrolet" to listOf("Onix", "Cruze", "S10", "Tracker", "Prisma", "Spin", "Equinox", "Trailblazer", "Montana"),
        "Honda" to listOf("Civic", "HR-V", "Fit", "CR-V", "City", "Accord", "WR-V"),
        "Nissan" to listOf("Frontier", "Versa", "Kicks", "Sentra", "March", "X-Trail", "Leaf"),
        "Fiat" to listOf("Cronos", "Toro", "Strada", "Mobi", "Argo", "Pulse", "Fastback", "Fiorino", "500"),
        "Peugeot" to listOf("208", "2008", "3008", "Partner", "308", "408", "5008", "Expert"),
        "Renault" to listOf("Sandero", "Logan", "Kwid", "Duster", "Oroch", "Kangoo", "Alaskan", "Stepway", "Master"),
        "Hyundai" to listOf("HB20", "Creta", "Tucson", "Santa Fe", "Kona", "Ioniq", "Venue", "Elantra"),
        "Kia" to listOf("Sportage", "Seltos", "Cerato", "Rio", "Picanto", "Carnival", "Niro", "Sorento"),
        "Mazda" to listOf("CX-5", "CX-30", "Mazda 3", "Mazda 2", "CX-9", "MX-5"),
        "Mercedes-Benz" to listOf("Clase A", "Clase C", "Clase E", "GLA", "GLC", "GLE", "Sprinter"),
        "BMW" to listOf("Serie 1", "Serie 3", "Serie 5", "X1", "X3", "X5", "X7"),
        "Audi" to listOf("A1", "A3", "A4", "Q2", "Q3", "Q5", "Q7", "e-tron"),
        "Jeep" to listOf("Renegade", "Compass", "Commander", "Grand Cherokee", "Wrangler", "Gladiator"),
        "Mitsubishi" to listOf("L200", "Outlander", "Montero", "Eclipse Cross", "ASX"),
        "Suzuki" to listOf("Swift", "Vitara", "Jimny", "Baleno", "S-Cross"),
        "Citroën" to listOf("C3", "C3 Aircross", "C4 Cactus", "C5 Aircross", "Berlingo", "Jumpy"),
        "Volvo" to listOf("XC40", "XC60", "XC90", "S60", "V60")
    )

    val years: List<String>
        get() {
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) // August is 7
            val maxYear = if (month >= java.util.Calendar.AUGUST) currentYear + 1 else currentYear
            return (maxYear downTo 1980).map { it.toString() }
        }

    val vehicleTypes = listOf("Auto", "Motocicleta", "Bicicleta", "Camioneta", "Otro")

    val maintenanceCategories = mapOf(
        "Mecánico General" to listOf(
            "Afinación mayor/menor",
            "Cambio de aceite y filtros",
            "Mano de obra general",
            "Diagnóstico por escáner"
        ),
        "Motor" to listOf(
            "Bandas de distribución / tensores",
            "Bujías y bobinas de encendido",
            "Empaques, juntas y retenes",
            "Sistema de enfriamiento"
        ),
        "Suspensión" to listOf(
            "Amortiguadores y bases",
            "Horquillas, bujes y rótulas",
            "Alineación y balanceo",
            "Terminales de dirección"
        ),
        "Frenos" to listOf(
            "Pastillas / Balatas",
            "Discos y rectificado",
            "Líquido de frenos",
            "Mangueras y calipers"
        ),
        "Eléctrico" to listOf(
            "Batería / Acumulador",
            "Alternador y marcha",
            "Luces, faros y fusibles",
            "Sensores y cableado"
        ),
        "Refacciones" to listOf(
            "Tornillería y herrajes",
            "Fluidos extra",
            "Kit de embrague (clutch)",
            "Limpiaparabrisas y cosmética"
        )
    )

    val expenseCategories = listOf(
        "Seguro",
        "Impuestos",
        "Lavado",
        "Estacionamiento",
        "Peaje",
        "Multa",
        "Combustible (Aditivos)",
        "Otro"
    )
}
