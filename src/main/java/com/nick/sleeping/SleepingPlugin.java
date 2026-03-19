package com.nick.sleeping;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SleepingPlugin extends JavaPlugin implements Listener {

    NamespacedKey bedPersistKey = new NamespacedKey(this, "bed");
    NamespacedKey bedWorldKey = new NamespacedKey(this, "bed_world");

    JavaPlugin plugin = this;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("wakeup").setExecutor(null);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player viewer : getServer().getOnlinePlayers()) {
                    boolean isSpectator = viewer.getGameMode() == GameMode.SPECTATOR;

                    for (Player target : getServer().getOnlinePlayers()) {
                        if (viewer == target) continue;

                        if (isSpectator) {
                            if (!viewer.canSee(target)) {
                                viewer.showPlayer(plugin, target);
                            }
                        } else {
                            if (viewer.canSee(target)) {
                                viewer.hidePlayer(plugin, target);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }


    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != BedEnterResult.OK) return;
        Player player = event.getPlayer();

        Block bed = event.getBed();
        Location bedLoc = bed.getLocation();

        int[] coords = new int[] { bedLoc.getBlockX(), bedLoc.getBlockY(), bedLoc.getBlockZ() };

        player.getPersistentDataContainer().set(
            bedPersistKey,
            PersistentDataType.INTEGER_ARRAY,
            coords
        );

        player.getPersistentDataContainer().set(
            bedWorldKey,
            PersistentDataType.STRING,
            bedLoc.getWorld().getName()
        );

        player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;

            PersistentDataContainer data = player.getPersistentDataContainer();

            if (data.has(bedPersistKey) && data.has(bedWorldKey)) {
                int[] coords = data.get(bedPersistKey, PersistentDataType.INTEGER_ARRAY);
                String worldName = data.get(bedWorldKey, PersistentDataType.STRING);

                if (coords != null && coords.length == 3 && worldName != null) {
                    World world = getServer().getWorld(worldName);
                    if (world != null) {
                        Location bedLoc = new Location(world, coords[0], coords[1], coords[2]);
                        player.teleport(bedLoc);
                        player.setGameMode(GameMode.SURVIVAL);
                        data.set(bedPersistKey, PersistentDataType.INTEGER_ARRAY, new int[] {});
                        data.set(bedWorldKey, PersistentDataType.STRING, "");
                    } else {
                        player.sendMessage("no bed to wake up to");
                    }
                } else {
                    player.sendMessage("no bed to wake up to");
                }
            } else {
                player.sendMessage("no bed to wake up to");
            }
        }
        return true;
    }
}
