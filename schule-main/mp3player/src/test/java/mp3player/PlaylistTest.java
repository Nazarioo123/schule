package mp3player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    @Test
    void testSongBasics() {
        Song song = new Song(1, "Bohemian Rhapsody", "Queen", "Opera", 354, "Rock", 1975);

        assertEquals("05:54", song.getFormattedDuration());
        assertTrue(song.matches("Queen"));
    }

    @Test
    void testPlaylistCoreFunctions() {
        Playlist playlist = new Playlist("Test-Playlist");
        Song song1 = new Song(1, "Blinding Lights", "The Weeknd", "After Hours", 200, "Pop", 2019);

        playlist.addSong(song1);
        assertEquals(1, playlist.size());
        assertNotNull(playlist.findById(1));

        assertFalse(playlist.search("Weeknd").isEmpty());
        assertTrue(playlist.search("Mozart").isEmpty());

        assertTrue(playlist.removeSongById(1));
        assertEquals(0, playlist.size());
        assertNull(playlist.findById(1));
    }
}