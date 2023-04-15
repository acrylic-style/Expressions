package xyz.acrylicstyle.expressions.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.expressions.spigot.util.SpigotUtil;
import xyz.acrylicstyle.util.expression.CompileData;
import xyz.acrylicstyle.util.expression.ExpressionParser;
import xyz.acrylicstyle.util.expression.RuntimeData;
import xyz.acrylicstyle.util.expression.instruction.InstructionSet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionsCommand implements TabExecutor {
    private final SpigotPlugin plugin;

    public ExpressionsCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (plugin.disallowNonPlayerSender && !(sender instanceof Player)) {
            return true;
        }
        String input = String.join(" ", args);
        try {
            CompileData.Builder compileDataBuilder =
                    CompileData.builder()
                            .allowPrivate(true)
                            .addVariable("sender", sender.getClass())
                            .addVariable("server", Bukkit.getServer().getClass());
            RuntimeData.Builder runtimeDataBuilder =
                    RuntimeData.builder()
                            .allowPrivate(true)
                            .addVariable("sender", sender)
                            .addVariable("server", Bukkit.getServer());
            for (Player player : Bukkit.getOnlinePlayers()) {
                compileDataBuilder.addVariable("player_" + player.getName(), player.getClass());
                runtimeDataBuilder.addVariable("player_" + player.getName(), player);
            }
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                compileDataBuilder.addVariable("plugin_" + plugin.getName(), plugin.getClass());
                runtimeDataBuilder.addVariable("plugin_" + plugin.getName(), plugin);
            }
            InstructionSet instructionSet = ExpressionParser.compile(input, compileDataBuilder.build());
            sender.sendMessage(String.valueOf(instructionSet.execute(runtimeDataBuilder.build())));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (plugin.disallowNonPlayerSender && !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        try {
            return SpigotUtil.getSuggestions(sender, String.join(" ", args)).collect(Collectors.toList());
        } catch (Exception e) {
            String message = e.getMessage();
            return Collections.singletonList(ChatColor.RED + message.substring(0, Math.min(150, message.length())));
        }
    }
}
