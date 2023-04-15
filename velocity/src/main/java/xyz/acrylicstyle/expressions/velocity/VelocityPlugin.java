package xyz.acrylicstyle.expressions.velocity;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.acrylicstyle.expressions.common.util.Util;
import xyz.acrylicstyle.util.InvalidArgumentException;
import xyz.acrylicstyle.util.expression.CompileData;
import xyz.acrylicstyle.util.expression.ExpressionParser;
import xyz.acrylicstyle.util.expression.RuntimeData;
import xyz.acrylicstyle.util.expression.instruction.InstructionSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Plugin(id = "expressions")
public class VelocityPlugin {
    @Inject
    public VelocityPlugin(ProxyServer server) {
        server.getCommandManager().register(new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("exprv")
                        .requires(source -> source.hasPermission("expressions.use"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("expression", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> suggest(suggest(ctx.getSource(), server, builder.getRemaining()), builder))
                                .executes((ctx) -> execute(ctx.getSource(), server, ctx.getArgument("expression", String.class)))
                        )
        ));
    }

    private static Stream<String> suggest(CommandSource source, ProxyServer server, String input) {
        List<String> variableNames = new ArrayList<>();
        variableNames.add("source");
        variableNames.add("server");
        CompileData.Builder compileDataBuilder = CompileData.builder()
                .addVariable("source", source.getClass())
                .addVariable("server", server.getClass());
        for (Player player : server.getAllPlayers()) {
            variableNames.add("player_" + player.getUsername());
            compileDataBuilder.addVariable("player_" + player.getUsername(), player.getClass());
        }
        try {
            return Util.getSuggestionsFull(variableNames.stream(), compileDataBuilder.build(), input);
        } catch (Exception e) {
            String message = e.getMessage();
            return Stream.of("§c" + message.substring(0, Math.min(150, message.length())));
        }
    }

    private static int execute(CommandSource source, ProxyServer server, String input) {
        CompileData.Builder compileDataBuilder = CompileData.builder()
                .allowPrivate(true)
                .addVariable("source", source.getClass())
                .addVariable("server", server.getClass());
        RuntimeData.Builder runtimeDataBuilder = RuntimeData.builder()
                .allowPrivate(true)
                .addVariable("source", source)
                .addVariable("server", server);
        for (Player player : server.getAllPlayers()) {
            compileDataBuilder.addVariable("player_" + player.getUsername(), player.getClass());
            runtimeDataBuilder.addVariable("player_" + player.getUsername(), player);
        }
        try {
            InstructionSet instructionSet = ExpressionParser.compile(input, compileDataBuilder.build());
            Object result = instructionSet.execute(runtimeDataBuilder.build());
            source.sendMessage(Component.text(String.valueOf(result)));
        } catch (InvalidArgumentException e) {
            source.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
            throw new RuntimeException(e);
        }
        return 0;
    }

    private static CompletableFuture<Suggestions> suggest(Stream<String> stream, SuggestionsBuilder suggestionsBuilder) {
        Objects.requireNonNull(suggestionsBuilder);
        String s = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        stream.filter((s2) -> matchesSubStr(s, s2.toLowerCase(Locale.ROOT))).forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();
    }

    private static boolean matchesSubStr(String s, String s2) {
        for (int i = 0; !s2.startsWith(s, i); ++i) {
            i = s2.indexOf(95, i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }
}
