package com.kostanay.alex.pegasdriver.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class BeatBox {
    private static final String SOUNDS_FOLDER = "sample_sounds";
    private AssetManager Assets;
    private List<Sound> Sounds = new ArrayList<>();
    private SoundPool SoundPool;
    private Context Context;

    public BeatBox(Context context){
        Context = context;
        Assets = context.getAssets();
        SoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }
    private void loadSounds(){
        String[] soundNames;
        try{
            soundNames = Assets.list(SOUNDS_FOLDER);
        } catch (IOException e){
            return;
        }

        for(String filename : soundNames){
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Sound sound = new Sound(assetPath);
                load(sound);
                Sounds.add(sound);
            } catch (IOException e) {}
        }

    }

    public List<Sound> getSounds() {
        return Sounds;
    }

    private void load(Sound sound) throws IOException {
        AssetFileDescriptor afd = Assets.openFd(sound.getAssetPath());
        int soundId = SoundPool.load(afd, 1);
        sound.setSoundId(soundId);
    }

    public void play(Sound sound){
        SharedPreferences pref = Context.getSharedPreferences("MyPref", MODE_PRIVATE);
        boolean soundDisabled = pref.getBoolean("soundDisabled", false);
        if(!soundDisabled) {
            Integer soundId = sound.getSoundId();
            if (soundId == null) {
                return;
            }
            SoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void release(){
        SoundPool.release();
    }
}
