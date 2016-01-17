package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class SongStoppedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private SongPlayer song;

    public SongStoppedEvent(SongPlayer song) {
        this.song = song;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public SongPlayer getSongPlayer() {
        return song;
    }
}
