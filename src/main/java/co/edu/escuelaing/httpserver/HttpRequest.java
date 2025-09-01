/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.httpserver;

import java.net.URI;

/**
 *
 * @author luisdanielbenavidesnavarro
 */
public class HttpRequest {
    
    URI requri = null;

    HttpRequest(URI requri) {
        this.requri = requri;
    }

    public String getValue(String paramName) {
        // Soporta múltiples parámetros en el query string
        if (requri.getQuery() == null) return null;
        String[] params = requri.getQuery().split("&");
        for (String param : params) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(paramName)) {
                return pair[1];
            }
        }
        return null;
    }

}
