package project.iotdom;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.ClientSocket;
import project.iotdom.connection.MessageProvider;
import project.iotdom.connection.MessageReceiver;
import project.iotdom.crypt.AES;
import project.iotdom.crypt.RSA;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.ChallPacket;
import project.iotdom.packets.KeyPacket;
import project.iotdom.packets.LogPacket;

public class LoginActivity extends AppCompatActivity {

    private Handler handler;
    private Button sendButton;
    private EditText loginField;
    private EditText passwordField;
    private ProgressBar bar;
    final Context context = this;
    private InetAddress adress = null;
    private int portNumber;
    private Thread loginThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handler = new Handler(context.getMainLooper());
        sendButton = (Button)findViewById(R.id.loginButton);
        loginField = (EditText)findViewById(R.id.loginEntry);
        passwordField = (EditText)findViewById(R.id.passwordEntry);
        bar = (ProgressBar)findViewById(R.id.progressBar);
        sendButton.setEnabled(false);

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (loginField.getText().length() > 0 && s.length() > 0) {
                    sendButton.setEnabled(true);
                }
                else
                    sendButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        loginField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordField.getText().length() > 0 && s.length() > 0) {
                    sendButton.setEnabled(true);
                }
                else
                    sendButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        sendButton.setOnClickListener(v -> {
            boolean result = readServerConfiguration();
            if (result) {
                if (loginField.getText().length() > 30 || passwordField.getText().length() > 30) {
                    showAlert(getResources().getString(R.string.logInfoError),"Błąd długości");
                    return;
                }
                loginThread = new Thread(new LoginTask(loginField.getText().toString(),passwordField.getText().toString()));
                loginThread.start();
            }
        });
    }

    private void onLogging() {
        bar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
    }

    private void afterLogin() {
        bar.setVisibility(View.INVISIBLE);
        sendButton.setEnabled(true);
    }

    private void showAlert(String message, String title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean readServerConfiguration() {
        String ipAdress = "";
        String port = "";
        try {
            InputStream is = getAssets().open("servConfig.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            ipAdress = bf.readLine();
            port = bf.readLine();
            bf.close();
        } catch (Exception e) {
            showAlert(getResources().getString(R.string.servConfError),"Błąd konfiguracji");
            return false;
        }
        try {
            adress = InetAddress.getByName(ipAdress);
        } catch (Exception e) {
            showAlert(getResources().getString(R.string.inetAdressError), "Niepoprawny adres serwera");
            return false;
        }
        try {
            portNumber = Integer.parseInt(port);
        } catch (Exception e) {
            showAlert(getResources().getString(R.string.portError),"Niepoprawny numer portu");
            return false;
        }
        return true;
    }

    class LoginTask implements Runnable {

        ClientSocket clientSocket;
        private static final int challRespLength = 4 + 1 + 257;
        private static final int ackRespLength = 4 + 1 + 32;
        String login, password;

        public LoginTask(String login, String password) {
            this.login = login;
            this.password = password;
        }


        //todo
        @Override
        public void run() {
            handler.post(() -> onLogging());
            clientSocket = new ClientSocket(adress,portNumber);
            try {
                clientSocket.connect();
            } catch (Exception e) {
                if (e instanceof SocketTimeoutException) {
                    cleanAfterError(getResources().getString(R.string.connectionTimeout),"Przekroczenie czasu połączenia");
                }
                else {
                    cleanAfterError(getResources().getString(R.string.connectionError),"Błąd połączenia");
                }
                return;
            }
            if (challengeExchange()) {
                KeyPacket keyPacket = new KeyPacket(AES.getInstance().getKey());
                AbstractMessage msg = MessageProvider.buildMessage(keyPacket,(byte)0);
                if (clientSocket.writeToSocket(msg.getBytes())) {
                    ByteBuffer buffer = MessageReceiver.receiveBytes(clientSocket,ackRespLength);
                    if (buffer == null) {
                        cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędne dane");
                        return;
                    }
                }
            }
            //chalenge error
            cleanAfterError(getResources().getString(R.string.servChallengeError),"Błąd serwera");
        }

        private void cleanAfterError(String message, String title) {
            clientSocket.close();
            handler.post(() -> afterLogin());
            handler.post(new AlertRun(message,title));
        }

        private LogPacket buildLogPacket() {
            LogPacket packet = new LogPacket(login,password);
            return packet;
        }

        private boolean challengeExchange() {
            AbstractPacket challenge = new ChallPacket();
            AbstractMessage msg = MessageProvider.buildMessage(challenge,(byte)0);
            if (clientSocket.writeToSocket(msg.getBytes())) {
                ByteBuffer buffer = MessageReceiver.receiveBytes(clientSocket,challRespLength);
                if (buffer == null) {
                    handler.post(new AlertRun(getResources().getString(R.string.serverResponseError),"Błędna odpowiedź"));
                    return false;
                }
                int plainLength = buffer.getInt();
                byte encryptionInfo = buffer.get();
                if (plainLength != 257 || encryptionInfo != 0x00) {
                    handler.post(new AlertRun(getResources().getString(R.string.serverResponseError),"Błędna odpowiedź"));
                    return false;
                }
                byte[] challResp = new byte[257];
                return RSA.getInstance().verifySign(challResp);
            }
            else {
                handler.post(new AlertRun(getResources().getString(R.string.writingError), "Błąd wysyłania"));
                return false;
            }
        }

        public class AlertRun implements Runnable {
            private String msg;
            private String title;

            public AlertRun(String msg, String title) {
                this.msg = msg;
                this.title = title;
            }
            @Override
            public void run() {
                showAlert(msg,title);
            }
        }
    }
}
