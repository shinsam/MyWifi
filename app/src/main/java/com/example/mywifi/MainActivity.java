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

/**
 * MainActivity는 Android 애플리케이션의 메인 액티비티입니다.
 * 이 클래스는 간단한 서버-클라이언트 통신을 구현합니다.
 */
public class MainActivity extends AppCompatActivity {

    // UI 요소들
    private TextView tvMyIpAddress; // 디바이스의 IP 주소를 표시하는 TextView
    private EditText etServerIp, etMessage; // 서버 IP와 메시지를 입력하는 EditText
    private Button btnConnect, btnSend; // 연결과 메시지 전송을 위한 버튼
    private TextView tvMessages; // 수신된 메시지를 표시하는 TextView

    // Socket 통신을 위한 변수들
    private ServerSocket serverSocket; // 서버 소켓
    private Socket clientSocket; // 클라이언트 소켓
    private PrintWriter out; // 출력 스트림
    private BufferedReader in; // 입력 스트림

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI 요소 초기화
        tvMyIpAddress = findViewById(R.id.tvMyIpAddress);
        etServerIp = findViewById(R.id.etServerIp);
        etMessage = findViewById(R.id.etMessage);
        btnConnect = findViewById(R.id.btnConnect);
        btnSend = findViewById(R.id.btnSend);
        tvMessages = findViewById(R.id.tvMessages);

        // tvMessages에 ScrollingMovementMethod 설정
        tvMessages.setMovementMethod(new ScrollingMovementMethod());

        // 디바이스의 IP 주소를 가져와서 표시
        String myIpAddress = getIpAddress();
        tvMyIpAddress.setText("My IP Address: " + myIpAddress);

        // 서버 소켓을 시작하는 스레드 실행
        new Thread(new ServerThread()).start();

        // btnConnect 버튼 클릭 시 서버에 연결
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

        // btnSend 버튼 클릭 시 메시지 전송
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

        // etMessage에서 엔터 키를 누르면 메시지 전송
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            String message = etMessage.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
                etMessage.setText("");
            }
            return true;
        });
    }

    /**
     * 디바이스의 IP 주소를 가져오는 메소드
     * @return 디바이스의 IP 주소
     */
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

    /**
     * 메시지를 서버에 전송하는 메소드
     * @param message 전송할 메시지
     */
    private void sendMessage(String message) {
        runOnUiThread(() -> tvMessages.append("\nMe: " + message)); // 전송된 메시지를 화면에 표시

        new Thread(() -> {
            if (out != null) {
                out.println(message);
                out.flush();
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Not connected to server", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * 서버 스레드 클래스
     * 클라이언트 연결을 수락하고 데이터를 수신합니다.
     */
    class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                // 서버 소켓을 포트 8888에서 생성합니다.
                serverSocket = new ServerSocket(8888);
                // 클라이언트의 연결을 기다리고, 연결이 수락되면 클라이언트 소켓을 초기화합니다.
                clientSocket = serverSocket.accept();
                // 클라이언트로부터 데이터를 읽기 위한 BufferedReader를 초기화합니다.
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // 클라이언트로 데이터를 보내기 위한 PrintWriter를 초기화합니다.
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // UI 스레드에서 토스트 메시지를 띄워 클라이언트가 연결되었음을 사용자에게 알립니다.
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Client connected", Toast.LENGTH_SHORT).show());

                String message;
                // 클라이언트로부터 메시지를 계속해서 읽고 화면에 표시합니다.
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    // UI 스레드에서 수신된 메시지를 tvMessages에 추가합니다.
                    runOnUiThread(() -> tvMessages.append("\nClient: " + finalMessage));
                }
            } catch (Exception e) {
                // 예외가 발생하면 스택 트레이스를 출력하고, UI 스레드에서 연결 재시도를 알리는 메시지를 표시합니다.
                e.printStackTrace();
                runOnUiThread(() -> tvMessages.append("\n다시 연결해주세요"));
            }
        }
    }

    /**
     * 클라이언트 스레드 클래스
     * 서버에 연결하고 데이터를 송수신합니다.
     */
    class ClientThread implements Runnable {
        private String serverIp; // 서버의 IP 주소를 저장하는 변수

        ClientThread(String serverIp) {
            this.serverIp = serverIp; // 생성자에서 서버 IP 주소를 초기화
        }

        @Override
        public void run() {
            try {
                // 서버 IP 주소와 포트 번호를 사용하여 클라이언트 소켓을 생성
                clientSocket = new Socket(serverIp, 8888);
                // 서버로부터 데이터를 읽기 위한 BufferedReader를 초기화
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // 서버로 데이터를 보내기 위한 PrintWriter를 초기화
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // UI 스레드에서 토스트 메시지를 띄워 서버에 연결되었음을 사용자에게 알림
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show());

                String message;
                // 서버로부터 메시지를 계속해서 읽고 화면에 표시
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    // UI 스레드에서 수신된 메시지를 tvMessages에 추가
                    runOnUiThread(() -> tvMessages.append("\nServer: " + finalMessage));
                }
            } catch (Exception e) {
                // 예외가 발생하면 스택 트레이스를 출력
                e.printStackTrace();
            }
        }
    }
}