package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Repository.GenreRepository;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private TrackRepository trackRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private ArtistRepository artistRepository;
    
    public SearchResultDTO search(String query, int limit) {
        // Get search results
        List<Track> tracks = trackRepository.findByTitleContainingIgnoreCase(query);
        List<Album> albums = albumRepository.findByTitleContainingIgnoreCase(query);
        List<Genre> genres = genreRepository.findByNameContainingIgnoreCase(query);
        List<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(query);
        List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(query);
        
        // Apply limit
        if (limit > 0) {
            tracks = tracks.stream().limit(limit).collect(Collectors.toList());
            albums = albums.stream().limit(limit).collect(Collectors.toList());
            genres = genres.stream().limit(limit).collect(Collectors.toList());
            playlists = playlists.stream().limit(limit).collect(Collectors.toList());
            artists = artists.stream().limit(limit).collect(Collectors.toList());
        }
        
        // Convert to DTOs
        List<TrackSearchDTO> trackDTOs = convertToTrackDTOs(tracks);
        List<AlbumSearchDTO> albumDTOs = convertToAlbumDTOs(albums);
        List<GenreSearchDTO> genreDTOs = convertToGenreDTOs(genres);
        List<PlaylistSearchDTO> playlistDTOs = convertToPlaylistDTOs(playlists);
        List<ArtistSearchDTO> artistDTOs = convertToArtistDTOs(artists);
        
        return SearchResultDTO.builder()
            .tracks(trackDTOs)
            .albums(albumDTOs)
            .genres(genreDTOs)
            .playlists(playlistDTOs)
            .artists(artistDTOs)
            .build();
    }
    
    private List<TrackSearchDTO> convertToTrackDTOs(List<Track> tracks) {
        return tracks.stream()
            .map(track -> TrackSearchDTO.builder()
                .id(track.getId())
                .title(track.getTitle())
                .duration(track.getDuration())
                .cover(track.getCover())
                .albumTitle(track.getAlbum() != null ? track.getAlbum().getTitle() : null)
                .artistNames(track.getArtists() != null ? 
                    track.getArtists().stream()
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList()) : 
                    List.of())
                .build())
            .collect(Collectors.toList());
    }
    
    private List<AlbumSearchDTO> convertToAlbumDTOs(List<Album> albums) {
        return albums.stream()
            .<AlbumSearchDTO>map(album -> AlbumSearchDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .cover(album.getCover())
                .artistNames(album.getArtists() != null ?
                    album.getArtists().stream()
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList()) :
                    List.of())
                .build())
            .collect(Collectors.toList());
    }
    
    private List<GenreSearchDTO> convertToGenreDTOs(List<Genre> genres) {
        return genres.stream()
            .map(genre -> GenreSearchDTO.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build())
            .collect(Collectors.toList());
    }
    
    private List<PlaylistSearchDTO> convertToPlaylistDTOs(List<Playlist> playlists) {
        return playlists.stream()
            .<PlaylistSearchDTO>map(playlist -> PlaylistSearchDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .cover(playlist.getCover())
                .trackCount(playlist.getTracks() != null ? playlist.getTracks().size() : 0)
                .build())
            .collect(Collectors.toList());
    }
    
    private List<ArtistSearchDTO> convertToArtistDTOs(List<Artist> artists) {
        return artists.stream()
            .<ArtistSearchDTO>map(artist -> ArtistSearchDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .avatar(artist.getAvatar())
                .bio(artist.getBio())
                    .active(artist.getActive())
                .build())
            .collect(Collectors.toList());
    }
}
