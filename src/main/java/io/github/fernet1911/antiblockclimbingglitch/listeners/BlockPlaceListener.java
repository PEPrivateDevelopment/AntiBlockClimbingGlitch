package io.github.fernet1911.antiblockclimbingglitch.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.BoundingBox;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            Block block = event.getBlock();
            BlockData data = block.getBlockData();

            // Do not ignore the block in the event of a double slab block creation
            if (!(data instanceof Slab && ((Slab) data).getType().equals(Slab.Type.DOUBLE))) {
                block.setType(Material.AIR, false); // Change the placed block to AIR to get an instant preview
            }

            /*
            Teleport the player back 1 block below if the Y of the placed block is <= of the Y of the player
            and the distance between the X and Z of the placed block is <= 0.3 of the X and Z of the player
             */
            double xDiff = block.getX() - location.getX();
            double zDiff = block.getZ() - location.getZ();
            Block firstBlock = location.getBlock();
            if ((xDiff <= 0.3 && xDiff >= -1.3) && (zDiff <= 0.3 && zDiff >= -1.3) && location.getBlockY() >= block.getY() && firstBlock.isPassable()) {
                Block secondBlock = firstBlock.getRelative(BlockFace.DOWN);
                if (secondBlock.isPassable()) {
                    Block thirdBlock = secondBlock.getRelative(BlockFace.DOWN);

                    // Get the height of the third block to TP the player to
                    double y = 0;
                    for (BoundingBox boundingBox : thirdBlock.getCollisionShape().getBoundingBoxes()) {
                        double maxY = boundingBox.getMaxY();
                        if (maxY > y) {
                            y = maxY;
                        }
                    }
                    // Set a default height of 1 for passable blocks like AIR
                    if (y == 0) {
                        y = 1;
                    }

                    // TP the player to the new location
                    location.setY(thirdBlock.getY() + y);
                    player.teleport(location);
                }
            }

            block.setBlockData(data, false); // Undo the block change
        }
    }

}
