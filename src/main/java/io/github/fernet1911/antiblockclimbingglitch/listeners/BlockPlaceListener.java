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
import org.bukkit.util.NumberConversions;

import java.util.HashSet;
import java.util.Set;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            BoundingBox boundingBox = player.getBoundingBox();
            Block block = event.getBlock();
            BlockData blockData = block.getBlockData();

            // Ignore the placed block to get an instant preview
            if (blockData instanceof Slab && ((Slab) blockData).getType().equals(Slab.Type.DOUBLE)) {
                BlockData bd = blockData.clone();
                ((Slab) bd).setType(Slab.Type.BOTTOM);
                block.setBlockData(bd, false);
            } else {
                block.setType(Material.AIR, false);
            }

            /*
            Teleport the player back 1 block below if the Y of the placed block is <= of the Y of the player
            and the distance between the X and Z of the placed block is <= 0.3 of the X and Z of the player
             */
            double xDiff = block.getX() - location.getX();
            double zDiff = block.getZ() - location.getZ();

            if ((xDiff <= 0.3 && xDiff >= -1.3) && (zDiff <= 0.3 && zDiff >= -1.3) && block.getY() <= location.getBlockY()) {
                Set<Block> firstBlocks = getFloorBlocks(location, boundingBox);
                Set<Block> secondBlocks = new HashSet<>();
                if (getLowerBlocks(firstBlocks, secondBlocks)) {
                    Set<Block> thirdBlocks = new HashSet<>();
                    if (getLowerBlocks(secondBlocks, thirdBlocks)) {
                        // Get the height of the third block and TP the player there
                        location.setY(location.getBlockY() - 2 + getMaxY(thirdBlocks));
                        player.teleport(location);
                    }
                }

                block.setBlockData(blockData, false); // Undo the block change
            }
        }
    }

    /**
     * Get the set of blocks representing the "floor" the player is standing on.
     *
     * @param location    Location to get the "floor" from
     * @param boundingBox BoundingBox of the player to check what blocks he's standing on
     * @return The set of blocks the player is standing on.
     */
    private Set<Block> getFloorBlocks(Location location, BoundingBox boundingBox) {
        Set<Block> floorBlocks = new HashSet<>();
        int blx = NumberConversions.floor(boundingBox.getMinX());
        int bgx = NumberConversions.ceil(boundingBox.getMaxX());
        int blz = NumberConversions.floor(boundingBox.getMinZ());
        int bgz = NumberConversions.ceil(boundingBox.getMaxZ());

        for (int x = blx; x < bgx; x++) {
            for (int z = blz; z < bgz; z++) {
                floorBlocks.add(new Location(location.getWorld(), x, location.getY(), z).getBlock());
            }
        }

        return floorBlocks;
    }

    /**
     * Get the set of blocks "1 layer" below the Source. Destination is the resulting set.
     *
     * @param sourceBlocks      Source set
     * @param destinationBlocks Destination set
     * @return A boolean telling if the player is trying to climb or not. The return value must be true for the Destination set to be considered exhaustive.
     */
    private boolean getLowerBlocks(Set<Block> sourceBlocks, Set<Block> destinationBlocks) {
        boolean climbing = true;

        for (Block b : sourceBlocks) {
            if (!b.isPassable()) {
                climbing = false;
                break;
            }

            destinationBlocks.add(b.getRelative(BlockFace.DOWN));
        }

        return climbing;
    }

    /**
     * Get the max height of the blocks in the set.
     *
     * @param blocks Set of blocks to check from
     * @return A double representing the max height of the block.
     */
    private double getMaxY(Set<Block> blocks) {
        double y = 0;

        for (Block b : blocks) {
            // Get the block's max height or set a default height of 1 for passable blocks like AIR
            double maxY = b.getCollisionShape().getBoundingBoxes().stream().mapToDouble(BoundingBox::getMaxY).max().orElse(1);

            if (maxY > y) {
                y = maxY;
            }
        }

        return y;
    }

}
