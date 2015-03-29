package com.rudedroid.rudesoundboard.data;

import java.io.InputStream;
import java.util.List;

import android.content.Context;

import com.rudedroid.rudesoundboard.R;
import com.rudedroid.rudesoundboard.util.XMLParser;

public class SoundboardManager {
    private static List<CustomSound> soundList;

    public SoundboardManager(Context c) {
        XMLParser parser = new XMLParser();
        InputStream index = c.getResources().openRawResource(R.raw.index);
        soundList = parser.Parse(index);
    }

    public static List<CustomSound> getSoundList() {
        return soundList;
    }
}
