package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnplay,btnNext,btnPrevious,btnFastForward,btnFastBackword;
    TextView txtSongStart,txtSongEnd,txtSongName;
    SeekBar seekmusicBar;
    ImageView songImage;
    String songName;
    public  static final String EXTRA_NAME ="song_name";
    static MediaPlayer mediaPlayer;
    ArrayList<File> mySongs;
    int position;
    Thread updateSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Music Player");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnplay=findViewById(R.id.btnPlay);
        btnNext=findViewById(R.id.btnNext);
        btnFastBackword=findViewById(R.id.btnFastbackword);
        btnFastForward=findViewById(R.id.btnFastForward);
        btnPrevious=findViewById(R.id.btnPrevious);

        txtSongEnd=findViewById(R.id.txtSongEnd);
        txtSongStart=findViewById(R.id.txtSongStart);
        txtSongName=findViewById(R.id.txtSongname);
        songImage=findViewById(R.id.imgSong);

        seekmusicBar=findViewById(R.id.seekBar);
        //barVisualizer=findViewById(R.id.blast);

        if (mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent =getIntent();
        Bundle bundle =intent.getExtras();
        mySongs = (ArrayList)bundle.getParcelableArrayList("song");
        String sName =intent.getStringExtra("songname");
        position=bundle.getInt("pos",0);
        txtSongName.setSelected(true);
        Uri uri =Uri.parse(mySongs.get(position).toString());
        songName =mySongs.get(position).getName();
        txtSongName.setText(songName);
        mediaPlayer =MediaPlayer.create(getBaseContext(),uri);
        mediaPlayer.start();

        updateSeekBar =new Thread()
        {
            @Override
            public void run() {
                int totalDuration =mediaPlayer.getDuration();
                int currentDuration =0;
                while (currentDuration<totalDuration){
                    try {
                            sleep(500);
                            currentDuration =mediaPlayer.getCurrentPosition();
                            seekmusicBar.setProgress(currentDuration);
                    }catch (InterruptedException | IllegalStateException e){
                      e.printStackTrace();
                    }
                }

            }
        };

        seekmusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekmusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary_dark), PorterDuff.Mode.MULTIPLY);
        seekmusicBar.getThumb().setColorFilter(getResources().getColor(R.color.av_orange),PorterDuff.Mode.SRC_IN);

        seekmusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        String endTime = craeteTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler =new Handler();
        final int delay =1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = craeteTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    btnplay.setBackgroundResource(R.drawable.ic_play_);
                    mediaPlayer.pause();
                }
                else
                {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });
        /*int audioSessionId =mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1)
        {
            barVisualizer.setAudioSessionId(audioSessionId);
        }*/


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position+1)%mySongs.size());
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();


                startAnimation(songImage,360f);
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position-1)<0)?(mySongs.size()-1):position-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(songImage,-360f);
            }
        });
    }
    public void startAnimation(View view,float degree)
    {
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(songImage,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet =new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }
    public String craeteTime(int duration){
        String time ="";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time =time+min+":";
        if (sec<10){
            time += "0";
        }
        time +=sec;
        return time;
    }
}
