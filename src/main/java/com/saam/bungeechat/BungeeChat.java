package com.saam.bungeechat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;


public class BungeeChat extends Plugin implements Listener {
    private File file;
    private ConfigurationProvider cfgProvider;
    private Configuration config;

    public BungeeChat() {

    }

    @Override
    public void onEnable() {
        System.out.println("Bungee Chat enabled");
        getProxy().getPluginManager().registerListener(this, this);

        //Create Config FIle
        cfgProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            file = new File(getDataFolder().getPath(), "config.yml");
            if (!file.exists()) {
                file.createNewFile();
                config = cfgProvider.load(this.getClass().getClassLoader().getResourceAsStream("config.yml"));
                cfgProvider.save(config, file);
            }

            config = cfgProvider.load(file);
            cfgProvider.save(config, file);

        } catch (IOException ignored) {
        }
    }

    @Override
    public void onDisable() {
        System.out.println("Bungee Chat disabled");
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();

        String msgPrefix = config.getString("msg.prefix");
        Boolean msgEnabled = config.getBoolean("msg.enable");

        String staffPrefix = config.getString("staffChat.prefix");
        Boolean staffEnabled = config.getBoolean("staffChat.enable");

        String globalPrefix = config.getString("userChat.prefix");
        Boolean globalEnabled = config.getBoolean("userChat.enable");


        if (message.startsWith(msgPrefix) && msgEnabled) {
            e.setCancelled(true);
            String[] words = message.split(" ");
            ProxiedPlayer receiver = getProxy().getPlayer(words[1]);
            if (!getProxy().getPlayers().contains(receiver)) return;

            String realMsg = message.replace(msgPrefix + " " + words[1], "");

            String messageRec = "&7[&a" + player + "&7]->&7[&aYou&7] &r" + realMsg;
            String messageSend = "&7[&aYou&7]->&7[&a" + player + "&7] &r" + realMsg;
            receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', messageRec));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSend));
        }

        if (message.startsWith(staffPrefix) && staffEnabled) {
            if (player.hasPermission("bungeechat.staff")) {
                e.setCancelled(true);
                String newMsg = ChatColor.translateAlternateColorCodes('&', "&7[&bStaff&7] &7 " + player + " > " + message.replace(staffPrefix, ""));
                for (ProxiedPlayer online : getProxy().getPlayers()) {
                    if (player.hasPermission("bungeechat.staff")) {
                        online.sendMessage(newMsg);
                    }
                }
            }
        }

        if (message.startsWith(globalPrefix) && globalEnabled) {
            if (player.hasPermission("bungeechat.user")) {
                e.setCancelled(true);
                String newMsg = ChatColor.translateAlternateColorCodes('&', "&7[&cGlobal&7] &7 " + player + " > " + message.replace(globalPrefix, ""));
                for (ProxiedPlayer online : getProxy().getPlayers()) {
                    if (online.hasPermission("bungeechat.user")) {
                        online.sendMessage(newMsg);
                    }
                }
            }
        }
    }
}
