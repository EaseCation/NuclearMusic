package com.fcmcpe.nuclear.music;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import com.xxmicloxx.NoteBlockAPI.*;

import java.io.File;
import java.util.*;

public class NuclearMusicPlugin extends PluginBase {

    /********
     * internal
     ********/

    private LinkedList<Song> songs = new LinkedList<>();
    private Map<NodeIntegerPosition, SongPlayer> songPlayers = new HashMap<>();

    /******** loading songs ********/

    static List<File> getAllNBSFiles(File path) {
        List<File> result = new ArrayList<>();
        File[] subFile = path.listFiles();
        if (subFile == null) return result;
        for (File aSubFile : subFile) {
            if (aSubFile.isDirectory()) continue;
            if (!aSubFile.getName().trim().toLowerCase().endsWith(".nbs")) continue;
            result.add(aSubFile);
        }
        return result;
    }

    /******** plugin ********/

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new NuclearMusicListener(), this);
        loadAllSongs();
        getServer().getScheduler().scheduleAsyncTask(new TickerRunnable());
    }

    private void loadAllSongs() {
        List<File> files = getAllNBSFiles(new File(getDataFolder(), "tracks"));
        files.forEach(file -> {
            Song song = NBSDecoder.parse(file);
            if (song == null) return;
            songs.add(song);
            getLogger().info("song: " + file.getName() + ": " + song.getTitle());
        });
        songs.sort((s1, s2) -> s1.getTitle().compareTo(s2.getTitle()));
        getLogger().info("loaded " + songs.size() + " songs.");
    }

    private Song nextSong(Song now) {
        if (!songs.contains(now)) return songs.getFirst();
        if (songs.indexOf(now) >= songs.size() - 1) return songs.getFirst();
        return songs.get(songs.indexOf(now) + 1);
    }

    class NodeIntegerPosition {
        int x;
        int y;
        int z;
        Level level;

        NodeIntegerPosition(int x, int y, int z, Level level) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.level = level;
        }

        NodeIntegerPosition(Position position) {
            this(position.getFloorX(), position.getFloorY(), position.getFloorZ(), position.getLevel());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NodeIntegerPosition)) return false;
            NodeIntegerPosition node = (NodeIntegerPosition) obj;
            return (x == node.x) && (y == node.y) && (z == node.z) && (level == node.level);
        }

        @Override
        public int hashCode() {
            return (x + ":" + y + ":" + z + ":" + level.getName()).hashCode();
        }
    }

    /******** Listener ********/

    class NuclearMusicListener implements Listener {
        @EventHandler
        public void onBlockTouch(PlayerInteractEvent event) {
            if (event.getAction() != PlayerInteractEvent.RIGHT_CLICK_BLOCK) return;
            if (!event.getPlayer().isCreative()) return;
            if (event.getPlayer().getInventory().getItemInHand().getId() != Item.DIAMOND_HOE) return;
            if (event.getBlock().getId() != Item.NOTEBLOCK) return;

            Song song;
            NodeIntegerPosition node = new NodeIntegerPosition(event.getBlock());

            if (songPlayers.containsKey(node)) {
                SongPlayer sp = songPlayers.get(node);
                Song now = sp.getSong();
                songPlayers.get(node).setPlaying(false);
                songPlayers.remove(node);
                song = nextSong(now);
                getServer().getOnlinePlayers().forEach((s, p) -> sp.removePlayer(p));
            } else {
                song = songs.getFirst();
            }

            NoteBlockSongPlayer songPlayer = new NoteBlockSongPlayer(song);
            songPlayer.setNoteBlock(event.getBlock());
            songPlayer.setAutoCycle(true);
            songPlayer.setAutoDestroy(false);
            getServer().getOnlinePlayers().forEach((s, p) -> songPlayer.addPlayer(p));
            songPlayer.setPlaying(true);
            songPlayers.put(node, songPlayer);
            event.getPlayer().sendMessage("Now playing: " + song.getTitle());

        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            songPlayers.forEach((p, s) -> s.addPlayer(player));
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            NoteBlockAPI.getInstance().stopPlaying(player);
        }
    }

    /********
     * Multi-thread part
     ********/

    class TickerRunnable extends AsyncTask {

        @Override
        public void onRun() {
            while (isEnabled()) {
                NoteBlockAPI.getInstance().playingSongs.forEach((s, a) -> a.forEach((SongPlayer::tryPlay)));
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignore) {
                }
            }
        }

    }

}
