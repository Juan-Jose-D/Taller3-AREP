package co.edu.escuelaing.microspringboot.examples;

import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ClaseController {
    // Para simular almacenamiento en memoria
    private static final List<String> componentes = new CopyOnWriteArrayList<>();

    @GetMapping("/App/hello")
    public static String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }

    @GetMapping("/App/pi")
    public static String pi() {
        return String.valueOf(Math.PI);
    }

    // Endpoints para index.html (favoritos)
    @GetMapping("/components/add")
    public static String addComponent(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "rating") String rating
    ) {
        String comp = String.format("%s|%s|%s|%s", name, type, description, rating);
        componentes.add(comp);
        return "OK";
    }

    @GetMapping("/components/list")
    public static String listComponents() {
        StringBuilder sb = new StringBuilder();
        for (String c : componentes) {
            String[] parts = c.split("\\|");
            sb.append("<tr>")
              .append("<td>").append(parts[0]).append("</td>")
              .append("<td>").append(parts[1]).append("</td>")
              .append("<td>").append(parts[2]).append("</td>")
              .append("<td>").append(parts[3]).append("</td>")
              .append("</tr>");
        }
        return sb.toString();
    }
}
