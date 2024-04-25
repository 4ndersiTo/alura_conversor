import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import constant.KeyAPI;

import static constant.CurrencyCode.*;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {

    private final static String MSG_INGRESO_MONTO = "Ingrese el monto en ";

    public static double obtenerTasaDeConversion(List<String> listTipos) throws IOException {

        String url_str = "https://v6.exchangerate-api.com/v6/"+ KeyAPI.KEY +"/latest/" + listTipos.get(1);

        URL url = new URL(url_str);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject jsonobj = root.getAsJsonObject();

        JsonObject conversionRates = jsonobj.getAsJsonObject("conversion_rates");
        return conversionRates.get(listTipos.get(2)).getAsDouble();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String opcion;
        List<String> listConversiones = new ArrayList<>();
        while (true) {
            System.out.print("""
                    **************************************************
                    Bienvenido al conversor de moneda.
                    1) Dólar ==> Sol Peruano
                    2) Sol peruano ==> Dólar
                    3) Dólar ==> Peso Argentino
                    4) Peso Argentino ==> Dólar
                    5) Dólar ==> Real brasileño
                    6) Real brasileño ==> Dólar
                    7) Historial de conversiones
                    8) Salir
                    **************************************************
                    """);

            System.out.print("Elija una opción válida: ");
            opcion = scanner.nextLine();

            if (opcion.equals("7")) {
                if (!listConversiones.isEmpty()) {
                    System.out.println("######## HISTORIAL DE CONVERSIONES ##########");
                    listConversiones.forEach(System.out::println);
                } else {
                    System.out.println("Historial vacio, haga una conversión");
                }

                System.out.println("\n\n\n");
                continue;

            } else if (opcion.equals("8")) { //TODO: SALIR
                System.out.println("Se cerró sistema de conversión");
                break;
            }

            List<List<String>> opciones2 = List.of(
                    List.of("1", USD.name(), PEN.name()),
                    List.of("2", PEN.name(), USD.name()),
                    List.of("3", USD.name(), ARS.name()),
                    List.of("4", ARS.name(), USD.name()),
                    List.of("5", USD.name(), BRL.name()),
                    List.of("6", BRL.name(), USD.name())
            );

            final String opctionFinal = opcion;
            if (opciones2.stream().map(list -> list.get(0)).toList().contains(opcion)) {

                Optional<List<String>> listOptional = opciones2.stream()
                        .filter(elemento -> elemento.get(0).equals(opctionFinal))
                        .findFirst();

                listOptional.ifPresent(listSeleccionado -> {
                    String label1 = Main.getLabel(listSeleccionado.get(1));
                    String label2 = Main.getLabel(listSeleccionado.get(2));

                    String context = "OPCION " + opctionFinal + ": " + label1 + " a " + label2;
                    System.out.println("############################ " + context + " ############################");
                    System.out.print(MSG_INGRESO_MONTO + label1 + ": ");
                    String montoAConvertir = scanner.nextLine();
                    double valorConvertido = 0.0;
                    try {
                        valorConvertido = Main.valorConvertido(montoAConvertir, listSeleccionado);
                        System.out.println("Valor convertido es: " + valorConvertido + " " + label2 + "\n\n\n");
                        listConversiones.add(">> " + context + ": " + montoAConvertir + " ==> " + valorConvertido + " | FECHA DE CONVERSIÓN: " + Main.getTimestamp());
                    } catch (IOException e) {
                        System.out.println("Ocurrió un error al consultar la API");
                        throw new RuntimeException(e);
                    }
                });

            } else {
                System.out.println(">>>>>> Opción no válida, ingrese una opción válida");
            }
        }
        scanner.close();
    }

    public static Double valorConvertido(String input, List<String> listTipos) throws IOException {
        Double montoConvertir = Double.parseDouble(input);
        Double tasa = Main.obtenerTasaDeConversion(listTipos);
        return montoConvertir * tasa;
    }

    private static String getLabel(String tipo) {
        return switch (tipo) {
            case "USD" -> "DOLARES";
            case "PEN" -> "SOLES";
            case "ARS" -> "Pesos Argentinos";
            case "BRL" -> "Reales basileños";
            default -> "";
        };
    }

    private static String getTimestamp() {
        Date fechaActual = new Date();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatoFecha.format(fechaActual);
    }

}