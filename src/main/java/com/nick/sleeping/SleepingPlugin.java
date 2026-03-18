package com.nick.sleeping;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.java.JavaPlugin;

public class SleepingPlugin extends JavaPlugin implements Listener {

    private final Map<Player, Block> beds = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if(event.getBedEnterResult() != BedEnterResult.OK) return;
        Player player = event.getPlayer();

        beds.put(player, event.getBed());
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (!beds.containsKey(player)) return;

        Block bed = beds.get(player);
        if (clicked.getLocation() == bed.getLocation()) {
            player.setGameMode(GameMode.SURVIVAL);
            beds.remove(player);

            player.teleport(bed.getLocation().add(0, 1, 0));
        }
    }
}
