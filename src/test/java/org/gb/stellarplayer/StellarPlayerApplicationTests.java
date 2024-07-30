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
        for (int i = 0; i < 10; i++) {
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
        int artistRandom = random.nextInt(artistRepository.findAll().size());
        Track track = Track.builder()
                .title(title)
                .duration(random.nextInt(300))
                .cover("https://placehold.co/600x400/"+color+ "/FFF" + "?text=" + String.valueOf(title.charAt(0)).toUpperCase())
                .lyrics(faker.lorem().paragraph(5))
                .status(faker.bool().bool())
                .artists(artistRepository.findAll().subList(artistRandom,artistRandom+1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .path("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                .album(albumRepository.findAll().get(random.nextInt(albumRepository.findAll().size())))
                .build();
        trackRepository.save(track);
        //TODO add track to album

    }
    }

    @Test
    void addTrackToAblum() {

    }

    @Test
    void createPlaylist() {
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String color = RandomColor.getRandomColor();
            String name ="Best of " +  faker.music().instrument();
            Playlist playlist = Playlist.builder()
                    .name(name)
                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
                    .status(true)
                    .type(PlaylistType.PUBLIC)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tracks(trackRepository.findAll().subList(0, random.nextInt(30) ))
                    .build();
            playlistRepository.save(playlist);
        }
    }

    @Test
    void createAlbum() {
        Faker faker = new Faker(new Locale("en-US"));
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String color = RandomColor.getRandomColor();
            String name = "Best of " + faker.music().genre();
            Album album = Album.builder()
                    .title(name)
                    .cover("https://placehold.co/600x400/" + color + "/FFF" + "?text=" + String.valueOf(name.charAt(0)).toUpperCase())
                    .status(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            albumRepository.save(album);
        }
    }



}
