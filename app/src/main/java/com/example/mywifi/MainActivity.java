
package com.example.mywifi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvMyIpAddress;
    private EditText etServerIp, etMessage;
    private Button btnConnect, btnSend;
    private TextView tvMessages;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMyIpAddress = findViewById(R.id.tvMyIpAddress);
        etServerIp = findViewById(R.id.etServerIp);
        etMessage = findViewById(R.id.etMessage);
        btnConnect = findViewById(R.id.btnConnect);
        btnSend = findViewById(R.id.btnSend);

        tvMessages = findViewById(R.id.tvMessages);
        // tvMessages에 ScrollingMovementMethod 설정
        tvMessages.setMovementMethod(new ScrollingMovementMethod());

        // 내용이 추가될 때마다 ScrollView를 하단으로 스크롤
//        tvMessages.addTextChangedListener(new android.text.TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(android.text.Editable s) {
//                tvMessages.post(() -> tvMessages.scrollTo(0, tvMessages.getBottom()));
//            }
//        });

        //키패드가 차지한 부분을 화면에 보여주기 위해 ScrollView를 최하단으로 스크롤
        //tvMessages.post(() -> tvMessages.scrollTo(0, tvMessages.getBottom()));

        // Display device IP address
        // Display device IP address
        String myIpAddress = getIpAddress();
        tvMyIpAddress.setText("My IP Address: " + myIpAddress);

        // Start server socket
        new Thread(new ServerThread()).start();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIp = etServerIp.getText().toString();
                if (!TextUtils.isEmpty(serverIp)) {
                    new Thread(new ClientThread(serverIp)).start();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter server IP address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(message);
                    etMessage.setText("");
                }
            }
        });

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            String message = etMessage.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
                etMessage.setText("");
            }
            return true;
        });
    }

    private String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : interfaces) {
                List<InetAddress> addresses = Collections.list(ni.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to get IP address";
    }


    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "IP not found";
    }


    private void sendMessage(String message) {
        runOnUiThread(() -> tvMessages.append("\nMe: " + message)); // Display the sent message on the screen

        new Thread(() -> {
            if (out != null) {
                out.println(message);
                out.flush();
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Not connected to server", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Client connected", Toast.LENGTH_SHORT).show());

                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    runOnUiThread(() -> tvMessages.append("\nClient: " + finalMessage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvMessages.append("\n다시 연결해주세요"));
            }
        }
    }

    class ClientThread implements Runnable {
        private String serverIp;

        ClientThread(String serverIp) {
            this.serverIp = serverIp;
        }

        @Override
        public void run() {
            try {
                clientSocket = new Socket(serverIp, 8888);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show());

                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    runOnUiThread(() -> tvMessages.append("\nServer: " + finalMessage));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}