package com.akr.spotify;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import io.github.cdimascio.dotenv.Dotenv;

import static spark.Spark.*;

public class Main {
    private static final int WEB_PORT = 3000;
    private static final int PORT = 8080;
    private static String authorizationCode;
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static volatile String top50List = null;

    public static void main(String[] args) {
        port(WEB_PORT);
        staticFiles.location("/public");

        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .load();

        String clientId = dotenv.get("clientId");
        String clientSecret = dotenv.get("clientSecret");
        String redirectUrl = "http://localhost:" + PORT + "/callback";

        get("/auth-url", (req, res) -> {
            res.type("application/json");
            return "{ \"url\": \"" + redirectUrl + "\" }";
        });

        try {
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

            int years = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < topTracks.size(); i++) {
                String line = (i + 1) + ". " + topTracks.get(i) + " Released In " + spotifyClient.getSongReleaseYear(topTracks.get(i));
                System.out.println(line);
                sb.append(line).append("\n");
                years += spotifyClient.getSongReleaseYear(topTracks.get(i));
            }

            System.out.println("Average Year of Song: " + (years / 50));
            List<SpotifyApiClient.Artist> topArtists = spotifyClient.getTopArtists(accessToken);
            System.out.println(topArtists);

            top50List = sb.toString();
            top50List+=("\nYour top 5 favorite artists are: "+topArtists+"\nYour average listening year is: "+ (years / 50));

            get("/", (req, res) -> {
                res.type("text/html");
                return Files.readString(Paths.get("src/main/resources/public/index.html"));
            });

            get("/data", (req, res) -> {
                res.type("application/json");

                int waited = 0;
                while (top50List == null && waited < 10000) {
                    Thread.sleep(100);
                    waited += 100;
                }

                if (top50List == null) {
                    return "{ \"error\": \"Data not ready\" }";
                }

                String escaped = top50List.replace("\n", "\\n").replace("\"", "\\\"");
                return "{ \"top50\": \"" + escaped + "\" }";
            });

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
