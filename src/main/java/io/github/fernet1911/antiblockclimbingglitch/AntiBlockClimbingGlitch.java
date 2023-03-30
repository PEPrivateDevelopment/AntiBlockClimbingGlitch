package io.github.fernet1911.antiblockclimbingglitch;

import io.github.fernet1911.antiblockclimbingglitch.listeners.BlockPlaceListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiBlockClimbingGlitch extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);

    }

    @Override
    public void onDisable() {
    }
}
