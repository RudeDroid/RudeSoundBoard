package com.rudedroid.rudesoundboard.data;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;

import com.rudedroid.rudesoundboard.R;

public class SoundboardManager {
	public ArrayList<CustomSound> soundsList;
	
	/** The INSTANCE. */
	private static SoundboardManager INSTANCE = null;

	public SoundboardManager(Context c) {
		XMLParser parser = new XMLParser();
		InputStream index = c.getResources().openRawResource(R.raw.index);
		soundsList = parser.Parse(index);
		
		INSTANCE = this;
	}

	public static SoundboardManager getInstance(Context c) {
		if(INSTANCE == null) {
			new SoundboardManager(c);
		}
		return INSTANCE;
	}
}
