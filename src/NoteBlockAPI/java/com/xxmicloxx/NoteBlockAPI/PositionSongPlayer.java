package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.BlockEventPacket;

public class PositionSongPlayer extends SongPlayer {

    private Position targetLocation;

    public PositionSongPlayer(Song song, Position targetLocation) {
        super(song);
        this.targetLocation = targetLocation;
    }

    public Position getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Position targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public void playTick(Player p, int tick) {
        if (!p.getLevel().getFolderName().equals(targetLocation.getLevel().getFolderName())) {
            // not in same world
            return;
        }
        byte playerVolume = NoteBlockAPI.getInstance().getPlayerVolume(p);

        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) continue;

            BlockEventPacket pk = new BlockEventPacket();
            pk.x = (int) targetLocation.x;
            pk.y = (int) targetLocation.y;
            pk.z = (int) targetLocation.z;
            pk.case1 = note.getInstrument();
            pk.case2 = note.getKey() - 33;
            p.dataPacket(pk);

            //p.getLevel().addSound(new MusicBlocksSound(targetLocation, note.getInstrument(), note.getKey()));
        }
    }
}
