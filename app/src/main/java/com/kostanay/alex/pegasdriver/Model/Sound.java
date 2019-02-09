package com.kostanay.alex.pegasdriver.Model;

public class Sound {
    private String AssetPath;
    private String Name;
    private Integer SoundId;

    public Sound(String assetPath){
        AssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length - 1];
        Name = filename.replace(".wav", "");
    }

    public String getAssetPath() {
        return AssetPath;
    }

    public String getName() {
        return Name;
    }

    public Integer getSoundId() {
        return SoundId;
    }

    public void setSoundId(Integer soundId){
        SoundId = soundId;
    }
}
