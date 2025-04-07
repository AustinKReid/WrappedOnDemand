package com.akr.spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Replace these with your Spotify app credentials
        String clientId = "acdc61d55c7e4fef9a4149183888a647";
        String clientSecret = "2efd9c410d5a4fdc9f3e555921a03f55";
        String redirectUrl = "http://localhost:8080/callback";

        SpotifyApiClient spotifyClient = new SpotifyApiClient(clientId, clientSecret, redirectUrl);

        System.out.println("Please authorize this application by visiting the following URL:");
        System.out.println(spotifyClient.getAuthorizationUrl());

        System.out.println("\nAfter authorization, you'll be redirected to a URL. Paste that URL here:");
        String callbackUrl = readInput();

        try {
            String code = extractAuthorizationCode(callbackUrl);
            System.out.println("\nExchanging authorization code for access token...");

            String accessToken = spotifyClient.getAccessToken(code);
            System.out.println("Successfully obtained access token!");

            System.out.println("\nFetching your top tracks...");
            List<SpotifyApiClient.Track> topTracks = spotifyClient.getTopTracks(accessToken);

            System.out.println("\nYour Top Tracks:");
            for (int i = 0; i < topTracks.size(); i++) {
                System.out.println((i + 1) + ". " + topTracks.get(i));
            }

        } catch (IOException | URISyntaxException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String extractAuthorizationCode(String callbackUrl) throws URISyntaxException {
        String query = new URI(callbackUrl).getQuery();
        return query.split("code=")[1].split("&")[0];
    }

    private static String readInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input", e);
        }
    }
}