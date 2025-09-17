// Mini-proyecto: Registro de Inscripciones a un Taller
// Implementación en consola siguiendo las fases del enunciado.

import kotlin.system.exitProcess

fun main() {
    // ---------------------------
    // 1) Datos base y constantes
    // ---------------------------
    // Elegimos val para constantes que no van a cambiar durante la ejecución.
    // Usaríamos var cuando necesitemos reasignar la variable (p. ej. contador que se
    // actualiza fuera de una colección o un estado que cambie de forma global).
    val taller: String = "Kotlin Básico"        // String: nombre del taller (no cambia)
    val aforoMaximo: Int = 5                    // Int: número entero de plazas
    val precioBase: Double = 100.0              // Double: precio que puede contener decimales

    // Mostrar encabezado usando templates
    println("=== $taller – Aforo: $aforoMaximo plazas – Precio base: \$${"%.2f".format(precioBase)} ===\n")

    // MutableList para guardar inscripciones (cada inscripcion es un Map)
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

        // Leer nombre no vacío
        val nombre = readNonEmptyString("Nombre:")
        if (nombre.equals("fin", ignoreCase = true)) break

        // Leer edad segura (>0)
        val edad = readPositiveInt("Edad (número entero):")

        // Leer email (opcional). Si vacío -> null
        print("Email (opcional, vacío si no tiene): ")
        val rawEmail = readLine()?.trim()
        val email: String? = if (rawEmail.isNullOrEmpty()) null else rawEmail
        val dominio: String = email?.substringAfter("@") ?: "sin-dominio"

        // Elegir modalidad válida (continue si inválida)
        print("Modalidad (${modalidades.joinToString("/")}): ")
        val modalidad = readLine()?.trim()?.lowercase() ?: ""
        if (modalidad !in modalidades) {
            println("Modalidad inválida.")
            continue
        }

        /**
         * Calcula el precio aplicando reglas:
         * - <18 -> 50% descuento
         * - >=65 -> 30% descuento
         * - else -> sin descuento
         *
         * Uso de when para claridad en varias ramas.
         */
        val precio = calculatePrice(precioBase, edad).coerceAtLeast(0.0)

        // Guardar inscripción como Map
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

    // ---------------------------
    // Operadores y estadísticas
    // ---------------------------
    println("\n=== Estadísticas ===")
    val total = inscripciones.size
    println("Número de inscripciones: $total")

    if (total > 0) {
        // map para obtener solo los precios
        val precios = inscripciones.map { (it["precio"] as? Double) ?: 0.0 }

        // precio promedio
        val promedio = precios.average()
        println("Precio promedio: \$${"%.2f".format(promedio)}")

        // mayor y menor precio
        val maxPrecio = precios.maxOrNull() ?: 0.0
        val minPrecio = precios.minOrNull() ?: 0.0
        println("Precio mayor: \$${"%.2f".format(maxPrecio)}")
        println("Precio menor: \$${"%.2f".format(minPrecio)}")

        // menores de edad (filter)
        val menores = inscripciones.filter { (it["edad"] as? Int ?: 0) < 18 }
        println("Inscritos menores de edad: ${menores.size}")

        // sortedByDescending por precio (demostración)
        val ordenadosDesc = inscripciones.sortedByDescending { (it["precio"] as? Double) ?: 0.0 }
        println("\nListado ordenado por precio (desc):")
        ordenadosDesc.forEach { m ->
            val n = m["nombre"]
            val p = m["precio"] as? Double ?: 0.0
            println("- $n : \$${"%.2f".format(p)}")
        }
    } else {
        println("No hay inscripciones para calcular estadísticas.")
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
            "1" -> { //1. Buscar por nombre
                print("Introduce nombre a buscar: ")
                val buscado = readLine()?.trim()?.lowercase() ?: ""
                val encontrados = inscripciones.filter {
                    (it["nombre"] as? String)?.lowercase()?.contains(buscado) == true
                }
                if (encontrados.isEmpty()) println("No se encontraron coincidencias.")
                else encontrados.forEach { mostrarInscripcion(it) }
            }

            "2" -> { //2. Contar por dominio
                val dominios = inscripciones.groupingBy { it["dominio"] }.eachCount()
                println("Inscripciones por dominio:")
                dominios.forEach { (dom, count) -> println("$dom : $count") }
            }

            "3" -> { //3. Ordenar por nombre
                val ordenados = inscripciones.sortedBy { it["nombre"] as? String ?: "" }
                println("Ordenado por nombre:")
                ordenados.forEach { mostrarInscripcion(it) }
            }

            "4" -> { //4. Ordenar por precio
                val ordenados = inscripciones.sortedBy { it["precio"] as? Double ?: 0.0 }
                println("Ordenado por precio:")
                ordenados.forEach { mostrarInscripcion(it) }
            }

            "5" -> { //5. Mostrar listado final
                println("\n=== Listado final ===")
                if (inscripciones.isEmpty()) println("No hay inscripciones.")
                else inscripciones.forEach { mostrarInscripcion(it) }
                println("Resumen: ${inscripciones.size} inscripciones – Precio base: \$${"%.2f".format(precioBase)}")
            }

            "0" -> { //0. Salir
                println("Saliendo... ¡Gracias por usar el sistema! 👋")
                exitProcess(0)
            }

            else -> println("Opción inválida.")
        }
    }
}

// ---------------------------
// Funciones auxiliares
// ---------------------------
fun mostrarInscripcion(ins: Map<String, Any?>) {
    val nombre = (ins["nombre"] as? String) ?: "—"
    val edad = (ins["edad"] as? Int) ?: 0
    val modalidad = (ins["modalidad"] as? String) ?: "—"
    val precio = (ins["precio"] as? Double) ?: 0.0
    val dominio = (ins["dominio"] as? String) ?: "sin-dominio"
    println("$nombre | Edad: $edad | $modalidad | \$${"%.2f".format(precio)} | dominio: $dominio")
}

/**
 * Lee una cadena no vacía desde consola. Repite hasta recibir algo no vacío.
 */
fun readNonEmptyString(prompt: String): String {
    while (true) {
        print("$prompt ")
        val line = readLine()?.trim()
        if (!line.isNullOrEmpty()) return line
        println("No puede estar vacío.")
    }
}

/**
 * Lee un entero positivo usando conversión segura toIntOrNull().
 * Si la conversión falla o el número <= 0, repite hasta obtener un valor válido.
 *
 * Además, demuestra la versión con excepción personalizada (capturada aquí).
 */
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

/**
 * Intenta convertir una cadena a Int.
 * Lanza InvalidNumberException si falla.
 */
fun parseIntOrThrow(s: String?): Int {
    val n = s?.toIntOrNull()
    if (n == null) throw InvalidNumberException("No se pudo convertir '$s' a entero")
    return n
}

/**
 * Excepción personalizada para entradas numéricas inválidas.
 */
class InvalidNumberException(message: String) : Exception(message)

/**
 * Calcula el precio aplicando reglas:
 * - <18 -> 50% descuento
 * - >=65 -> 30% descuento
 * - else -> sin descuento
 *
 * Uso de when para claridad en varias ramas.
 */
fun calculatePrice(base: Double, edad: Int): Double {
    val descuento = when {
        edad < 18 -> 0.50
        edad >= 65 -> 0.30
        else -> 0.0
    }
    return base * (1.0 - descuento)
}
