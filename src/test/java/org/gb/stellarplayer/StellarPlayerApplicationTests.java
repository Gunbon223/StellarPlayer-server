//package org.gb.stellarplayer;
//
//import com.github.javafaker.Faker;
//import com.github.javafaker.HowIMetYourMother;
//import com.github.javafaker.Music;
//import org.gb.stellarplayer.Entites.*;
//import org.gb.stellarplayer.Model.Enum.PlaylistType;
//import org.gb.stellarplayer.Repository.*;
//import org.gb.stellarplayer.Ultils.RandomColor;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest
//class StellarPlayerApplicationTests {
//
//    @Test
//    void contextLoads() {
//    }
//
//    @Autowired
//    AlbumRepository albumRepository;
//    @Autowired
//    ArtistRepository artistRepository;
//
//    @Autowired
//    GenreRepository genreRepository;
//    @Autowired
//    HistoryRepository historyRepository;
//    @Autowired
//    PlaylistRepository playlistRepository;
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    OrderRepository orderRepository;
//    @Autowired
//    VoucherRepository voucherRepository;
//    @Autowired
//    UserSubscriptionRepository userSubscriptionRepository;
//    @Autowired
//    UserVoucherRepository userVoucherRepository;
//    @Autowired
//    TrackRepository trackRepository;
//
//    @Test
//    void createArtist() {
//        Faker faker = new Faker(new Locale("en-US"));
//        Random random = new Random();
//        String color = RandomColor.getRandomColor();
//        for (int i = 0; i < 20; i++) {
//            String name = faker.artist().name() +" "+ faker.leagueOfLegends().champion();
//            Artist artist = Artist.builder()
//                    .name(name)
//                    .avatar("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
//                    .bio(faker.lorem().paragraph(3))
//                    .active(faker.bool().bool())
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//            artistRepository.save(artist);
//        }
//    }
//
//    @Test
//    void createAlbum() {
//        Faker faker = new Faker(new Locale("en-US"));
//        Random random = new Random();
//        for (int i = 0; i < 10; i++) {
//            String color = RandomColor.getRandomColor();
//            String name = faker.leagueOfLegends().location() +" " + faker.music().instrument() ;
//            List<Artist> allArtists = artistRepository.findAll();
//            int numberOfArtists = random.nextInt(3) + 1;
//            Collections.shuffle(allArtists);
//            Album album = Album.builder()
//                    .title(name)
//                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
//                    .status(true)
//                    .releaseDate(LocalDate.from(LocalDateTime.now().minusDays(random.nextInt(365 * 20))))
//                    .artists(allArtists.subList(0, numberOfArtists))
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//            albumRepository.save(album);
//        }
//    }
//
//    @Test
//    void createGenres() {
//        Faker faker = new Faker(new Locale("en-US"));
//        String[] commonGenres = {"Rock", "Pop", "Hip Hop", "R&B", "Jazz", "Blues", "Electronic",
//                                "Classical", "Country", "Reggae", "Metal", "Folk", "Indie"};
//
//        for (String genreName : commonGenres) {
//            Genre genre = Genre.builder()
//                    .name(genreName)
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//            genreRepository.save(genre);
//        }
//
//        // Add some random genres with more unique names
//        for (int i = 0; i < 7; i++) {
//            String name = faker.music().genre();
//            Genre genre = Genre.builder()
//                    .name(name)
//                    .description("Explore the unique sounds and rhythms of " + name.toLowerCase() + " music.")
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//            genreRepository.save(genre);
//        }
//    }
//
//    @Test
//    void testTracks(){
//        Faker faker = new Faker(new Locale("en-US"));
//        Random random = new Random();
//        String color = RandomColor.getRandomColor();
//
//        // Get all genres
//        List<Genre> allGenres = genreRepository.findAll();
//
//        // If no genres exist yet, create them
//        if (allGenres.isEmpty()) {
//            createGenres();
//            allGenres = genreRepository.findAll();
//        }
//
//        for (int i = 0; i < 90; i++) {
//            String title = faker.hipster().word() + " " + faker.music().genre() + " " + faker.lorem().word();
//            List<Artist> allArtists = artistRepository.findAll();
//            int numberOfArtists = random.nextInt(3) + 1;
//            Collections.shuffle(allArtists);
//
//            // Randomly select genres
//            int numberOfGenres = random.nextInt(3) + 1; // 1-3 genres per track
//            Collections.shuffle(allGenres);
//            List<Genre> selectedGenres = allGenres.subList(0, Math.min(numberOfGenres, allGenres.size()));
//
//            // Generate English lyrics
//            StringBuilder lyrics = new StringBuilder();
//            String[] englishWords = {
//                "The night is young", "Music fills the air", "Dancing in the moonlight",
//                "Lost in the rhythm", "Feel the beat", "Singing our song",
//                "Memories fade away", "Love is in the air", "Time stands still",
//                "Dreams come alive", "Heart beats faster", "Soul takes flight",
//                "Shining through the rain", "Breaking free", "Rising above",
//                "Echoes of the past", "New horizons", "Shining bright",
//                "Falling in love", "Heartfelt melody", "Soulful song",
//                "Lost in the moment", "Dancing through the night", "Memories of you",
//                "Whispers in the wind", "Sunset over the sea", "Waking up to you",
//                "Lost in the stars", "Dancing in the moonlight", "Memories of you",
//                "Whispers in the wind", "Sunset over the sea", "Waking up to you",
//                "Lost in the stars", "Dancing in the moonlight", "Memories of you",
//                "Whispers in the wind", "Sunset over the sea", "Waking up to you",
//                "Lost in the stars", "Dancing in the moonlight", "Memories of you",
//                "Whispers in the wind", "Sunset over the sea", "Waking up to you",
//                "Lost in the stars", "Dancing in the moonlight", "Memories of you",
//                "Dancing in the moonlight", "Watching the stars", "Lost in the moment",
//                "Whispers in the wind", "Sunset over the sea", "Waking up to you",
//                "Lost in the stars", "Dancing in the moonlight", "Memories of you",
//
//
//            };
//            for (int j = 0; j < 10; j++) {
//                lyrics.append(englishWords[random.nextInt(englishWords.length)]).append("\n");
//            }
//
//            Track track = Track.builder()
//                    .title(title)
//                    .duration(random.nextInt(300))
//                    .cover("https://placehold.co/600x400/"+color+ "/FFF" + "?text=" + String.valueOf(title.charAt(0)).toUpperCase())
//                    .lyrics(lyrics.toString())
//                    .status(faker.bool().bool())
//                    .artists(allArtists.subList(0, numberOfArtists))
//                    .genres(selectedGenres)
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .album(albumRepository.findAll().get(random.nextInt(albumRepository.findAll().size())))
//                    .path("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
//                    .build();
//            trackRepository.save(track);
//        }
//    }
//
//    @Test
//    void createPlaylist() {
//        Faker faker = new Faker(new Locale("en-US"));
//        Random random = new Random();
//        for (int i = 0; i < 10; i++) {
//            String color = RandomColor.getRandomColor();
//            String name = faker.book().title() + " " + (faker.app().name());
//            List<Track> allTracks = trackRepository.findAll();
//            int numberOfTracks = random.nextInt(10) + 1;
//            Collections.shuffle(allTracks);
//            Playlist playlist = Playlist.builder()
//                    .name(name)
//                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
//                    .status(true)
//                    .type(PlaylistType.PUBLIC)
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .tracks(allTracks.subList(0, numberOfTracks))
//                    .build();
//            playlistRepository.save(playlist);
//        }
//    }
//
//    List<String> paths = new ArrayList<>();
//
//
//    @Test
//    void fakerTest() {
//
//        Faker faker = new Faker(new Locale("en-US"));
//        System.out.println(faker.artist().name());
//        System.out.println(faker.music().genre());
//        System.out.println(faker.music().instrument());
//        System.out.println(faker.howIMetYourMother().catchPhrase());
//        System.out.println(faker.lorem().paragraph(3));
//        System.out.println();
//    }
//
//    @Test
//    void runAllTests() {
//        // Create basic data first
//        createGenres();
//        createArtist();
//        createAlbum();
//
//        // Create tracks with all relationships
//        testTracks();
//
//        // Create playlists with tracks
//        createPlaylist();
//
//        System.out.println("All test data has been generated successfully!");
//    }
//}
