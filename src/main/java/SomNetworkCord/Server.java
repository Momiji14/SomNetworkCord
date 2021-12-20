package SomNetworkCord;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;

import static SomNetworkCord.PacketChat.PacketChatLine;

public class Server {
    private static ServerSocket SNC_ServerSocket;
    private static final HashMap<String, List<Packet>> BufferPacket = new HashMap();

    public Server() {
    }

    static void start() {
        Bukkit.getScheduler().runTaskAsynchronously(System.plugin, () -> {
            try {
                SNC_ServerSocket = new ServerSocket(System.SNC_Port);
                System.Log("SNC-Server Start Successful");

                while(true) {
                    Socket socket = SNC_ServerSocket.accept();
                    Bukkit.getScheduler().runTaskAsynchronously(System.plugin, () -> {
                        try {
                            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            Packet packet = (Packet)in.readObject();
                            String ID = packet.getID();
                            BufferPacket.putIfAbsent(ID, new ArrayList());
                            if (!packet.isCheck()) {
                                boolean bool = receive(packet);
                                if (bool) {
                                    for (List<Packet> packets : BufferPacket.values()) {
                                        packets.add(packet);
                                    }
                                }
                            }

                            if (BufferPacket.containsKey(ID) && socket.getOutputStream() != null) {
                                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                                out.writeObject(BufferPacket.get(ID));
                                BufferPacket.get(ID).clear();
                                out.close();
                            }

                            in.close();
                            socket.close();
                        } catch (ClassNotFoundException | IOException var7) {
                            System.Log("SNC-Server Input Failed -> " + socket.getInetAddress());
                        }

                    });
                }
            } catch (IOException var1) {
                System.Log("SNC-Server Start Failed");
            }
        });
    }

    static void sendSNC(Packet packet) {
        if (BufferPacket.size() > 0) {
            for (List<Packet> packets : BufferPacket.values()) {
                packets.add(packet);
            }
        }

    }

    static boolean receive(Packet packet) {
        if (packet.getType() == PacketType.CHAT.getID()) {
            Client.receive(packet);
            StringBuilder line = new StringBuilder();
            for (String json : packet.getJson()) {
                if (json != null) {
                    PacketChat chatPacket = System.gson.fromJson(json, PacketChat.class);
                    line.append(chatPacket.message);
                }
            }
            if (DiscordBot.isEnabled()) {
                DiscordBot.send("**" + Function.unColored(line.toString()) + "**");
            }

            return true;
        } else {
            return false;
        }
    }

    static void close() {
        try {
            sendSNC(new Packet(PacketType.CHAT, PacketChatLine("SNC-Server Restart...")));
            if (SNC_ServerSocket != null) {
                SNC_ServerSocket.close();
            }

            System.Log("SNC-Server Close Successful");
        } catch (IOException var1) {
            System.Log("SNC-Server Close Failed");
        }

    }
}
