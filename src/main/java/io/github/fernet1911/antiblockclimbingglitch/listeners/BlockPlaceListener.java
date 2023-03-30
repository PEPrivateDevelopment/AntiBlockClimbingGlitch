package io.github.fernet1911.antiblockclimbingglitch.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            Block block = event.getBlock();
            Material material = block.getType();
            BlockData data = block.getBlockData();

            // Do not ignore the block in the event of a double slab block creation
            if (!(data instanceof Slab && ((Slab) data).getType().equals(Slab.Type.DOUBLE))) {
                block.setType(Material.AIR, false); // Change the placed block to AIR to get an instant preview
            }

            /*
            Teleport the player back 1 block below if the block was placed on the same (or lower) Y of the player
            and the distance between the X and Z of the placed block is <= 0.3 of the X and Z of the player
             */
            double xDiff = block.getX() - location.getX();
            double zDiff = block.getZ() - location.getZ();
            if ((xDiff <= 0.3 && xDiff >= -1.3) && (zDiff <= 0.3 && zDiff >= -1.3) && location.getBlockY() >= block.getY() && location.getBlock().isPassable()) {
                location.add(0, -1, 0);
                if (location.getBlock().isPassable()) {
                    player.teleport(location);
                }
            }

            block.setType(material); // Undo the block change
        }
    }

}
