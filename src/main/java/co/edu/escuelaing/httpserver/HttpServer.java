package co.edu.escuelaing.httpserver;

import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.PostMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;
import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import java.util.function.BiFunction;

public class HttpServer {

    private static String staticFilesFolder = "public";
    public static Map<String, Method> services = new HashMap<>();
    public static Map<String, Method> postServices = new HashMap<>();
    public static Map<String, BiFunction<HttpRequest, HttpResponse, String>> getRoutes = new HashMap<>();

    public static void get(String path, BiFunction<HttpRequest, HttpResponse, String> handler) {
        getRoutes.put(path, handler);
    }

    public static void loadServices() throws ClassNotFoundException, IOException {
        File srcDir = new File("src/main/java");
        loadServicesFromDir(srcDir, "");
    }

    private static void loadServicesFromDir(File dir, String pkg) throws ClassNotFoundException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadServicesFromDir(file, pkg + file.getName() + ".");
            } else if (file.getName().endsWith(".java")) {
                String className = pkg + file.getName().replace(".java", "");
                try {
                    Class<?> c = Class.forName(className);
                    if (c.isAnnotationPresent(RestController.class)) {
                        Method[] methods = c.getDeclaredMethods();
                        for (Method m : methods) {
                            if (m.isAnnotationPresent(GetMapping.class)) {
                                String mapping = m.getAnnotation(GetMapping.class).value();
                                services.put(mapping, m);
                            }
                            if (m.isAnnotationPresent(PostMapping.class)) {
                                String mapping = m.getAnnotation(PostMapping.class).value();
                                postServices.put(mapping, m);
                            }
                        }
                    }
                } catch (Throwable t) {
                }
            }
        }
    }

    public static void runServer(String[] args) throws IOException, URISyntaxException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException {
        loadServices();

        get("/manual/hello",
                (req, res) -> "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello from manual route!");

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;

        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String inputLine, outputLine = null;

            boolean firstline = true;
            URI requri = null;
            String method = "GET";
            int contentLength = 0;

            // Leer headers y método
            while ((inputLine = in.readLine()) != null) {
                if (firstline) {
                    String[] parts = inputLine.split(" ");
                    method = parts[0];
                    requri = new URI(parts[1]);
                    System.out.println("Path: " + requri.getPath());
                    firstline = false;
                }
                if (inputLine.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(inputLine.split(":")[1].trim());
                }
                System.out.println("Received: " + inputLine);
                if (inputLine.isEmpty()) {
                    break;
                }
            }

            String body = null;
            if ("POST".equalsIgnoreCase(method) && contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                body = new String(bodyChars);
            }

            if ("POST".equalsIgnoreCase(method) && postServices.containsKey(requri.getPath())) {
                outputLine = invokePostService(requri, body);
            } else if (services.containsKey(requri.getPath())) {
                outputLine = invokeService(requri);
            } else if (getRoutes.containsKey(requri.getPath())) {
                outputLine = getRoutes.get(requri.getPath()).apply(new HttpRequest(requri), new HttpResponse());
            } else {
                String filePath = staticFilesFolder + (requri.getPath().equals("/") ? "/index.html" : requri.getPath());
                File file = new File(filePath.startsWith("/") ? filePath.substring(1) : filePath);
                if (file.exists() && !file.isDirectory()) {
                    String contentType = getContentType(filePath);
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    out.print("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n");
                    out.flush();
                    clientSocket.getOutputStream().write(fileBytes);
                    clientSocket.getOutputStream().flush();
                    outputLine = null;
                } else {
                    out.print("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nArchivo no encontrado");
                    outputLine = null;
                }
            }

            if (outputLine != null) {
                out.print(outputLine);
                out.flush();
            }
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String invokePostService(URI requri, String body)
            throws IllegalAccessException, InvocationTargetException {
        Method m = postServices.get(requri.getPath());
        if (m == null) {
            return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nEndpoint no encontrado";
        }
        String header = "HTTP/1.1 200 OK\r\n"
                + "content-type: application/json\r\n"
                + "\r\n";
        if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
            return header + m.invoke(null, body);
        } else {
            return header + m.invoke(null);
        }
    }

    private static String invokeService(URI requri) throws IllegalAccessException, InvocationTargetException {
        HttpRequest req = new HttpRequest(requri);
        HttpResponse res = new HttpResponse();
        String servicePath = requri.getPath();
        Method m = services.get(servicePath);
        if (m == null) {
            return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nEndpoint no encontrado";
        }
        String header = "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "\r\n";
        String[] argsValues = null;
        if (m.getParameterCount() > 0 && m.getParameterAnnotations()[0].length > 0) {
            RequestParam rp = (RequestParam) m.getParameterAnnotations()[0][0];
            if (requri.getQuery() == null) {
                argsValues = new String[] { rp.defaultValue() };
            } else {
                String queryParamName = rp.value();
                argsValues = new String[] { req.getValue(queryParamName) };
            }
            return header + m.invoke(null, (Object[]) argsValues);
        } else {
            return header + m.invoke(null);
        }
    }

    public static void start(String[] args) throws IOException, URISyntaxException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException {
        runServer(args);
    }

    private static String getContentType(String filePath) {
        if (filePath.endsWith(".html"))
            return "text/html";
        if (filePath.endsWith(".css"))
            return "text/css";
        if (filePath.endsWith(".js"))
            return "application/javascript";
        if (filePath.endsWith(".png"))
            return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg"))
            return "image/jpeg";
        if (filePath.endsWith(".gif"))
            return "image/gif";
        return "text/plain";
    }

}
