package org.gb.stellarplayer;

import com.github.javafaker.Faker;
import com.github.javafaker.HowIMetYourMother;
import com.github.javafaker.Music;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Ultils.RandomColor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SpringBootTest
class StellarPlayerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    FavoriteRepository favoriteRepository;
    @Autowired
    GenreRepository genreRepository;
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    VoucherRepository voucherRepository;
    @Autowired
    UserSubscriptionRepository userSubscriptionRepository;
    @Autowired
    UserVoucherRepository userVoucherRepository;
    @Autowired
    TrackRepository trackRepository;


    @Test
    void createArtist() {
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        String color = RandomColor.getRandomColor();
        for (int i = 0; i < 20; i++) {
            String name = faker.artist().name();
            Artist artist = Artist.builder()
                    .name(name)
                    .avatar("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
                    .bio(faker.lorem().paragraph(3))
                    .active(faker.bool().bool())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            artistRepository.save(artist);
        }

    }

    @Test
    void testTracks(){
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        String color = RandomColor.getRandomColor();
        for (int i = 0; i < 50; i++) {
            String title = faker.howIMetYourMother().catchPhrase() + " " + faker.music().genre() + " " + faker.music().instrument();
            List<Artist> allArtists = artistRepository.findAll();
            int numberOfArtists = random.nextInt(3) + 1;
            Collections.shuffle(allArtists);
            Track track = Track.builder()
                    .title(title)
                    .duration(random.nextInt(300))
                    .cover("https://placehold.co/600x400/"+color+ "/FFF" + "?text=" + String.valueOf(title.charAt(0)).toUpperCase())
                    .lyrics(faker.lorem().paragraph(5))
                    .status(faker.bool().bool())
                    .artists(allArtists.subList(0, numberOfArtists))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .album(albumRepository.findAll().get(random.nextInt(albumRepository.findAll().size())))
                    .path("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                    .build();
            trackRepository.save(track);

        }
    }

    @Test
    void createPlaylist() {
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String color = RandomColor.getRandomColor();
            String name = faker.music().genre() + " " + faker.music().instrument();
            List<Track> allTracks = trackRepository.findAll();
            int numberOfTracks = random.nextInt(10) + 1;
            Collections.shuffle(allTracks);
            Playlist playlist = Playlist.builder()
                    .name(name)
                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
                    .status(true)
                    .type(PlaylistType.PUBLIC)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tracks(allTracks.subList(0, numberOfTracks))
                    .build();
            playlistRepository.save(playlist);
        }
    }

    @Test
    void fakerTest() {
        Faker faker = new Faker(new Locale("en-US"));
        System.out.println(faker.artist().name());
        System.out.println(faker.music().genre());
        System.out.println(faker.music().instrument());
        System.out.println(faker.howIMetYourMother().catchPhrase());
        System.out.println(faker.lorem().paragraph(3));
        System.out.println();
    }

    @Test
    void createAlbum() {
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String color = RandomColor.getRandomColor();
            String name = faker.dune().character() +" " + faker.music().instrument();
            List<Artist> allArtists = artistRepository.findAll();
            int numberOfArtists = random.nextInt(3) + 1;
            Collections.shuffle(allArtists);
            Album album = Album.builder()
                    .title(name)
                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
                    .status(true)
                    .artists(allArtists.subList(0, numberOfArtists))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            albumRepository.save(album);
        }
    }


}
