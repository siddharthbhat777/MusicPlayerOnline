package com.siddharth_and_rashmi_music_player.music_player.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.siddharth_and_rashmi_music_player.music_player.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}