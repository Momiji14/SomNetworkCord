package SomNetworkCord;

import org.bukkit.Bukkit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                List<String> Target = packet.getTarget();
                                if (bool) {
                                    for (Map.Entry<String, List<Packet>> packets : BufferPacket.entrySet()) {
                                        if (Target.contains("All") || Target.contains(packets.getKey())) {
                                            packets.getValue().add(packet);
                                        }
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
                            System.Log("SNC-Server Input Failed -> " + socket.getInetAddress() + "/" + socket.getPort());
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
        Client.receive(packet);
    }

    static boolean receive(Packet packet) {
        if (packet.getType() == PacketType.CHAT.getID()) {
            Client.receive(packet);
            StringBuilder line = new StringBuilder();
            try {
                for (String json : packet.getJson()) {
                    if (json != null) {
                        PacketChat chatPacket = System.gson.fromJson(json, PacketChat.class);
                        line.append(chatPacket.message);
                    }
                }
            } catch (Exception ignored) {}
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
