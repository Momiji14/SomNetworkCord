package SomNetworkCord;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

enum PacketType {
    CHECK(0),
    CHAT(1);

    private final int id;

    PacketType(int id) {
        this.id = id;
    }

    int getID() {
        return this.id;
    }
}

public class Packet implements Serializable {
    private final int type;
    private String ID;
    private List<String> Target = Collections.singletonList("All");
    private String[] Json;

    Packet(PacketType type) {
        this.ID = System.ID;
        this.type = type.getID();
    }

    Packet(PacketType type, String[] Json) {
        this.ID = System.ID;
        this.type = type.getID();
        this.Json = Json;
    }

    boolean isCheck() {
        return this.type == PacketType.CHECK.getID();
    }

    int getType() {
        return this.type;
    }

    String[] getJson() {
        return this.Json;
    }

    String getID() {
        return this.ID;
    }

    void setID(String ID) {
        this.ID = ID;
    }

    void setTarget(List<String> Target) {
        this.Target = Target;
    }

    List<String> getTarget() {
        return this.Target;
    }
}

class PacketChat {
    String message;
    String hoverText;
    String runCommand;

    PacketChat(String[] message) {
        if (message.length >= 1) {
            this.message = message[0];
        }

        if (message.length >= 2) {
            this.hoverText = message[1];
        }

        if (message.length >= 3) {
            this.runCommand = message[2];
        }

    }

    PacketChat(String message) {
        this.message = message;
    }

    PacketChat(String message, String hoverText) {
        this.message = message;
        this.hoverText = hoverText;
    }

    PacketChat(String message, String hoverText, String runCommand) {
        this.message = message;
        this.hoverText = hoverText;
        this.runCommand = runCommand;
    }

    String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static PacketChat fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, PacketChat.class);
    }

    static String[] PacketChatLine(String str) {
        PacketChat chatPacket = new PacketChat(str);
        String[] json = new String[1];
        json[0] = chatPacket.toJson();
        return json;
    }
}