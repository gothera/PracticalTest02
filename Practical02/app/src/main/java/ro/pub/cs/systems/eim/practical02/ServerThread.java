package ro.pub.cs.systems.eim.practical02;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class ServerThread extends Thread {

    private int port = 0;
    private ServerSocket serverSocket = null;

    private HashMap<String, Integer> data = null;

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        this.data = new HashMap<>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setData(String key, int x) {
        this.data.put(key, x);
    }

    public synchronized HashMap<String, Integer> getData() {
        return data;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                BufferedReader bufferedReader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                if (bufferedReader == null || printWriter == null) {
                    Log.e(Constants.TAG, "[SERVER THREAD] Buffered Reader / Print Writer are null!");
                    return;
                }

                String key = bufferedReader.readLine();
                Log.i(Constants.TAG, "Got key from client " + key);
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("https://api.dictionaryapi.dev/api/v2/entries/en/"+key);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                if (pageSourceCode == null) {
                    Log.d("[SERVER THREAD]", "Got no words from api");
                }
                JSONArray res = new JSONArray(pageSourceCode);
                JSONObject jsonObject = res.getJSONObject(0);
                JSONArray meanings = jsonObject.getJSONArray("meanings");
                JSONObject jsonObject2 = meanings.getJSONObject(0);
                Log.i("[server thread final obj]", jsonObject2.toString());
                JSONArray definitions = jsonObject2.getJSONArray("definitions");
                JSONObject defObject = definitions.getJSONObject(0);
                Log.i("[server thread final obj]", defObject.toString());
                String finalDef = defObject.getString("definition");

                printWriter.println(finalDef);
                printWriter.flush();

//                JSONArray definitions = jsonArray.getJSONArray("definitions");
//                String finalResponse = definitions.getString()
                socket.close();

            }
        }  catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
