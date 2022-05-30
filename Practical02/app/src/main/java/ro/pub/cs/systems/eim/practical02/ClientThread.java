package ro.pub.cs.systems.eim.practical02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientThread extends Thread {

    private String address;
    private int port;
    private String key;
    private TextView responseTextView;

    private Socket socket;

    public ClientThread(String address, int port, String key, TextView responseTextView) {
        this.address = address;
        this.port = port;
        this.key = key;
        this.responseTextView = responseTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            Log.i("[CLIENT THREAD]", "address and portt " + address + " " + port);

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                return;
            }
            printWriter.println(key);
            printWriter.flush();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i("[CLIENT THREAD]", "Got line from server " + line);
                String value = String.valueOf(line);
                responseTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        responseTextView.setText(value);
                    }
                });
            }
        } catch (IOException ioException) {
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
