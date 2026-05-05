package mp3player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Tests für Playlist und Song.
 */
class PlaylistTest {

    private Playlist playlist;
    private Song     song1, song2, song3;

    @BeforeEach
    void setUp() {
        playlist = new Playlist("Test-Playlist");
        song1 = new Song(1, "Bohemian Rhapsody", "Queen",      "Opera",   354, "Rock", 1975);
        song2 = new Song(2, "Blinding Lights",   "The Weeknd", "After Hours", 200, "Pop", 2019);
        song3 = new Song(3, "Hotel California",  "Eagles",     "Hotel",   391, "Rock", 1977);
        playlist.addSong(song1);
        playlist.addSong(song2);
        playlist.addSong(song3);
    }

    // --- Song-Tests ---

    @Test
    void testGetFormattedDuration() {
        assertEquals("05:54", song1.getFormattedDuration()); // 354 s = 5:54
        assertEquals("03:20", song2.getFormattedDuration()); // 200 s = 3:20
    }

    @Test
    void testMatchesTitleCaseInsensitive() {
        assertTrue(song1.matches("bohemian"));
        assertTrue(song1.matches("QUEEN"));
        assertFalse(song1.matches("Weeknd"));
    }

    @Test
    void testMatchesBlankQuery() {
        assertFalse(song1.matches(""));
        assertFalse(song1.matches("  "));
        assertFalse(song1.matches(null));
    }

    @Test
    void testToCsvAndFromCsv() {
        String csv  = song1.toCsv();
        Song   copy = Song.fromCsv(csv);
        assertEquals(song1.getId(),              copy.getId());
        assertEquals(song1.getTitle(),           copy.getTitle());
        assertEquals(song1.getArtist(),          copy.getArtist());
        assertEquals(song1.getDurationSeconds(), copy.getDurationSeconds());
        assertEquals(song1.getYear(),            copy.getYear());
    }

    // --- Playlist-Tests ---

    @Test
    void testSize() {
        assertEquals(3, playlist.size());
    }

    @Test
    void testFindByIdFound() {
        Song found = playlist.findById(2);
        assertNotNull(found);
        assertEquals("Blinding Lights", found.getTitle());
    }

    @Test
    void testFindByIdNotFound() {
        assertNull(playlist.findById(999));
    }

    @Test
    void testSearchReturnsCorrectResults() {
        List<Song> result = playlist.search("Rock"); // matches genre? no – matches title/artist/album
        // "Bohemian Rhapsody" artist=Queen, album=Opera → no rock keyword in those
        // But "Hotel California" album=Hotel → no; let's search by artist
        List<Song> queens = playlist.search("Queen");
        assertEquals(1, queens.size());
        assertEquals("Bohemian Rhapsody", queens.get(0).getTitle());
    }

    @Test
    void testSearchNoResults() {
        List<Song> result = playlist.search("Beethoven");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTotalDurationSeconds() {
        int total = 354 + 200 + 391;
        assertEquals(total, playlist.getTotalDurationSeconds());
    }

    @Test
    void testGetAverageDuration() {
        double avg = (354.0 + 200.0 + 391.0) / 3;
        assertEquals(avg, playlist.getAverageDurationSeconds(), 0.001);
    }

    @Test
    void testGetShortestSong() {
        Song shortest = playlist.getShortestSong();
        assertNotNull(shortest);
        assertEquals(song2.getId(), shortest.getId()); // 200s is shortest
    }

    @Test
    void testGetLongestSong() {
        Song longest = playlist.getLongestSong();
        assertNotNull(longest);
        assertEquals(song3.getId(), longest.getId()); // 391s is longest
    }

    @Test
    void testRemoveSongById() {
        boolean removed = playlist.removeSongById(2);
        assertTrue(removed);
        assertEquals(2, playlist.size());
        assertNull(playlist.findById(2));
    }

    @Test
    void testRemoveNonexistentId() {
        boolean removed = playlist.removeSongById(999);
        assertFalse(removed);
        assertEquals(3, playlist.size());
    }

    @Test
    void testNextId() {
        assertEquals(4, playlist.nextId());
    }

    @Test
    void testEmptyPlaylistStatistics() {
        Playlist empty = new Playlist("Leer");
        assertEquals(0, empty.getTotalDurationSeconds());
        assertEquals(0.0, empty.getAverageDurationSeconds(), 0.001);
        assertNull(empty.getShortestSong());
        assertNull(empty.getLongestSong());
        assertEquals(1, empty.nextId());
    }
}
