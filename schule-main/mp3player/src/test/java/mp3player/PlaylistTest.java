package mp3player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlaylistTest {

    private Playlist playlist;
    private Song song1;
    private Song song2;

    @BeforeEach
    public void setUp() {
        playlist = new Playlist("Test Playlist");
        song1 = new Song(1, "Test Song 1", "Artist 1", "Album 1", 120, "Pop", 2020);
        song2 = new Song(2, "Test Song 2", "Artist 2", "Album 2", 180, "Rock", 2021);
    }

    @Test
    public void testAddSong() {
        assertTrue(playlist.addSong(song1), "Der Song sollte erfolgreich hinzugefügt werden.");
        assertEquals(1, playlist.size(), "Die Playlist-Größe sollte 1 sein.");

        assertFalse(playlist.addSong(song1), "Duplikate sollten nicht hinzugefügt werden dürfen.");
        assertEquals(1, playlist.size(), "Die Playlist-Größe sollte sich bei einem Duplikat nicht ändern.");
    }

    @Test
    public void testRemoveSongById() {
        playlist.addSong(song1);
        playlist.addSong(song2);

        assertTrue(playlist.removeSongById(1), "Der Song mit ID 1 sollte gelöscht werden.");
        assertEquals(1, playlist.size(), "Es sollte nur noch ein Song in der Playlist sein.");
        assertNull(playlist.findById(1), "Der Song mit ID 1 sollte nicht mehr auffindbar sein.");
    }

    @Test
    public void testGetTotalDurationSeconds() {
        playlist.addSong(song1);
        playlist.addSong(song2);

        assertEquals(300, playlist.getTotalDurationSeconds(), "Die Gesamtdauer sollte exakt 300 Sekunden betragen.");
    }

    @Test
    public void testSearch() {
        playlist.addSong(song1);
        playlist.addSong(song2);

        var results = playlist.search("Rock");
        assertEquals(1, results.size(), "Es sollte genau 1 Song für den Suchbegriff 'Rock' gefunden werden.");
        assertEquals(song2, results.get(0), "Der gefundene Song sollte 'song2' sein.");
    }
}