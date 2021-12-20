package SomNetworkCord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.util.ArrayList;
import java.util.List;

public final class DiscordBot {
    private static boolean Enabled = false;
    static DiscordClient client;
    static GatewayDiscordClient gateway;

    public DiscordBot() {
    }

    static boolean isEnabled() {
        return Enabled;
    }

    public static void BotStart() {
        System.Log("DiscordBot Start");
        Enabled = true;
        client = DiscordClient.create(System.DiscordBotToken);
        gateway = client.login().block();
        gateway.on(MessageCreateEvent.class).subscribe((event) -> {
            Message message = event.getMessage();
            if (message.getChannelId().asLong() == System.DiscordChatChannel) {
                User user = message.getAuthor().get();
                String UserName = user.getUsername();
                if (!user.isBot()) {
                    List<PacketChat> packetChatList = new ArrayList<>();
                    packetChatList.add(new PacketChat("§b[Discord]§r "));
                    packetChatList.add(new PacketChat(UserName));
                    packetChatList.add(new PacketChat("§a:§r "));
                    String[] data = message.getContent().split("$end");
                    for (String str : data) {
                        String[] split = str.split("$tag");
                        packetChatList.add(new PacketChat(split));
                    }
                    String[] Json = new String[16];
                    int i = 0;
                    for (PacketChat packet : packetChatList) {
                        Json[i] = System.gson.toJson(packet);
                        i++;
                    }
                    Packet packet = new Packet(PacketType.CHAT, Json);
                    Server.sendSNC(packet);
                }
            }

        });
    }

    static void linkRegister() {
    }

    static void send(String message) {
        if (isEnabled()) {
            client.getChannelById(Snowflake.of(System.DiscordChatChannel)).createMessage(message).subscribe();
        }

    }

    static void disconnect() {
        gateway.onDisconnect().subscribe();
    }
}
