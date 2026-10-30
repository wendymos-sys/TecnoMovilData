
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.Duration;
import java.util.OptionalDouble;

public class Main {

    // Representa un registro del sistema de transporte
    record Registro(
            String idUsuario,
            String ruta,
            String estacion,
            String accion,
            LocalDateTime timestamp) {
    }

    public static void main(String[] args) {

        System.out.println("Sistema TecnoMovilData");

        // Creamos una lista con los registros de pasajeros
        List<Registro> registros = List.of(

                new Registro("U1", "R1", "Centro", "entrada",
                        LocalDateTime.of(2026, 10, 20, 7, 0)),

                new Registro("U2", "R1", "Centro", "entrada",
                        LocalDateTime.of(2026, 10, 20, 7, 10)),

                new Registro("U1", "R2", "Norte", "entrada",
                        LocalDateTime.of(2026, 10, 20, 7, 20)),

                new Registro("U3", "R1", "Centro", "entrada",
                        LocalDateTime.of(2026, 10, 20, 8, 0)),

                new Registro("U2", "R2", "Norte", "entrada",
                        LocalDateTime.of(2026, 10, 20, 8, 10)),

                new Registro("U1", "R1", "Sur", "entrada",
                        LocalDateTime.of(2026, 10, 20, 8, 30)),

                new Registro("U3", "R1", "Sur", "salida",
                        LocalDateTime.of(2026, 10, 20, 8, 40)));

        // Mostramos todos los registros
        System.out.println("\nREGISTROS DEL SISTEMA:");

        for (Registro registro : registros) {
            System.out.println(registro);
        }

        // Mostramos la cantidad de registros
        System.out.println("\nTotal de registros: " + registros.size());

        // Calculamos la afluencia por estacion
        Map<String, Long> afluencia = afluenciaPorEstacion(registros);

        System.out.println("\nAFLUENCIA POR ESTACION:");

        afluencia.forEach((estacion, cantidad) -> {
            System.out.println(estacion + ": " + cantidad);
        });

        // Calculamos las horas pico
        Map<Integer, Long> horas = horasPico(registros);

        System.out.println("\nFLUJO DE PASAJEROS POR HORA:");

        horas.forEach((hora, cantidad) -> {
            System.out.println(hora + ":00 - " + cantidad + " entradas");
        });

        // Calculamos las rutas mas utilizadas
        Map<String, Long> rutas = rutasMasUtilizadas(registros);

        System.out.println("\nRUTAS MAS UTILIZADAS:");

        rutas.forEach((ruta, cantidad) -> {
            System.out.println(ruta + ": " + cantidad + " entradas");
        });

        // Calculamos los patrones de viaje
        Map<String, List<String>> patrones = patronesDeViaje(registros);

        System.out.println("\nPATRONES DE VIAJE POR USUARIO:");

        patrones.forEach((usuario, estaciones) -> {
            System.out.println(usuario + ": " + estaciones);
        });

        // Calculamos el tiempo promedio
        OptionalDouble promedio = tiempoPromedioMinutos(registros);

        System.out.println("\nTIEMPO PROMEDIO ENTRE REGISTROS:");

        if (promedio.isPresent()) {
            System.out.println(
                    promedio.getAsDouble() + " minutos");
        } else {
            System.out.println("No hay suficientes registros.");
        }

        // Definimos el umbral de entradas
        long umbral = 2;

        // Detectamos las rutas criticas
        Map<String, Long> criticas = rutasCriticas(registros, umbral);

        System.out.println("\nDETECCION DE RUTAS SOBRECARGADAS:");

        if (criticas.isEmpty()) {
            System.out.println("No se encontraron rutas sobrecargadas.");
        } else {
            criticas.forEach((ruta, cantidad) -> {
                System.out.println(
                        ruta + ": " + cantidad + " entradas - SOBRECARGADA");
            });
        }

    }

    public static Map<String, Long> afluenciaPorEstacion(
            List<Registro> registros) {

        return registros.stream()

                // Seleccionamos solamente las entradas
                .filter(registro -> registro.accion().equals("entrada"))

                // Agrupamos por nombre de estacion y contamos
                .collect(Collectors.groupingBy(
                        Registro::estacion,
                        Collectors.counting()));
    }

    public static Map<Integer, Long> horasPico(
            List<Registro> registros) {

        return registros.stream()

                // Seleccionamos solamente las entradas
                .filter(registro -> registro.accion().equals("entrada"))

                // Agrupamos por hora y contamos
                .collect(Collectors.groupingBy(
                        registro -> registro.timestamp().getHour(),
                        Collectors.counting()));
    }

    public static Map<String, Long> rutasMasUtilizadas(
            List<Registro> registros) {

        return registros.stream()

                // Seleccionamos solamente las entradas
                .filter(registro -> registro.accion().equals("entrada"))

                // Agrupamos por ruta y contamos
                .collect(Collectors.groupingBy(
                        Registro::ruta,
                        Collectors.counting()));
    }

    public static Map<String, List<String>> patronesDeViaje(
            List<Registro> registros) {

        return registros.stream()

                // Seleccionamos solamente las entradas
                .filter(registro -> registro.accion().equals("entrada"))

                // Ordenamos los registros por fecha y hora
                .sorted(Comparator.comparing(Registro::timestamp))

                // Agrupamos las estaciones por usuario
                .collect(Collectors.groupingBy(
                        Registro::idUsuario,
                        Collectors.mapping(
                                Registro::estacion,
                                Collectors.toList())));
    }

    public static OptionalDouble tiempoPromedioMinutos(
            List<Registro> registros) {

        // Ordenamos los registros por usuario y fecha
        List<Registro> ordenados = registros.stream()
                .filter(registro -> registro.accion().equals("entrada"))
                .sorted(
                        Comparator.comparing(Registro::idUsuario)
                                .thenComparing(Registro::timestamp))
                .toList();

        // Calculamos los intervalos entre registros consecutivos
        return java.util.stream.IntStream
                .range(1, ordenados.size())

                // Comparamos solamente registros del mismo usuario
                .filter(i -> ordenados.get(i - 1).idUsuario()
                        .equals(ordenados.get(i).idUsuario()))

                // Calculamos la diferencia en minutos
                .mapToLong(i -> Duration.between(
                        ordenados.get(i - 1).timestamp(),
                        ordenados.get(i).timestamp()).toMinutes())

                // Calculamos el promedio
                .average();
    }

    public static Map<String, Long> rutasCriticas(
            List<Registro> registros, long umbral) {

        // Obtenemos las entradas agrupadas por ruta
        Map<String, Long> rutas = rutasMasUtilizadas(registros);

        // Seleccionamos las rutas que superan el umbral
        return rutas.entrySet().stream()
                .filter(entrada -> entrada.getValue() > umbral)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

}
