package de.derioo.chals.randomizer;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.derioo.chals.randomizer.commands.PluginBrigadierCommand;
import de.derioo.chals.randomizer.commands.RandomizerCommand;
import de.derioo.chals.randomizer.listener.BlockBreakListener;
import de.derioo.chals.server.api.Config;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.players;


import static net.minecraft.commands.Commands.literal;

public final class Randomizer extends JavaPlugin implements Listener {

    @Getter
    private Map<Material, Material> blocks = new HashMap<>();

    private Config config;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        config = new Config(this, "randomizer");
        for (Map.Entry<String, JsonElement> entry : config.get().get("blocks").getAsJsonObject().entrySet()) {
            blocks.put(Material.valueOf(entry.getKey()), Material.valueOf(entry.getValue().getAsString()));
        }

        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

        this.registerPluginBrigadierCommand("randomizer", literal ->
            literal.requires(stack -> stack.getBukkitEntity().hasPermission("sc.randomizer"))
                    .then(literal("start").executes((ctx -> {
                        getJsonConfig().addProperty("present", true);
                        ctx.getSource().getBukkitSender().sendMessage(Component.text("Der Randomizer ist nun an, falls er aus war"));
                        return Command.SINGLE_SUCCESS;
                    })))
                    .then(literal("stop").executes((ctx -> {
                        getJsonConfig().addProperty("present", false);
                        ctx.getSource().getBukkitSender().sendMessage(Component.text("Der Randomizer ist nun aus, falls er an war"));
                        return Command.SINGLE_SUCCESS;
                    })))
        );
    }

    public JsonObject getJsonConfig() {
        return config.get();
    }

    @Override
    public void onDisable() {
        saveJsonConfig();
    }


    public void saveJsonConfig() {
        for (Map.Entry<Material, Material> entry : blocks.entrySet()) {
            config.get().get("blocks").getAsJsonObject().addProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        config.save();
    }

    private PluginBrigadierCommand registerPluginBrigadierCommand(final String label, final Consumer<LiteralArgumentBuilder<CommandSourceStack>> command) {
        final PluginBrigadierCommand pluginBrigadierCommand = new PluginBrigadierCommand(this, label, command);
        this.getServer().getCommandMap().register(this.getName(), pluginBrigadierCommand);
        ((CraftServer) this.getServer()).syncCommands();
        return pluginBrigadierCommand;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @EventHandler
    public void onCommandRegistered(final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        if (!(event.getCommand() instanceof PluginBrigadierCommand pluginBrigadierCommand)) {
            return;
        }
        final LiteralArgumentBuilder<CommandSourceStack> node = literal(event.getCommandLabel());
        pluginBrigadierCommand.command().accept(node);
        event.setLiteral((LiteralCommandNode) node.build());
    }
}
