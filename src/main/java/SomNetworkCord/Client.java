package SomNetworkCord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import static SomNetworkCord.PacketChat.PacketChatLine;
import static SomNetworkCord.System.Log;

public class Client implements Listener {
    public Client() {
    }

    static void start() {
        sendSNC(new Packet(PacketType.CHAT, PacketChatLine("§b[" + System.ID + "]§r Connected SNC")));
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
            } catch (ClassNotFoundException | IOException e) {
                Log("sendSNC fatal");
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
                    line.append(chatPacket.message);
                    TextComponent text = new TextComponent(chatPacket.message);
                    if (chatPacket.hoverText != null) {
                        text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(chatPacket.hoverText)));
                    }

                    if (chatPacket.runCommand != null) {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, chatPacket.runCommand));
                    }

                    message.addExtra(text);
                }
            }

            Bukkit.getLogger().info(line.toString());
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }

    }
}
