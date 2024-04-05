package de.derioo.chals.randomizer.commands;

import de.derioo.chals.randomizer.Randomizer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RandomizerCommand implements CommandExecutor, TabExecutor {

    private final Randomizer plugin;

    public RandomizerCommand(Randomizer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.execute(sender, args);
        return false;
    }

    private void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("randomizer.start")) {
            sender.sendMessage(Component.text("Keine Rechte"));
            return;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("start")) {
            plugin.getJsonConfig().addProperty("present", true);
            sender.sendMessage(Component.text("Der Randomizer ist an, falls er aus war"));
            return;
        }
        plugin.getJsonConfig().addProperty("present", false);
        sender.sendMessage(Component.text("Der Randomizer ist aus, falls er an war"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("start", "stop");
    }
}
