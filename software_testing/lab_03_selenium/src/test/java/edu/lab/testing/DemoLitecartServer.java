package edu.lab.testing;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Small local site for checking the Selenium infrastructure without installing PHP/MySQL.
 * It is NOT the real Litecart store; pass -DsiteUrl=http://localhost/litecart/ to use that.
 */
final class DemoLitecartServer implements AutoCloseable {
    private final HttpServer server;

    private DemoLitecartServer(HttpServer server) {
        this.server = server;
    }

    static DemoLitecartServer start() throws IOException {
        byte[] home = readResource("/demo/index.html");
        byte[] product = readResource("/demo/purple-duck.html");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/litecart/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                byte[] response;
                int status;
                if ("/litecart/".equals(path)) {
                    response = home;
                    status = 200;
                } else if ("/litecart/purple-duck.html".equals(path)) {
                    response = product;
                    status = 200;
                } else {
                    response = "Not found".getBytes(StandardCharsets.UTF_8);
                    status = 404;
                }
                exchange.getResponseHeaders().set("Content-Type",
                        status == 200 ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(status, response.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(response);
                }
            } finally {
                exchange.close();
            }
        });
        server.start();
        return new DemoLitecartServer(server);
    }

    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/litecart/";
    }

    private static byte[] readResource(String name) throws IOException {
        try (InputStream stream = DemoLitecartServer.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException("Missing test resource: " + name);
            }
            return stream.readAllBytes();
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
