package com.code.damahe.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.code.damahe.app.MusicEvent
import com.code.damahe.app.PlayerState
import com.code.damahe.res.R
import com.code.damahe.modal.Music
import com.code.damahe.modal.MusicControllerUiState
import com.code.damahe.modal.getImage
import com.code.damahe.modal.getImgArt
import com.code.damahe.util.Util.formatDurationTimeStyle
import com.code.damahe.viewmodel.PlayerViewModel

@Composable
fun AllSongsList(getAllAudio: List<Music>, click: (Int)-> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        items(count = getAllAudio.size) {
            MusicItem(getAllAudio[it]) { click(it) }
        }
    }
}

@Composable
fun MusicItem(music: Music, click: ()-> Unit) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = { click() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgArt = getImgArt(music.path)
            if (getImage(imgArt) != null) {
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    bitmap = getImage(imgArt)!!.asImageBitmap(),
                    contentDescription = "Song cover"
                )
            } else {
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    painter = painterResource(id = R.drawable.music_note_24),
                    contentDescription = "Song cover"
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(1f)
            ) {
                Text(
                    text = music.title,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun PlayerLayout(viewModel: PlayerViewModel) {
    val musicUiState = viewModel.musicControllerUiState

    if (musicUiState.currentSong != null) {
        viewModel.updateMusicState()
        viewModel.updateCurrentPosition(true)
        val playPauseIcon = if (musicUiState.playerState  == PlayerState.PLAYING) Icons.Rounded.Face else Icons.Rounded.PlayArrow
        PlayerScreenContent(musicUiState, playPauseIcon, viewModel::onEvent)
    }
}

@Composable
fun PlayerScreenContent(
    musicUiState: MusicControllerUiState,
    playPauseIcon: ImageVector,
    onEvent: (MusicEvent) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .aspectRatio(1f)

                    ) {
                        val imgArt = getImgArt(musicUiState.currentSong?.path)
                        if (getImage(imgArt) != null) {
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.0f)
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(5.dp)),
                                bitmap = getImage(imgArt)!!.asImageBitmap(),
                                contentDescription = "Song cover"
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.0f)
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(5.dp)),
                                painter = painterResource(id = R.drawable.music_note_24),
                                contentDescription = "Song cover"
                            )
                        }
                    }

                    Text(
                        text = musicUiState.currentSong?.title ?: "UnKnown",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = musicUiState.currentSong?.artist  ?: "UnKnown",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            alpha = 0.60f
                        })

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {

                        Slider(
                            value = musicUiState.currentPosition.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            valueRange = 0f..musicUiState.totalDuration.toFloat(),
                            onValueChange = { onEvent(MusicEvent.SeekSongToPosition(it.toLong())) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatDurationTimeStyle(musicUiState.currentPosition),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                formatDurationTimeStyle(musicUiState.totalDuration),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = "Skip Previous",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = { onEvent(MusicEvent.SkipToPreviousSong) })
                                .padding(12.dp)
                                .size(32.dp)
                        )
                        Icon(
                            imageVector = playPauseIcon,
                            contentDescription = "Play",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = { onEvent(MusicEvent.PauseResumeSong) })
                                .size(64.dp)
                                .padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowRight,
                            contentDescription = "Skip Next",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = { onEvent(MusicEvent.SkipToNextSong) })
                                .padding(12.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
        }
    }
}