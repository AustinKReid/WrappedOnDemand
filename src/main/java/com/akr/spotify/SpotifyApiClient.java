package com.akr.spotify;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;

public class SpotifyApiClient {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final OkHttpClient httpClient;
    private final Gson gson;

    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_URL = "https://api.spotify.com/v1";

    public SpotifyApiClient(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    public String getAuthorizationUrl() {
        return AUTH_URL + "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=user-top-read" +
                "&show_dialog=true";
    }

    public String getAccessToken(String authorizationCode) throws IOException {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", authorizationCode)
                .add("redirect_uri", redirectUri)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            TokenResponse tokenResponse = gson.fromJson(response.body().string(), TokenResponse.class);
            return tokenResponse.access_token;
        }
    }

    public List<Track> getTopTracks(String accessToken) throws IOException {
        String url = API_URL + "/me/top/tracks?time_range=medium_term&limit=50";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error " + response);

            TopTracksResponse tracksResponse = gson.fromJson(
                    response.body().string(),
                    TopTracksResponse.class
            );
            return tracksResponse.items;
        }
    }

    //https://developer.spotify.com/documentation/web-api/concepts/api-calls
    public Integer getSongReleaseYear(Track track) { //hand

        String date = track.album.release_date;
        date = date.substring(0,4);

        return Integer.parseInt(date);

    }

    // Response classes
    private static class TokenResponse {
        String access_token;
        String token_type;
        int expires_in;
        String refresh_token;
        String scope;
    }

    public static class TopTracksResponse {
        List<Track> items;
        int total;
        int limit;
        int offset;
        String href;
        String next;
        String previous;
    }

    public static class Track {
        public String id;
        public String name;
        public List<Artist> artists;
        public Album album;
        public int duration_ms;
        public int popularity;
        public String uri;

        @Override
        public String toString() {
            return name + " by " + artists.get(0).name;
        }
    }

    public static class Artist {
        String id;
        String name;
        String uri;
    }

    public static class Album {
        String id;
        String name;
        List<Image> images;
        String uri;
        @SerializedName("release_date") //hand
        String release_date;
    }

    public static class Image {
        String url;
        Integer height;
        Integer width;
    }
}