package co.edu.escuelaing.microspringboot.examples;

import co.edu.escuelaing.microspringboot.annotations.PostMapping;
import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ClaseController {
    @PostMapping("/api/components")
    public static String addComponentApiPost(String body) {
        System.out.println("POST recibido: " + body);
        String name = extractJsonValue(body, "name");
        String type = extractJsonValue(body, "type");
        String description = extractJsonValue(body, "description");
        String rating = extractJsonValue(body, "rating");
        String comp = String.format("%s|%s|%s|%s", name, type, description, rating);
        componentes.add(comp);
        return "{\"status\":\"OK\"}";
    }

    private static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int start = idx + search.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != '"') end++;
        return json.substring(start, end);
    }

    private static final List<String> componentes = new CopyOnWriteArrayList<>();

    @GetMapping("/hello")
    public static String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }

    @GetMapping("/pi")
    public static String pi() {
        return String.valueOf(Math.PI);
    }

    @GetMapping("/add")
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

    @GetMapping("/api/components")
    public static String getComponentsApi() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (String c : componentes) {
            String[] parts = c.split("\\|");
            if (!first) sb.append(",");
            sb.append("{\"name\":\"").append(parts[0]).append("\"")
              .append(",\"type\":\"").append(parts[1]).append("\"")
              .append(",\"description\":\"").append(parts[2]).append("\"")
              .append(",\"rating\":").append(parts[3]).append("}");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    @GetMapping("/api/components/add") 
    public static String addComponentApiGet(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "rating") String rating
    ) {
        String comp = String.format("%s|%s|%s|%s", name, type, description, rating);
        componentes.add(comp);
        return "OK";
    }

    @GetMapping("/api/components/post")
    public static String addComponentApiPost(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "rating") String rating
    ) {
        String comp = String.format("%s|%s|%s|%s", name, type, description, rating);
        componentes.add(comp);
        return "OK";
    }
}
