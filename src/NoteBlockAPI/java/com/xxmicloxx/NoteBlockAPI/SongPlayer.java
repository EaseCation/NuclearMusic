package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.fcmcpe.nuclear.music.NuclearMusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SongPlayer {

    protected Song song;
    protected boolean playing = false;
    protected short tick = -1;
    protected ArrayList<String> playerList = new ArrayList<>();
    protected boolean autoDestroy = false;
    protected boolean destroyed = false;
    protected boolean autoCycle = true;
    protected byte fadeTarget = 100;
    protected byte volume = 100;
    protected byte fadeStart = volume;
    protected int fadeDuration = 60;
    protected int fadeDone = 0;
    protected FadeType fadeType = FadeType.FADE_LINEAR;

    public SongPlayer(Song song) {
        this.song = song;
        createThread();
    }

    public boolean getAutoCycle() {
        return autoCycle;
    }

    public void setAutoCycle(boolean b) {
        autoCycle = b;
    }

    public FadeType getFadeType() {
        return fadeType;
    }

    public void setFadeType(FadeType fadeType) {
        this.fadeType = fadeType;
    }

    public byte getFadeTarget() {
        return fadeTarget;
    }

    public void setFadeTarget(byte fadeTarget) {
        this.fadeTarget = fadeTarget;
    }

    public byte getFadeStart() {
        return fadeStart;
    }

    public void setFadeStart(byte fadeStart) {
        this.fadeStart = fadeStart;
    }

    public int getFadeDuration() {
        return fadeDuration;
    }

    public void setFadeDuration(int fadeDuration) {
        this.fadeDuration = fadeDuration;
    }

    public int getFadeDone() {
        return fadeDone;
    }

    public void setFadeDone(int fadeDone) {
        this.fadeDone = fadeDone;
    }

    protected void calculateFade() {
        if (fadeDone == fadeDuration) {
            return; // no fade today
        }
        double targetVolume = Interpolator.interpLinear(new double[]{0, fadeStart, fadeDuration, fadeTarget}, fadeDone);
        setVolume((byte) targetVolume);
        fadeDone++;
    }

    protected void createThread() {
        Server.getInstance().getScheduler().scheduleAsyncTask(new SongPlayerAsyncTask());
    }

    public List<String> getPlayerList() {
        return Collections.unmodifiableList(playerList);
    }

    public void addPlayer(Player p) {
        synchronized (this) {
            if (!playerList.contains(p.getName())) {
                playerList.add(p.getName());
                ArrayList<SongPlayer> songs = NuclearMusic.INSTANCE.playingSongs
                        .get(p.getName());
                if (songs == null) {
                    songs = new ArrayList<SongPlayer>();
                }
                songs.add(this);
                NuclearMusic.INSTANCE.playingSongs.put(p.getName(), songs);
            }
        }
    }

    public boolean getAutoDestroy() {
        synchronized (this) {
            return autoDestroy;
        }
    }

    public void setAutoDestroy(boolean value) {
        synchronized (this) {
            autoDestroy = value;
        }
    }

    public abstract void playTick(Player p, int tick);

    public void destroy() {
        synchronized (this) {
            SongDestroyingEvent event = new SongDestroyingEvent(this);
            Server.getInstance().getPluginManager().callEvent(event);
            //Bukkit.getScheduler().cancelTask(threadId);
            if (event.isCancelled()) {
                return;
            }
            destroyed = true;
            playing = false;
            setTick((short) -1);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        if (!playing) {
            SongStoppedEvent event = new SongStoppedEvent(this);
            Server.getInstance().getPluginManager().callEvent(event);
        }
    }

    public short getTick() {
        return tick;
    }

    public void setTick(short tick) {
        this.tick = tick;
    }

    public void removePlayer(Player p) {
        synchronized (this) {
            playerList.remove(p.getName());
            if (NuclearMusic.INSTANCE.playingSongs.get(p.getName()) == null) {
                return;
            }
            ArrayList<SongPlayer> songs = new ArrayList<>(
                    NuclearMusic.INSTANCE.playingSongs.get(p.getName()));
            songs.remove(this);
            NuclearMusic.INSTANCE.playingSongs.put(p.getName(), songs);
            if (playerList.isEmpty() && autoDestroy) {
                SongEndEvent event = new SongEndEvent(this);
                Server.getInstance().getPluginManager().callEvent(event);
                destroy();
            }
        }
    }

    public byte getVolume() {
        return volume;
    }

    public void setVolume(byte volume) {
        this.volume = volume;
    }

    public Song getSong() {
        return song;
    }

    class SongPlayerAsyncTask extends AsyncTask {

        @Override
        public void onRun() {

            while (!destroyed) {
                long startTime = System.currentTimeMillis();
                synchronized (SongPlayer.this) {
                    if (playing) {
                        calculateFade();
                        tick++;
                        if (tick > song.getLength()) {
                            playing = false;
                            tick = -1;
                            SongEndEvent event = new SongEndEvent(SongPlayer.this);
                            Server.getInstance().getPluginManager().callEvent(event);
                            if (autoDestroy) {
                                destroy();
                                return;
                            }
                            if (autoCycle) {
                                playing = true;
                            }
                        }
                        for (String s : playerList) {
                            try {
                                Player p = Server.getInstance().getPlayerExact(s);
                                if (p == null) {
                                    // offline...
                                    continue;
                                }
                                playTick(p, tick);
                            } catch (Exception e) {
                                //do nothing
                            }

                        }
                    }
                }
                long duration = System.currentTimeMillis() - startTime;
                float delayMillis = song.getDelay() * 50;
                if (duration < delayMillis) {
                    try {
                        Thread.sleep((long) (delayMillis - duration));
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }
    }
}
