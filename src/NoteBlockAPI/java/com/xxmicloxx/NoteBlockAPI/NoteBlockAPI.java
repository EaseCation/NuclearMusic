package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Snake1999 on 2016/1/19.
 * Package com.xxmicloxx.NoteBlockAPI in project NuclearMusic.
 */
public final class NoteBlockAPI {

    private static NoteBlockAPI instance;
    public HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<>();
    public HashMap<String, Byte> playerVolume = new HashMap<>();

    private NoteBlockAPI() {
    }

    public static NoteBlockAPI getInstance() {
        if (instance == null) instance = new NoteBlockAPI();
        return instance;
    }

    public boolean isReceivingSong(Player p) {
        return ((playingSongs.get(p.getName()) != null) && (!playingSongs.get(p.getName()).isEmpty()));
    }

    public void stopPlaying(Player p) {
        if (playingSongs.get(p.getName()) == null) {
            return;
        }
        for (SongPlayer s : playingSongs.get(p.getName())) {
            s.removePlayer(p);
        }
    }

    public void setPlayerVolume(Player p, byte volume) {
        playerVolume.put(p.getName(), volume);
    }

    public byte getPlayerVolume(Player p) {
        Byte b = playerVolume.get(p.getName());
        if (b == null) {
            b = 100;
            playerVolume.put(p.getName(), b);
        }
        return b;
    }
}
