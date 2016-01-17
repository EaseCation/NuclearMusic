package com.fcmcpe.nuclear.music;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import com.xxmicloxx.NoteBlockAPI.NBSDecoder;
import com.xxmicloxx.NoteBlockAPI.PositionSongPlayer;
import com.xxmicloxx.NoteBlockAPI.Song;
import com.xxmicloxx.NoteBlockAPI.SongPlayer;

import java.io.File;
import java.util.*;

public class NuclearMusicPlugin extends PluginBase {

    private LinkedList<Song> songs = new LinkedList<>();
    private Map<Position, SongPlayer> songPlayers = new HashMap<>();

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

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new NuclearMusicListener(), this);
        loadAllSongs();
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

    class NuclearMusicListener implements Listener {
        @EventHandler
        public void onBlockTouch(PlayerInteractEvent event) {
            if (event.getAction() != PlayerInteractEvent.RIGHT_CLICK_BLOCK) return;
            if (!event.getPlayer().isCreative()) return;
            if (event.getPlayer().getInventory().getItemInHand().getId() != Item.DIAMOND_HOE) return;
            if (event.getBlock().getId() != Item.NOTEBLOCK) return;

            Song song;

            if (songPlayers.containsKey(event.getBlock())) {
                SongPlayer sp = songPlayers.get(event.getBlock());
                Song now = sp.getSong();
                songPlayers.get(event.getBlock()).setPlaying(false);
                songPlayers.remove(event.getBlock());
                event.getPlayer().sendMessage("Destroyed song");
                song = nextSong(now);
                getServer().getOnlinePlayers().forEach((s, p) -> sp.removePlayer(p));
            } else {
                song = songs.getFirst();
            }

            SongPlayer songPlayer = new PositionSongPlayer(song, event.getBlock());
            songPlayer.setAutoCycle(true);
            songPlayer.setAutoDestroy(false);
            getServer().getOnlinePlayers().forEach((s, p) -> songPlayer.addPlayer(p));
            songPlayer.setPlaying(true);
            songPlayers.put(event.getBlock(), songPlayer);
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
            NuclearMusic.INSTANCE.stopPlaying(player);
        }
    }

}
