package negocio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Validaciones de datos de entrada. Todos los métodos lanzan
 * IllegalArgumentException con un mensaje claro cuando el dato es inválido.
 */
public class Validador {

    private Validador() { }

    /** El texto no puede ser nulo ni estar vacío. */
    public static String texto(String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    /** Convierte a entero >= 0; valida vacío, formato y signo. */
    public static int entero(String campo, String valor) {
        String limpio = texto(campo, valor);
        int numero;
        try {
            numero = Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número entero.");
        }
        if (numero < 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
        }
        return numero;
    }

    /** Convierte a decimal >= 0 (acepta coma o punto); valida vacío, formato y signo. */
    public static double decimal(String campo, String valor) {
        String limpio = texto(campo, valor).replace(',', '.');
        double numero;
        try {
            numero = Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número.");
        }
        if (numero < 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
        }
        return numero;
    }

    /** Cédula: exactamente 10 dígitos. */
    public static String cedula(String valor) {
        String limpio = texto("cédula", valor);
        if (!limpio.matches("\\d{10}")) {
            throw new IllegalArgumentException("La cédula debe tener exactamente 10 dígitos numéricos.");
        }
        return limpio;
    }

    /** Teléfono: solo dígitos (entre 7 y 10). */
    public static String telefono(String valor) {
        String limpio = texto("teléfono", valor);
        if (!limpio.matches("\\d{7,10}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 10 dígitos numéricos.");
        }
        return limpio;
    }

    /** Año del vehículo: entre 1950 y el año siguiente al actual. */
    public static int anio(String valor) {
        int anio = entero("año", valor);
        int maximo = LocalDate.now().getYear() + 1;
        if (anio < 1950 || anio > maximo) {
            throw new IllegalArgumentException("El año debe estar entre 1950 y " + maximo + ".");
        }
        return anio;
    }

    /** Fecha en formato AAAA-MM-DD. */
    public static LocalDate fecha(String campo, String valor) {
        String limpio = texto(campo, valor);
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe tener el formato AAAA-MM-DD.");
        }
    }
}
