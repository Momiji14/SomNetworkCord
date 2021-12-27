package SomNetworkCord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static SomNetworkCord.PacketChat.PacketChatLine;

public final class System extends JavaPlugin implements Listener, PluginMessageListener {
    static Plugin plugin;
    static Gson gson = new Gson();
    static FileConfiguration config;
    static String OperationMode = "Client";
    static String ID;
    static String IP;
    static int SNC_Port;
    static int Web_Port;
    static String DiscordBotToken = "";
    static long DiscordChatChannel = -1L;
    static String ResourcePack = "";

    public System() {
    }

    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();
        plugin = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        this.initialize();
        this.SocketStart();
    }

    public void onDisable() {
        this.initialize();
        if (OperationMode.equalsIgnoreCase("Server")) {
            Server.close();
            WebServer.close();
            if (DiscordBot.isEnabled()) {
                DiscordBot.disconnect();
            }
        }

    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    }

    public void TeleportServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    void initialize() {
        this.reloadConfig();
        config.options().copyDefaults(true);
        OperationMode = config.getString("OperationMode");
        ID = config.getString("ID");
        IP = config.getString("IP");
        SNC_Port = config.getInt("SNC-Port");
        Web_Port = config.getInt("Web-Port");
        DiscordBotToken = config.getString("DiscordBotToken");
        DiscordChatChannel = config.getLong("DiscordChatChannel");
        ResourcePack = config.getString("ResourcePack");
        this.saveConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("SomNetworkChat") && args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                this.initialize();
                return true;
            }

            if (args[0].equalsIgnoreCase("sendSNC")) {
                if (args.length >= 2) {
                    Packet packet = new Packet(PacketType.CHAT, PacketChatLine(args[1]));
                    if (args.length >= 3) {
                        String[] split = args[2].split(",");
                        packet.setTarget(Arrays.asList(split));
                    }
                    if (OperationMode.equalsIgnoreCase("Client")) {
                        Client.sendSNC(packet);
                    } else if (OperationMode.equalsIgnoreCase("Server")) {
                        Server.sendSNC(packet);
                    }

                    return true;
                }

                this.senderMessage(sender, "/snc sendSNC [String]");
            } else {
                this.senderMessage(sender, "/snc [sendSNC/sendWeb]");
            }
        }

        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("Hub")) {
                this.TeleportServer(player, "Lobby");
                return true;
            }

            if (cmd.getName().equalsIgnoreCase("ResourcePack")) {
                if (!ResourcePack.equals("")) {
                    player.setResourcePack(ResourcePack);
                } else {
                    player.sendMessage("§b[SNC]§r §eリソースパックが設定されていません");
                }

                return true;
            }
        }

        return false;
    }

    void senderMessage(CommandSender sender, String message) {
        Player player = (Player)sender;
        if (player != null) {
            player.sendMessage(message);
        }

    }

    void SocketStart() {
        OperationMode = config.getString("OperationMode", "Client");
        if (OperationMode.equalsIgnoreCase("Client")) {
            Client.start();
        } else if (OperationMode.equalsIgnoreCase("Server")) {
            Server.start();
            WebServer.start();
            if (!DiscordBotToken.equalsIgnoreCase("")) {
                DiscordBot.BotStart();
            } else {
                Log("DiscordBot Disabled");
            }
        } else {
            Log("OperationModeが不正です [Server/Client]");
        }

    }

    public static void Log(String log) {
        Log(log, "SNC");
    }

    public static void Log(String log, String form) {
        Bukkit.getServer().getLogger().info("§b[" + form + "]§r " + log);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§b[" + form + "]§r " + log);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        String msg = event.getMessage();
        if (!msg.contains("$cancel")) {
            List<PacketChat> message = new ArrayList<>();
            message.add(new PacketChat("§b[" + ID + "]§r "));
            message.add(new PacketChat(event.getPlayer().getDisplayName()));
            message.add(new PacketChat("§a:§r "));
            String[] data = msg.split("<end>");
            for (String str : data) {
                String[] split = str.split("<tag>");
                PacketChat packetChat = new PacketChat(split);
                message.add(packetChat);
            }
            String[] Json = new String[16];
            int i = 0;
            for (PacketChat packet : message) {
                Json[i] = gson.toJson(packet);
                i++;
            }
            Packet packet = new Packet(PacketType.CHAT, Json);
            if (OperationMode.equalsIgnoreCase("Client")) {
                Client.sendSNC(packet);
            } else if (OperationMode.equalsIgnoreCase("Server")) {
                Server.sendSNC(packet);
            } else {
                Log("OperationModeが不正です [Server/Client]");
            }
        }
    }
}
