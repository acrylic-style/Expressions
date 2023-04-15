package xyz.acrylicstyle.expressions.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SpigotPlugin extends JavaPlugin {
    public boolean disallowNonPlayerSender = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        disallowNonPlayerSender = getConfig().getBoolean("disallowNonPlayerSender", true);
        Objects.requireNonNull(getCommand("expr")).setExecutor(new ExpressionsCommand(this));
    }
}
