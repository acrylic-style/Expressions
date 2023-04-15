package xyz.acrylicstyle.expressions.spigot.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.acrylicstyle.expressions.common.util.Util;
import xyz.acrylicstyle.util.expression.CompileData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SpigotUtil {
    public static Stream<String> getSuggestions(Object sender, String input) {
        List<String> variableNames = new ArrayList<>();
        variableNames.add("sender");
        variableNames.add("server");
        CompileData.Builder compileDataBuilder =
                CompileData.builder()
                        .allowPrivate(true)
                        .addVariable("sender", sender.getClass())
                        .addVariable("server", Bukkit.getServer().getClass());
        for (Player player : Bukkit.getOnlinePlayers()) {
            variableNames.add("player_" + player.getName());
            compileDataBuilder.addVariable("player_" + player.getName(), player.getClass());
        }
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            variableNames.add("plugin_" + plugin.getName());
            compileDataBuilder.addVariable("plugin_" + plugin.getName(), plugin.getClass());
        }
        try {
            return Util.getSuggestionsPartial(variableNames.stream(), compileDataBuilder.build(), input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
