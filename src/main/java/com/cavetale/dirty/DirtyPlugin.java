package com.cavetale.dirty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class DirtyPlugin extends JavaPlugin implements Listener {
    private static final List<String> COMMANDS = Arrays.asList("item", "block", "entity", "cancel");
    private final HashMap<UUID, EnumSet<CommandOption>> blockTool = new HashMap<>();
    private final HashMap<UUID, EnumSet<CommandOption>> entityTool = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    enum CommandOption {
        CONSOLE, PRETTY;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return false;
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            getLogger().info("Player expected");
            return true;
        }
        if (!COMMANDS.contains(args[0])) return false;
        EnumSet<CommandOption> options = EnumSet.noneOf(CommandOption.class);
        for (int i = 1; i < args.length; i += 1) {
            try {
                options.add(CommandOption.valueOf(args[i].toUpperCase()));
            } catch (IllegalArgumentException iae) {
                player.sendMessage("Illegal option: " + args[i]);
            }
        }
        switch (args[0]) {
        case "item": {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null) item = new ItemStack(Material.AIR);
            printTag(player, "Item TAG of " + item.getType() + ": ",
                     Dirty.getItemTag(item), options);
            return true;
        }
        case "block": {
            blockTool.put(player.getUniqueId(), options);
            player.sendMessage("Now right click a block.");
            return true;
        }
        case "entity": {
            entityTool.put(player.getUniqueId(), options);
            player.sendMessage("Now right click an entity.");
            return true;
        }
        case "cancel": {
            if (null == blockTool.remove(player.getUniqueId())
                && null == entityTool.remove(player.getUniqueId())) {
                player.sendMessage("You had not tool active.");
            } else {
                player.sendMessage("Lookup tool cancelled.");
            }
            return true;
        }
        default: return false;
        }
    }

    private void printTag(Player player, String prefix, Object tag,
                          EnumSet<CommandOption> options) {
        GsonBuilder builder = new GsonBuilder()
            .disableHtmlEscaping();
        if (options.contains(CommandOption.PRETTY)) {
            builder.setPrettyPrinting();
        }
        Gson gson = builder.create();
        String json = gson.toJson(tag);
        player.sendMessage(prefix + json);
        if (options.contains(CommandOption.CONSOLE)) {
            getLogger().info(json);
            player.sendMessage("Also printed to console.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1) {
            return COMMANDS.stream().filter(i -> i.startsWith(args[0]))
                .collect(Collectors.toList());
        }
        if (args.length > 1 && COMMANDS.contains(args[0]) && !"cancel".equals(args[0])) {
            return Arrays.stream(CommandOption.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .filter(i -> i.startsWith(args[args.length - 1]))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EnumSet<CommandOption> options = blockTool.remove(player.getUniqueId());
        if (options == null) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        event.setCancelled(true);
        printTag(player, "Block TAG of " + block.getBlockData().getAsString() + ": ",
                 Dirty.getBlockTag(block), options);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EnumSet<CommandOption> options = entityTool.remove(player.getUniqueId());
        if (options == null) return;
        Entity entity = event.getRightClicked();
        event.setCancelled(true);
        printTag(player, "Entity TAG of " + entity.getType() + ": ",
                 Dirty.getEntityTag(entity), options);
    }
}
