package mp3player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class


CsvStorage {

    private static final String SONG_HEADER =
            "id;title;artist;album;durationSeconds;genre;year";

    private static final String PLAYLIST_HEADER =
            "playlistName;songIds";

    private final Path songFilePath;
    private final Path playlistFilePath;

    public CsvStorage(String songFilePath, String playlistFilePath) {
        this.songFilePath = Path.of(songFilePath);
        this.playlistFilePath = Path.of(playlistFilePath);
    }

    public ArrayList<Song> loadSongs() {
        ArrayList<Song> songs = new ArrayList<>();

        File file = songFilePath.toFile();

        if (!file.exists()) {
            return songs;
        }

        try (BufferedReader reader = Files.newBufferedReader(songFilePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                try {
                    songs.add(Song.fromCsv(line));
                } catch (IllegalArgumentException e) {
                    System.err.println("Ungültige Song-Zeile übersprungen: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Songs: " + e.getMessage());
        }

        return songs;
    }

    public boolean saveSongs(List<Song> songs) {
        try {
            createParentDirectoryIfNeeded(songFilePath);

            try (BufferedWriter writer = Files.newBufferedWriter(songFilePath, StandardCharsets.UTF_8)) {
                writer.write(SONG_HEADER);
                writer.newLine();

                for (Song song : songs) {
                    writer.write(song.toCsv());
                    writer.newLine();
                }
            }

            return true;
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Songs: " + e.getMessage());
            return false;
        }
    }

    public Map<String, List<Integer>> loadPlaylists() {
        Map<String, List<Integer>> playlists = new LinkedHashMap<>();
        File file = playlistFilePath.toFile();

        if (!file.exists()) {
            return playlists;
        }

        try (BufferedReader reader = Files.newBufferedReader(playlistFilePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length != 2) {
                    System.err.println("Ungültige Playlist-Zeile übersprungen: " + line);
                    continue;
                }

                String playlistName = parts[0].trim();
                if (playlistName.isEmpty()) {
                    System.err.println("Playlist ohne Namen übersprungen: " + line);
                    continue;
                }

                List<Integer> songIds = playlists.computeIfAbsent(playlistName, key -> new ArrayList<>());
                String idsString = parts[1].trim();

                if (!idsString.isEmpty()) {
                    String[] ids = idsString.split(",");
                    for (String idStr : ids) {
                        try {
                            songIds.add(Integer.parseInt(idStr.trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("Ungültige Song-ID '" + idStr + "' übersprungen.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Playlists: " + e.getMessage());
        }
        return playlists;
    }

    public boolean savePlaylists(Map<String, Playlist> playlists) {
        try {
            createParentDirectoryIfNeeded(playlistFilePath);

            try (BufferedWriter writer = Files.newBufferedWriter(playlistFilePath, StandardCharsets.UTF_8)) {
                writer.write(PLAYLIST_HEADER);
                writer.newLine();

                for (Map.Entry<String, Playlist> entry : playlists.entrySet()) {
                    String playlistName = entry.getKey();
                    Playlist playlist = entry.getValue();

                    StringJoiner ids = new StringJoiner(",");
                    for (Song song : playlist.getSongs()) {
                        ids.add(String.valueOf(song.getId()));
                    }

                    writer.write(safe(playlistName) + ";" + ids.toString());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Playlists: " + e.getMessage());
            return false;
        }
    }

    public String getSongFilePath() {
        return songFilePath.toString();
    }

    public String getPlaylistFilePath() {
        return playlistFilePath.toString();
    }

    private static void createParentDirectoryIfNeeded(Path filePath) throws IOException {
        Path parent = filePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.replace(";", ",").trim();
    }
}