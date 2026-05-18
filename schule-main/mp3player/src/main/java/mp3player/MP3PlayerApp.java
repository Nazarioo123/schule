package mp3player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MP3PlayerApp {

    private static final String SONG_FILE = "songs.csv";
    private static final String PLAYLIST_FILE = "playlist.csv";

    private static CsvStorage storage;
    private static Scanner scanner;

    private static final Map<String, Playlist> playlists = new LinkedHashMap<>();
    private static final List<Song> allSongs = new ArrayList<>();

    private static Playlist currentPlaylist;

    public static void main(String[] args) {
        storage = new CsvStorage(SONG_FILE, PLAYLIST_FILE);
        scanner = new Scanner(System.in);

        loadData();

        if (playlists.isEmpty()) {
            System.out.println("Keine Playlists gefunden. Standard-Playlist wird erstellt.");
            currentPlaylist = createPlaylist("Default");
            addSampleData(currentPlaylist);
        }

        runMainMenu();

        saveData();

        System.out.println("Gespeichert. Bye!");
    }

    private static void loadData() {
        allSongs.clear();
        allSongs.addAll(storage.loadSongs());

        Map<Integer, Song> songById = new LinkedHashMap<>();

        for (Song song : allSongs) {
            songById.put(song.getId(), song);
        }

        Map<String, List<Integer>> loadedPlaylists = storage.loadPlaylists();

        playlists.clear();

        for (Map.Entry<String, List<Integer>> entry : loadedPlaylists.entrySet()) {
            Playlist playlist = new Playlist(entry.getKey());

            for (Integer songId : entry.getValue()) {
                Song song = songById.get(songId);

                if (song != null) {
                    playlist.addSong(song);
                }
            }

            playlists.put(playlist.getName(), playlist);
        }
    }

    private static void saveData() {
        storage.saveSongs(allSongs);
        storage.savePlaylists(playlists);
    }

    private static void runMainMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== PLAYLIST AUSWAHL =====");
            printPlaylists();

            System.out.println("n = neue Playlist erstellen");
            System.out.println("0 = Beenden");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "0" -> running = false;
                case "n" -> createPlaylistInteractive();
                default -> openPlaylist(input);
            }
        }
    }

    private static void openPlaylist(String name) {
        currentPlaylist = playlists.get(name);

        if (currentPlaylist == null) {
            System.out.println("Playlist nicht gefunden.");
            return;
        }

        playlistMenu();
    }

    private static void playlistMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Playlist: " + currentPlaylist.getName() + " ===");
            System.out.println("1 = Songs anzeigen");
            System.out.println("2 = Song hinzufügen");
            System.out.println("3 = Song löschen");
            System.out.println("4 = Song suchen");
            System.out.println("5 = Statistik anzeigen");
            System.out.println("0 = Zurück");
            System.out.print("Auswahl: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showSongs();
                case "2" -> addSongToPlaylist();
                case "3" -> removeSongFromPlaylist();
                case "4" -> searchSongs();
                case "5" -> showStatistics();
                case "0" -> running = false;
                default -> System.out.println("Ungültige Auswahl.");
            }
        }
    }

    private static void printPlaylists() {
        if (playlists.isEmpty()) {
            System.out.println("Keine Playlists vorhanden.");
            return;
        }

        for (String name : playlists.keySet()) {
            System.out.println("- " + name);
        }
    }

    private static void createPlaylistInteractive() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Der Playlist-Name darf nicht leer sein.");
            return;
        }

        if (playlists.containsKey(name)) {
            System.out.println("Diese Playlist existiert bereits.");
            return;
        }

        createPlaylist(name);
    }

    private static Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name);
        playlists.put(playlist.getName(), playlist);
        System.out.println("Playlist erstellt: " + playlist.getName());
        return playlist;
    }

    private static void showSongs() {
        if (currentPlaylist == null) {
            System.out.println("Keine Playlist ausgewählt.");
            return;
        }

        if (currentPlaylist.isEmpty()) {
            System.out.println("Diese Playlist enthält keine Songs.");
            return;
        }

        for (Song song : currentPlaylist.getSongs()) {
            System.out.println(song);
        }
    }

    private static void addSongToPlaylist() {
        if (currentPlaylist == null) {
            System.out.println("Keine Playlist ausgewählt.");
            return;
        }

        System.out.print("Titel: ");
        String title = scanner.nextLine().trim();

        System.out.print("Künstler: ");
        String artist = scanner.nextLine().trim();

        System.out.print("Album: ");
        String album = scanner.nextLine().trim();

        int durationSeconds = readInt("Dauer in Sekunden: ", 0, Integer.MAX_VALUE);

        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();

        int year = readInt("Jahr: ", 0, 9999);

        int id = getNextGlobalSongId();

        Song song = new Song(id, title, artist, album, durationSeconds, genre, year);

        allSongs.add(song);
        currentPlaylist.addSong(song);

        System.out.println("Song hinzugefügt.");
    }

    private static void removeSongFromPlaylist() {
        if (currentPlaylist == null) {
            System.out.println("Keine Playlist ausgewählt.");
            return;
        }

        int id = readInt("Song-ID: ", 1, Integer.MAX_VALUE);

        boolean removed = currentPlaylist.removeSongById(id);

        if (removed) {
            System.out.println("Song aus Playlist entfernt.");
        } else {
            System.out.println("Song-ID wurde in dieser Playlist nicht gefunden.");
        }
    }

    private static void searchSongs() {
        if (currentPlaylist == null) {
            System.out.println("Keine Playlist ausgewählt.");
            return;
        }

        System.out.print("Suchbegriff: ");
        String query = scanner.nextLine().trim();

        List<Song> result = currentPlaylist.search(query);

        if (result.isEmpty()) {
            System.out.println("Keine Treffer gefunden.");
            return;
        }

        for (Song song : result) {
            System.out.println(song);
        }
    }

    private static void showStatistics() {
        if (currentPlaylist == null) {
            System.out.println("Keine Playlist ausgewählt.");
            return;
        }

        System.out.println("Anzahl Songs: " + currentPlaylist.size());
        System.out.println("Gesamtdauer: " + currentPlaylist.getTotalDurationFormatted());
        System.out.printf("Durchschnittliche Dauer: %.2f Sekunden%n", currentPlaylist.getAverageDurationSeconds());

        Song shortest = currentPlaylist.getShortestSong();
        Song longest = currentPlaylist.getLongestSong();

        System.out.println("Kürzester Song: " + (shortest == null ? "-" : shortest));
        System.out.println("Längster Song: " + (longest == null ? "-" : longest));
    }

    private static int getNextGlobalSongId() {
        int max = 0;

        for (Song song : allSongs) {
            if (song.getId() > max) {
                max = song.getId();
            }
        }

        return max + 1;
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println("Bitte eine Zahl zwischen " + min + " und " + max + " eingeben.");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ungültige Zahl.");
            }
        }
    }

    private static void addSampleData(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist darf nicht null sein.");
        }

        Song s1 = new Song(1, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock", 1975);
        Song s2 = new Song(2, "Blinding Lights", "The Weeknd", "After Hours", 200, "Pop", 2019);

        if (findGlobalSongById(s1.getId()) == null) {
            allSongs.add(s1);
        }

        if (findGlobalSongById(s2.getId()) == null) {
            allSongs.add(s2);
        }

        playlist.addSong(s1);
        playlist.addSong(s2);
    }

    private static Song findGlobalSongById(int id) {
        for (Song song : allSongs) {
            if (song.getId() == id) {
                return song;
            }
        }

        return null;
    }
}