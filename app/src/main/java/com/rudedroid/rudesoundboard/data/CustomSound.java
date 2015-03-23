package com.rudedroid.rudesoundboard.data;

import java.io.Serializable;

public class CustomSound implements Serializable {
	private String title;
	private String sound;
	
	public CustomSound() {
		super();
		setSound("");
		title = "";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}
}
