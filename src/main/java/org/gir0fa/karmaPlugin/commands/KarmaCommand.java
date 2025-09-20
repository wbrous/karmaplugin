package org.gir0fa.karmaPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.gir0fa.karmaPlugin.model.Alignment;
import org.gir0fa.karmaPlugin.service.KarmaService;

import java.util.*;
import java.util.stream.Collectors;

public class KarmaCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final KarmaService karmaService;

    public KarmaCommand(Plugin plugin, KarmaService karmaService) {
        this.plugin = plugin;
        this.karmaService = karmaService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                int value = karmaService.getKarma(player.getUniqueId());
                Alignment alignment = karmaService.getAlignment(player.getUniqueId());
                sender.sendMessage(ChatColor.YELLOW + "Karma: " + ChatColor.WHITE + value + ChatColor.GRAY + " (" + alignment + ")");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " get <player> | give/take/set <player> <amount> | reload");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "get" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " get <player>");
                    return true;
                }
                if (!sender.hasPermission("karma.view.others")) {
                    sender.sendMessage(ChatColor.RED + "You lack permission: karma.view.others");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    return true;
                }
                int value = karmaService.getKarma(target.getUniqueId());
                Alignment a = karmaService.getAlignment(target.getUniqueId());
                sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s Karma: " + ChatColor.WHITE + value + ChatColor.GRAY + " (" + a + ")");
                return true;
            }
            case "give", "take", "set" -> {
                if (!sender.hasPermission("karma.admin")) {
                    sender.sendMessage(ChatColor.RED + "You lack permission: karma.admin");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + sub + " <player> <amount>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    return true;
                }
                int amount;
                try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Amount must be an integer");
                    return true;
                }
                switch (sub) {
                    case "give" -> karmaService.addKarma(target.getUniqueId(), amount);
                    case "take" -> karmaService.addKarma(target.getUniqueId(), -amount);
                    case "set" -> karmaService.setKarma(target.getUniqueId(), amount);
                }
                int newValue = karmaService.getKarma(target.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Updated " + (target.getName() != null ? target.getName() : target.getUniqueId()) + " to " + newValue);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("karma.admin")) {
                    sender.sendMessage(ChatColor.RED + "You lack permission: karma.admin");
                    return true;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin instanceof org.gir0fa.karmaPlugin.KarmaPlugin kp) {
                        kp.reloadPlugin();
                    } else {
                        plugin.reloadConfig();
                    }
                });
                sender.sendMessage(ChatColor.GREEN + "KarmaSMP reloaded.");
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " get <player> | give/take/set <player> <amount> | reload");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("get"));
            if (sender.hasPermission("karma.admin")) subs.addAll(List.of("give", "take", "set", "reload"));
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).sorted().collect(Collectors.toList());
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            return List.of("10", "20", "-10", "50");
        }
        return Collections.emptyList();
    }
}


