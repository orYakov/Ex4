package com.example.ex4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    TcpClient mTcpClient;

    // 'ocCreate' implementation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // the function of the connect button
    public void onClick(View v) {
        // extract the ip and port values from the input boxes
        String ip = ((EditText) findViewById(R.id.ip_enter_box)).getText().toString();
        if (ip.equals("")) {
            ip = "10.0.2.2";
        }
        String portStr = ((EditText) findViewById(R.id.port_enter_box)).getText().toString();
        if (portStr.equals("")) {
            portStr = "5402";
        }
        int port = Integer.parseInt(portStr);

        // initiate the Tcp client
        TcpClient tcpClient = TcpClient.getInstance();
        tcpClient.setSERVER_IP(ip);
        tcpClient.setSERVER_PORT(port);
        // start the connection
        ConnectTask connectTask = new ConnectTask(tcpClient);
        connectTask.execute();
        // show the joystick
        Intent intent = new Intent(this, joystick.class);
        startActivity(intent);
//        tcpClient.sendMessage("qqqqqqqqqqqq\n");
//        tcpClient.sendMessage("blablabla\n");
//        tcpClient.sendMessage("kkkkkkkkkkkkk\n");
    }

}
