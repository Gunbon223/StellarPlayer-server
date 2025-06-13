package org.gb.stellarplayer.Model.Enum;

public enum PlaylistType {
    PUBLIC,
    PRIVATE,
    USER,  // User-created playlists
    // Recommendation playlist types (shortened for database compatibility)
    TRENDING,
    NEW_DAILY,
    NEW_RELEASE,
    VIRAL,
    USER_REC,
    GENRE_MIX,
    ARTIST_MIX,
    ARTIST_RADIO,
    DISCOVERY
}
