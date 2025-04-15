package com.akr.spotify;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import io.github.cdimascio.dotenv.Dotenv; //

public class Main {
    private static final int PORT = 8080;
    private static String authorizationCode;
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load(); //
        String clientId = dotenv.get("clientId"); //
        String clientSecret = dotenv.get("clientSecret"); //

        String redirectUrl = "http://localhost:" + PORT + "/callback";

        try {
            // Start the callback server
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/callback", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    authorizationCode = query.split("code=")[1].split("&")[0];

                    String response = "Authorization Complete";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }

                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            server.start();

            SpotifyApiClient spotifyClient = new SpotifyApiClient(clientId, clientSecret, redirectUrl);

            System.out.println("1. Opening Authorization In Browser");
            System.out.println(spotifyClient.getAuthorizationUrl());

            System.out.println("\n2. Waiting For Authorization");
            latch.await();
            server.stop(0);

            System.out.println("3. Exchanging code for access token...");
            String accessToken = spotifyClient.getAccessToken(authorizationCode);
            System.out.println("Access Token Received!");

            System.out.println("\n4. Fetching Top Tracks");
            List<SpotifyApiClient.Track> topTracks = spotifyClient.getTopTracks(accessToken);

            System.out.println("\nYour Top Tracks:");

            //hand
            int years = 0;

            for (int i = 0; i < topTracks.size(); i++) {
                System.out.println((i + 1) + ". " + topTracks.get(i) + " Released In " + spotifyClient.getSongReleaseYear(topTracks.get(i)));
                years = years + spotifyClient.getSongReleaseYear(topTracks.get(i));
            }

            //hand
            System.out.println("Average Year of Song " + years/50);


            List<SpotifyApiClient.Artist> topArtists = spotifyClient.getTopArtists(accessToken);

            System.out.println(topArtists);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}