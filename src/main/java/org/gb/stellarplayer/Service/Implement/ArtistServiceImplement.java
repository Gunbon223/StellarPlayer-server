package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ArtistServiceImplement implements ArtistService {
    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Override
    public List<Artist> getArtists() {
        List<Artist> artists = artistRepository.findAll();
        artists.sort(Comparator.comparing(Artist::getCreatedAt));
        return artists;
    }

    @Override
    public Artist getArtistById(int id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Artist not found"));
    }

    @Override
    public Artist addArtist(Artist artist) {
        artist.setId(null); // Ensure new entity
        return artistRepository.save(artist);
    }

    @Override
    public Artist updateArtist(Artist artist) {
        if (artist.getId() == null || !artistRepository.existsById(artist.getId())) {
            throw new BadRequestException("Artist not found for update");
        }
        return artistRepository.save(artist);
    }

    @Override
    public void deleteArtist(int id) {
        if (!artistRepository.existsById(id)) {
            throw new BadRequestException("Artist not found for delete");
        }
        artistRepository.deleteById(id);
    }

    @Override
    public Page<Track> getTracksByArtistId(int artistId, Pageable pageable) {
        return trackRepository.findByArtistsIdOrderByCreatedAtDesc(artistId, pageable);
    }

    @Override
    public Page<Album> getAlbumsByArtistId(int artistId, Pageable pageable) {
        return albumRepository.findByArtistsIdOrderByCreatedAtDesc(artistId, pageable);
    }
} 