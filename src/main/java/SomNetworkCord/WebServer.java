package SomNetworkCord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.bukkit.Bukkit;

public class WebServer {
    private static ServerSocket serverSocket;
    private static final Socket[] socket = new Socket[32];
    private static final OutputStream[] os = new OutputStream[32];

    public WebServer() {
    }

    static void start() {
        Bukkit.getScheduler().runTaskAsynchronously(System.plugin, () -> {
            int n = 0;

            try {
                serverSocket = new ServerSocket(System.Web_Port);
                System.Log("Web-Server Start Successful", "Web");

                while(true) {
                    socket[n] = serverSocket.accept();
                    socket[n].setKeepAlive(true);
                    System.Log("Connected -> " + socket[n].getInetAddress());
                    InputStream is = socket[n].getInputStream();
                    os[n] = socket[n].getOutputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    handShake(in, os[n]);
                    int finalN = n;
                    Bukkit.getScheduler().runTaskAsynchronously(System.plugin, () -> {
                        while(true) {
                            try {
                                byte[] buff = new byte[1024];
                                int lineData = is.read(buff);

                                for(int ix = 0; ix < lineData - 6; ++ix) {
                                    buff[ix + 6] ^= buff[ix % 4 + 2];
                                }

                                String line = new String(buff, 6, lineData - 6, StandardCharsets.UTF_8);
                                byte[] sendHead = new byte[]{buff[0], (byte)line.getBytes(StandardCharsets.UTF_8).length};

                                for(int i = 0; i < 32; ++i) {
                                    if (socket[i].isConnected()) {
                                        os[i].write(sendHead);
                                        os[i].write(line.getBytes(StandardCharsets.UTF_8));
                                    }
                                }

                                System.Log(line + " -> " + socket[finalN].getInetAddress() + ", " + socket[finalN].getPort());
                                if (!line.equals("bye")) {
                                    continue;
                                }
                            } catch (IOException var7) {
                                System.Log("Echo Error", "Web");
                            }

                            return;
                        }
                    });
                    ++n;
                }
            } catch (IOException var4) {
                System.Log("Web-Server Start Failed", "Web");
            }
        });
    }

    static void handShake(BufferedReader in, OutputStream os) {
        String key = "";

        try {
            String header;
            while(!(header = in.readLine()).equals("")) {
                System.Log(header, "Web");
                String[] spLine = header.split(":");
                if (spLine[0].equals("Sec-WebSocket-Key")) {
                    key = spLine[1].trim();
                }
            }

            key = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] keyUtf8 = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] keySha1 = md.digest(keyUtf8);
            Encoder encoder = Base64.getEncoder();
            byte[] keyBase64 = encoder.encode(keySha1);
            String keyNext = new String(keyBase64);
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: " + keyNext + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            os.write(response);
        } catch (NoSuchAlgorithmException | IOException var11) {
        }

    }

    static void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            System.Log("Web-Server Close Successful", "Web");
        } catch (IOException var1) {
            System.Log("Web-Server Close Failed", "Web");
        }

    }
}
