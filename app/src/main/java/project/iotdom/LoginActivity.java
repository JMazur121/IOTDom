package project.iotdom;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.ClientSocket;
import project.iotdom.connection.MessageProvider;
import project.iotdom.connection.MessageReceiver;
import project.iotdom.crypt.AES;
import project.iotdom.crypt.RSA;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.ChallPacket;
import project.iotdom.packets.ChallRespPacket;
import project.iotdom.packets.KeyPacket;
import project.iotdom.packets.LogPacket;
import project.iotdom.packets.SSIDPacket;

public class LoginActivity extends AppCompatActivity {

    public static final String PASS_SSID = "SSID_RECEIVED";
    public static final String PASS_HOST = "HOST_ADDRESS";
    public static final String PASS_PORT = "PORT_NUMBER";

    private Button sendButton;
    private EditText loginField;
    private EditText passwordField;
    private ProgressBar bar;
    final Context context = this;
    private Handler handler = new Handler();
    private InetAddress adress = null;
    private int portNumber;
    private Thread loginThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                //AsyncLoginTask task = new AsyncLoginTask();
                //task.execute(loginField.getText().toString(),passwordField.getText().toString());
            }
        });

        readKeyConfiguration();
        /**
        byte[] key = AES.getInstance().getKey();
        Log.i("seskey",AbstractMessage.bytesToHexString(key));

        KeyPacket keyPacket = new KeyPacket(AES.getInstance().getKey());
        AbstractMessage msg = MessageProvider.buildMessage(keyPacket,(byte)0);
        Log.i("rsaseskey",AbstractMessage.bytesToHexString(msg.getBytes()));
         **/
    }

    private void onLogging() {
        bar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        loginField.setEnabled(false);
        passwordField.setEnabled(false);
    }

    private void afterLogin() {
        bar.setVisibility(View.INVISIBLE);
        sendButton.setEnabled(true);
        loginField.setEnabled(true);
        passwordField.setEnabled(true);
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

    private void readKeyConfiguration() {
        byte[] keyBytes = null;
        byte[] keyBytes2 = null;
        try {
            InputStream in = getAssets().open("serwer_key-public.pem");
            keyBytes = new byte[in.available()];
            in.read(keyBytes);
            in.close();

            InputStream in2 = getAssets().open("serwer_key-private.pem");
            keyBytes2 = new byte[in2.available()];
            in2.read(keyBytes2);
            in2.close();
        } catch (IOException e) {
            showAlert(getResources().getString(R.string.keyError),"Błąd konfiguracji");
            //use this convinient function to disable all elements
            onLogging();
            return;
        }
        try {
            RSA.getInstance().generateKey(keyBytes);
            RSA.getInstance().generatePrivateKey(keyBytes2);
        }
        catch (Exception e) {
            showAlert(getResources().getString(R.string.keyError),"Błąd konfiguracji");
            //use this convinient function to disable all elements
            onLogging();
            return;
        }
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
        String login, password;

        public LoginTask(String login, String password) {
            this.login = login;
            this.password = password;
        }

        @Override
        public void run() {
            handler.post(LoginActivity.this::onLogging);
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

                byte[] key = AES.getInstance().getKey();
                Log.i("seskey",AbstractMessage.bytesToHexString(key));

                AbstractMessage msg = MessageProvider.buildMessage(keyPacket,(byte)0);
                Log.i("rsaseskey",AbstractMessage.bytesToHexString(msg.getBytes()));
                if (clientSocket.writeToSocket(msg.getBytes())) {
                    AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                    Log.i("ssid","Odbieram ssid");
                    if ((packet == null) || (packet.getPacketHeader() != AbstractPacket.HEADER_SSID)) {
                        cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędne dane");
                        return;
                    }
                    byte ssid = ((SSIDPacket)packet).getSsid();
                    Log.i("ssidok","Odebralem ssid : "+String.valueOf(ssid));
                    //send login data
                    Log.i("logdata","Wysylam dane logowania");
                    msg = MessageProvider.buildMessage(buildLogPacket(),ssid);
                    if (clientSocket.writeToSocket(msg.getBytes())) {
                        Log.i("logresp","Probuje odebrac odpowiedz na logowanie");
                        packet = MessageReceiver.decryptedPacket(clientSocket);
                        if (packet == null) {
                            cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędne dane");
                            return;
                        }
                        else if (packet.getPacketHeader() == AbstractPacket.HEADER_NAK) {
                            cleanAfterError(getResources().getString(R.string.wrong_login_data), "Błędne informacje logowania");
                            return;
                        }
                        else if (packet.getPacketHeader() == AbstractPacket.HEADER_ACK) {
                            Log.i("logok","Poprawnie zalogowano. Zaczynam nowa aktywnosc");
                            //data is fine, open new Activity
                            clientSocket.close();
                            handler.post(LoginActivity.this::afterLogin);
                            OpenServicesActivity servicesActivity = new OpenServicesActivity(ssid);
                            handler.post(servicesActivity);
                        }
                        else {
                            cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędne dane");
                            return;
                        }
                    }//writing error
                }//writing error
                cleanAfterError(getResources().getString(R.string.writingError),"Błąd wysyłania");
                return;
            }
            //chalenge error
            cleanAfterError(getResources().getString(R.string.servChallengeError),"Błąd serwera");
        }

        private void cleanAfterError(String message, String title) {
            clientSocket.close();
            handler.post(new AlertRun(message,title));
            handler.post(LoginActivity.this::afterLogin);
        }

        private LogPacket buildLogPacket() {
            return new LogPacket(login,password);
        }

        private boolean challengeExchange() {
            Log.i("Chall_start","wchodze");
            AbstractPacket challenge = new ChallPacket();
            AbstractMessage msg = MessageProvider.buildMessage(challenge,(byte)0);
            if (clientSocket.writeToSocket(msg.getBytes())) {
                AbstractPacket packet = MessageReceiver.plainPacket(clientSocket);
                if ((packet == null) || (packet.getPacketHeader() != AbstractPacket.HEADER_CHALL_RESP)) {
                    handler.post(new AlertRun(getResources().getString(R.string.serverResponseError),"Błędna odpowiedź"));
                    return false;
                }
                Log.i("signverify","Testuje podpis");
                return RSA.getInstance().verifySign(packet.getPacketBytes(), Arrays.copyOfRange(challenge.getPacketBytes(),1,challenge.getPacketBytes().length));
            }
            else {
                handler.post(new AlertRun(getResources().getString(R.string.writingError), "Błąd wysyłania"));
                return false;
            }
        }

        public class OpenServicesActivity implements Runnable {
            private byte ssid;

            public OpenServicesActivity(byte ssid) {
                this.ssid = ssid;
            }

            @Override
            public void run() {
                Intent intent = new Intent(context,ServicesActivity.class);
                intent.putExtra(PASS_SSID, ssid);
                intent.putExtra(PASS_HOST, adress.getHostAddress());
                intent.putExtra(PASS_PORT, portNumber);
                startActivity(intent);
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
