package project.iotdom;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    private Handler handler;
    private Button sendButton;
    private EditText loginField;
    private EditText passwordField;
    final Context context = this;
    private InetAddress adress = null;
    private int portNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handler = new Handler(context.getMainLooper());
        sendButton = (Button)findViewById(R.id.loginButton);
        loginField = (EditText)findViewById(R.id.loginEntry);
        passwordField = (EditText)findViewById(R.id.passwordEntry);
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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = readServerConfiguration();
                if (result) {

                }
            }
        });
    }

    private void showAlert(String message, String title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
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

}
