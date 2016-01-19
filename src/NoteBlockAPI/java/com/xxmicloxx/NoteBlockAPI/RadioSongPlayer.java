package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;
import cn.nukkit.level.Position;

public class RadioSongPlayer extends SongPlayer {

    public RadioSongPlayer(Song song) {
        super(song);
    }

    @Override
    public void playTick(Player p, int tick) {
        byte playerVolume = NoteBlockAPI.getInstance().getPlayerVolume(p);
        Position pos = p.getPosition();
        pos.y += p.getEyeHeight();
        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            //Server.getInstance().getLogger().info("play..." + String.valueOf(note.getInstrument()) + String.valueOf(note.getKey() - 33));

            //System.out.println(pk.x + " " + pk.y + " " + pk.z + " " + pk.case1 + " " + pk.case2);
        }
    }
}
