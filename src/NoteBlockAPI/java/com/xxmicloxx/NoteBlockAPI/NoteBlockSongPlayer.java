package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.network.protocol.BlockEventPacket;

/**
 * Created with IntelliJ IDEA.
 * User: ml
 * Date: 07.12.13
 * Time: 12:56
 */
public class NoteBlockSongPlayer extends SongPlayer {
    private Block noteBlock;

    public NoteBlockSongPlayer(Song song) {
        super(song);
    }

    public Block getNoteBlock() {
        return noteBlock;
    }

    public void setNoteBlock(Block noteBlock) {
        this.noteBlock = noteBlock;
    }

    @Override
    public void playTick(Player p, int tick) {
        if (noteBlock.getId() != 25) {
            return;
        }
        if (!p.getLevel().getFolderName().equals(noteBlock.getLevel().getFolderName())) {
            // not in same world
            return;
        }
        byte playerVolume = NoteBlockAPI.getInstance().getPlayerVolume(p);

        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            BlockEventPacket pk = new BlockEventPacket();
            pk.x = (int) noteBlock.x;
            pk.y = (int) noteBlock.y;
            pk.z = (int) noteBlock.z;
            pk.case1 = note.getInstrument();
            pk.case2 = note.getKey() - 33;
            p.dataPacket(pk);
            //p.getLevel().addSound(new MusicBlocksSound(noteBlock, note.getInstrument(), note.getKey()), new Player[]{p});
        }
    }
}
