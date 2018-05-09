package com.ironbear775.musichunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ironbear775 on 2017/12/30.
 */

public class MusicDetailActivity extends BaseActivity {
    private Music mMusic;
    private static String URL = "https://www.tikitiki.cn/downloadurl.do?quality=%1$s&id=%2$s&type=1";
    private byte[] albumArtBytes;
    private MediaPlayer mediaPlayer;
    private boolean canDownload = false;
    private Handler handler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private File dir;
    private ImageView play;
    private AnimatedVectorDrawable playToPauseDrawable, pauseToPlayDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_detail_layout);

        mMusic = (Music) getIntent().getSerializableExtra("Music");

        TextView title = findViewById(R.id.tv_song_title);
        TextView detail = findViewById(R.id.tv_song_detail);
        TextView mp3Size = findViewById(R.id.mp3_size);
        TextView oggSize = findViewById(R.id.ogg_size);
        final ImageView albumArt = findViewById(R.id.iv_album_art);
        play = findViewById(R.id.play_button);
        FloatingActionButton oggDownload = findViewById(R.id.ogg_download);
        FloatingActionButton mp3Download = findViewById(R.id.mp3_download);
        FloatingActionButton mp3LowDownload = findViewById(R.id.mp3_low_download);
        FloatingActionButton albumArtDownload = findViewById(R.id.album_art_download);
        final AppCompatSeekBar seekBar = findViewById(R.id.seek_bar);

        playToPauseDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.play_to_pause_anim);

        pauseToPlayDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.pause_to_play_anim);
        seekBar.setEnabled(false);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.sendEmptyMessageDelayed(1, 500);
                return false;
            }
        });
        String songTitle = getResources().getString(R.string.song_title) + mMusic.getTitle();

        title.setText(songTitle);

        String artistText = getResources().getString(R.string.song_artist) + mMusic.getArtist() +
                "   " + getResources().getString(R.string.song_album) + mMusic.getAlbum();
        detail.setText(artistText);


        String size1 = getResources().getString(R.string.mp3_size)
                + mMusic.getMp3Size() + "MB" + "   " + getResources().getString(R.string.mp3_low_size)
                + mMusic.getMp3LowSize() + "MB";
        mp3Size.setText(size1);

        String size2 = getResources().getString(R.string.ogg_size)
                + mMusic.getOggSize() + "MB";
        oggSize.setText(size2);

        Glide.with(this)
                .asBitmap()
                .load(mMusic.getAlbumArtUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        albumArtBytes = stream.toByteArray();
                        albumArt.setImageBitmap(resource);
                        canDownload = true;
                    }
                });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
                seekBar.setEnabled(true);
                seekBar.setMax(mediaPlayer.getDuration());
                executorService.execute(runnable);
            }
        });

        oggDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDownload) {
                    if (mMusic.getOggSize().equals("0")) {
                        Toast.makeText(getApplicationContext(), R.string.no_recourse,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] strings = getFileName();
                        String songName = strings[1] + " - " + strings[0] + ".ogg";
                        Intent intent = new Intent("Download Music");
                        intent.putExtra("Song Name", songName);
                        intent.putExtra("Image Data", albumArtBytes);

                        try {
                            String url = String.format(URL,
                                    URLEncoder.encode("sogg", "UTF-8"), mMusic.getUrl());
                            intent.putExtra("Url", url);
                            sendBroadcast(intent);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(MusicDetailActivity.this, R.string.wait_image_ready,
                            Toast.LENGTH_SHORT)
                            .show();
            }
        });

        mp3Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDownload) {
                    if (mMusic.getMp3Size().equals("0")) {
                        Toast.makeText(getApplicationContext(), R.string.no_recourse,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] strings = getFileName();
                        String songName = strings[1] + " - " + strings[0] + ".mp3";
                        Intent intent = new Intent("Download Music");
                        intent.putExtra("Song Name", songName);
                        intent.putExtra("Image Data", albumArtBytes);

                        try {
                            String url = String.format(URL,
                                    URLEncoder.encode("s320", "UTF-8"), mMusic.getUrl());
                            intent.putExtra("Url", url);
                            sendBroadcast(intent);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(MusicDetailActivity.this, R.string.wait_image_ready,
                            Toast.LENGTH_SHORT)
                            .show();
            }
        });

        mp3LowDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDownload) {
                    if (mMusic.getMp3LowSize().equals("0")) {
                        Toast.makeText(getApplicationContext(), R.string.no_recourse,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] strings = getFileName();
                        String songName = strings[1] + " - " + strings[0] + ".mp3";
                        Intent intent = new Intent("Download Music");
                        intent.putExtra("Song Name", songName);
                        intent.putExtra("Image Data", albumArtBytes);
                        try {
                            String url = String.format(URL,
                                    URLEncoder.encode("s128", "UTF-8"), mMusic.getUrl());
                            intent.putExtra("Url", url);
                            sendBroadcast(intent);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(MusicDetailActivity.this, R.string.wait_image_ready,
                            Toast.LENGTH_SHORT)
                            .show();
            }
        });

        albumArtDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDownload) {
                    downloadAlbumArt();
                } else {
                    Toast.makeText(MusicDetailActivity.this, R.string.wait_image_ready, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }


    /**
     * 播放在线预览歌曲
     */
    private void playMusic() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                String url = null;
                if (!mMusic.getMp3LowSize().equals("0")) {
                    url = String.format(URL, URLEncoder.encode("s128", "UTF-8"),
                            mMusic.getUrl());
                } else if (!mMusic.getOggSize().equals("0")) {
                    url = String.format(URL, URLEncoder.encode("sogg", "UTF-8"),
                            mMusic.getUrl());
                } else if (!mMusic.getMp3Size().equals("0")) {
                    url = String.format(URL, URLEncoder.encode("sogg", "UTF-8"),
                            mMusic.getUrl());
                }
                if (url != null) {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    play.setImageDrawable(pauseToPlayDrawable);
                    pauseToPlayDrawable.start();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_recourse,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setImageDrawable(playToPauseDrawable);
                    playToPauseDrawable.start();
                } else {
                    mediaPlayer.start();
                    play.setImageDrawable(pauseToPlayDrawable);
                    pauseToPlayDrawable.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                handler.sendEmptyMessage(1);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 下载专辑封面
     */
    private void downloadAlbumArt() {
        String[] strings = getFileName();
        File file = new File(dir,
                strings[1] + " - " + strings[0] + ".jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(albumArtBytes);
            fileOutputStream.close();
            Toast.makeText(getApplicationContext(), R.string.download_success, Toast.LENGTH_SHORT)
                    .show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), R.string.download_failed, Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    /**
     * 返回格式化后的文件名称
     *
     * @return 返回一个String数组 s[0]和s[1]分别为格式化后的歌曲名，歌手们
     */
    private String[] getFileName() {
        String newSongTitle, newArtist;
        String songTitle = mMusic.getTitle();
        String artist = mMusic.getArtist();
        newSongTitle = songTitle;

        if (songTitle.contains("/")) {
            newSongTitle = songTitle.replace("/", " - ");
        }

        newArtist = artist;

        if (artist.contains("/")) {
            newArtist = artist.replace("/", " - ");
        }

        dir = new File(MainActivity.LOCAL_PATH, MainActivity.FILE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new String[]{newSongTitle, newArtist};
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }
}
