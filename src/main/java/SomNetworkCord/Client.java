package SomNetworkCord;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static SomNetworkCord.PacketChat.PacketChatLine;
import static SomNetworkCord.System.Log;

public class Client implements Listener {
    public Client() {
    }

    static boolean connect;

    static void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(System.plugin, () -> {
            sendSNC(new Packet(PacketType.CHECK));
        }, 1L, 10L);
    }

    static void sendSNC(Packet packet) {
        Bukkit.getScheduler().runTaskAsynchronously(System.plugin, () -> {
            try {
                Socket socket = new Socket(System.IP, System.SNC_Port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(packet);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                List<Packet> inPacket;
                if ((inPacket = (List<Packet>) in.readObject()) != null) {
                    for (Packet packet1 : inPacket) {
                        receive(packet1);
                    }
                }

                in.close();
                out.close();
                socket.close();

                if (packet.isCheck() && !connect) {
                    connect = true;
                    sendSNC(new Packet(PacketType.CHAT, PacketChatLine("§b[" + System.ID + "]§r Connected SNC")));
                }
            } catch (ClassNotFoundException | IOException e) {
                if (packet.isCheck() && connect) {
                    connect = false;
                    Log("Disconnected SNC");
                }
            }

        });
    }

    static void receive(Packet packet) {
        if (packet.getType() == PacketType.CHAT.getID()) {
            TextComponent message = new TextComponent();
            StringBuilder line = new StringBuilder();
            for (String json : packet.getJson()) {
                if (json != null) {
                    PacketChat chatPacket = System.gson.fromJson(json, PacketChat.class);
                    TextComponent text = new TextComponent(chatPacket.message);
                    line.append(chatPacket.message);
                    if (chatPacket.hoverText != null) {
                        List<TextComponent> components = new ArrayList<>();
                        String[] str = chatPacket.hoverText.split("<nl>");
                        TextComponent hoverText = new TextComponent();
                        TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
                        for (int i  = 0; i < str.length; i++) {
                            hoverText.addExtra(new TextComponent(str[i]));
                            if (i < str.length-1)
                            hoverText.addExtra(newLine);
                        }
                        components.add(hoverText);
                        text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, components.toArray(new BaseComponent[components.size()])));
                    }
                    if (chatPacket.runCommand != null) {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, chatPacket.runCommand));
                    }
                    message.addExtra(text);
                }
            }

            Bukkit.getLogger().info(line.toString());
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(message);
            }
        }

    }
}
