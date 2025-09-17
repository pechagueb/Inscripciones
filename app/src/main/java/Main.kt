import kotlin.system.exitProcess

fun main() {
    val taller: String = "Kotlin Básico"
    val aforoMaximo: Int = 5
    val precioBase: Double = 100.0

    println("=== $taller – Aforo: $aforoMaximo plazas – Precio base: \$${"%.2f".format(precioBase)} ===\n")

    val modalidades = listOf("mañana", "tarde")
    println("Modalidades disponibles: ${modalidades.joinToString(", ")}\n")

    val inscripciones = mutableListOf<Map<String, Any?>>()

    // === Alta de inscripciones ===
    while (true) {
        if (inscripciones.size >= aforoMaximo) {
            println("Aforo completo ($aforoMaximo).")
            break
        }

        println("\n--- Nueva inscripción (escribe 'fin' en nombre para terminar) ---")
        val nombre = readNonEmptyString("Nombre:")
        if (nombre.equals("fin", ignoreCase = true)) break

        val edad = readPositiveInt("Edad (número entero):")
        print("Email (opcional, vacío si no tiene): ")
        val rawEmail = readLine()?.trim()
        val email: String? = if (rawEmail.isNullOrEmpty()) null else rawEmail
        val dominio: String = email?.substringAfter("@") ?: "sin-dominio"

        print("Modalidad (${modalidades.joinToString("/")}): ")
        val modalidad = readLine()?.trim()?.lowercase() ?: ""
        if (modalidad !in modalidades) {
            println("Modalidad inválida.")
            continue
        }

        val precio = calculatePrice(precioBase, edad).coerceAtLeast(0.0)

        inscripciones.add(
            mapOf(
                "nombre" to nombre,
                "edad" to edad,
                "email" to email,
                "dominio" to dominio,
                "modalidad" to modalidad,
                "precio" to precio
            )
        )

        println("Inscripción añadida: $nombre – \$${"%.2f".format(precio)}")
    }

    // === Menú interactivo ===
    menuLoop(inscripciones, precioBase)
}

// ==================== Funciones auxiliares ====================

fun menuLoop(inscripciones: MutableList<Map<String, Any?>>, precioBase: Double) {
    while (true) {
        println(
            """
            
            === Menú de opciones ===
            1. Buscar por nombre
            2. Contar por dominio
            3. Ordenar por nombre
            4. Ordenar por precio
            5. Mostrar listado final
            0. Salir
            """.trimIndent()
        )
        print("Elige una opción: ")
        when (readLine()?.trim()) {
            "1" -> {
                print("Introduce nombre a buscar: ")
                val buscado = readLine()?.trim()?.lowercase() ?: ""
                val encontrados = inscripciones.filter {
                    (it["nombre"] as? String)?.lowercase()?.contains(buscado) == true
                }
                if (encontrados.isEmpty()) println("No se encontraron coincidencias.")
                else encontrados.forEach { mostrarInscripcion(it) }
            }

            "2" -> {
                val dominios = inscripciones.groupingBy { it["dominio"] }.eachCount()
                println("Inscripciones por dominio:")
                dominios.forEach { (dom, count) -> println("$dom : $count") }
            }

            "3" -> {
                val ordenados = inscripciones.sortedBy { it["nombre"] as? String ?: "" }
                println("Ordenado por nombre:")
                ordenados.forEach { mostrarInscripcion(it) }
            }

            "4" -> {
                val ordenados = inscripciones.sortedBy { it["precio"] as? Double ?: 0.0 }
                println("Ordenado por precio:")
                ordenados.forEach { mostrarInscripcion(it) }
            }

            "5" -> {
                println("\n=== Listado final ===")
                if (inscripciones.isEmpty()) println("No hay inscripciones.")
                else inscripciones.forEach { mostrarInscripcion(it) }
                println("Resumen: ${inscripciones.size} inscripciones – Precio base: \$${"%.2f".format(precioBase)}")
            }

            "0" -> {
                println("Saliendo... ¡Gracias por usar el sistema! 👋")
                exitProcess(0)
            }

            else -> println("Opción inválida.")
        }
    }
}

fun mostrarInscripcion(ins: Map<String, Any?>) {
    val nombre = (ins["nombre"] as? String) ?: "—"
    val edad = (ins["edad"] as? Int) ?: 0
    val modalidad = (ins["modalidad"] as? String) ?: "—"
    val precio = (ins["precio"] as? Double) ?: 0.0
    val dominio = (ins["dominio"] as? String) ?: "sin-dominio"
    println("$nombre | Edad: $edad | $modalidad | \$${"%.2f".format(precio)} | dominio: $dominio")
}

fun readNonEmptyString(prompt: String): String {
    while (true) {
        print("$prompt ")
        val line = readLine()?.trim()
        if (!line.isNullOrEmpty()) return line
        println("No puede estar vacío.")
    }
}

fun readPositiveInt(prompt: String): Int {
    while (true) {
        print("$prompt ")
        try {
            val value = parseIntOrThrow(readLine())
            if (value > 0) return value else println("Debe ser > 0.")
        } catch (e: InvalidNumberException) {
            println("Error: ${e.message}")
        }
    }
}

fun parseIntOrThrow(s: String?): Int {
    val n = s?.toIntOrNull()
    if (n == null) throw InvalidNumberException("No se pudo convertir '$s' a entero")
    return n
}

class InvalidNumberException(message: String) : Exception(message)

fun calculatePrice(base: Double, edad: Int): Double {
    val descuento = when {
        edad < 18 -> 0.50
        edad >= 65 -> 0.30
        else -> 0.0
    }
    return base * (1.0 - descuento)
}
