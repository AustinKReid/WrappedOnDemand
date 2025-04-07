package com.akr.spotify; //test

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static final int PORT = 8080; // Must match your redirect URL port
    private static String authorizationCode;
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {
        String clientId = "acdc61d55c7e4fef9a4149183888a647";
        String clientSecret = "2efd9c410d5a4fdc9f3e555921a03f55";
        String redirectUrl = "http://localhost:" + PORT + "/callback";

        try {
            // Start the callback server
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/callback", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    authorizationCode = query.split("code=")[1].split("&")[0];

                    // Send response to browser
                    String response = "Authorization complete! You may close this window.";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }

                    latch.countDown(); // Release the main thread
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            server.start();

            SpotifyApiClient spotifyClient = new SpotifyApiClient(clientId, clientSecret, redirectUrl);

            System.out.println("1. Opening authorization in your browser...");
            System.out.println(spotifyClient.getAuthorizationUrl());

            // Wait for the callback
            System.out.println("\n2. Waiting for authorization...");
            latch.await();
            server.stop(0); // Shutdown server after getting the code

            System.out.println("3. Exchanging code for access token...");
            String accessToken = spotifyClient.getAccessToken(authorizationCode);
            System.out.println("Successfully obtained access token!");

            System.out.println("\n4. Fetching your top tracks...");
            List<SpotifyApiClient.Track> topTracks = spotifyClient.getTopTracks(accessToken);

            System.out.println("\nYour Top Tracks:");
            for (int i = 0; i < topTracks.size(); i++) {
                System.out.println((i + 1) + ". " + topTracks.get(i));
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}