package project.iotdom;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.ClientSocket;
import project.iotdom.connection.MessageProvider;
import project.iotdom.connection.MessageReceiver;
import project.iotdom.model.Service;
import project.iotdom.model.ServicesListModel;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.DescPacket;
import project.iotdom.packets.NakPacket;
import project.iotdom.packets.SSIDPacket;
import project.iotdom.packets.ServicesPacket;
import project.iotdom.packets.ValPacket;
import project.iotdom.views.ComplexAdapter;

public class ServicesActivity extends AppCompatActivity {

    private final Context context = this;
    private Handler handler;
    private ServicesListModel model;
    private ComplexAdapter adapter;

    private RecyclerView servicesView;

    private Button getServices, refresh, logout;
    private ProgressBar getBar, refreshBar, logoutBar;

    private InetAddress adress;
    private int portNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        handler = new Handler();

        model = ViewModelProviders.of(this).get(ServicesListModel.class);
        Intent intent = getIntent();
        byte ssid = intent.getByteExtra(LoginActivity.PASS_SSID,(byte)0);
        model.setSSID(ssid);
        portNumber = intent.getIntExtra(LoginActivity.PASS_PORT,60000);
        try {
            adress = InetAddress.getByName(intent.getStringExtra(LoginActivity.PASS_HOST));
        } catch (UnknownHostException e) { }

        getBar = (ProgressBar)findViewById(R.id.getServicesProgressBar);
        refreshBar = (ProgressBar)findViewById(R.id.refreshProgressBar);
        logoutBar = (ProgressBar)findViewById(R.id.logoutProgressBar);

        adapter = new ComplexAdapter(model.getServices());
        servicesView = (RecyclerView) findViewById(R.id.servicesRecycler);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        servicesView.addItemDecoration(itemDecoration);
        servicesView.setAdapter(adapter);
        servicesView.setLayoutManager(new LinearLayoutManager(this));

        getServices = (Button)findViewById(R.id.getServicesButton);
        getServices.setOnClickListener(v -> {
            blockButtons(false);
            servicesView.setEnabled(false);
            getBar.setVisibility(View.VISIBLE);
            GetServicesTask task = new GetServicesTask(model.getSSID());
            Thread thread = new Thread(task);
            thread.start();
        });

        refresh = (Button)findViewById(R.id.refreshButton);
        refresh.setOnClickListener(v -> {
            if (model.getServices().isEmpty())
                return;
        });

        logout = (Button)findViewById(R.id.logOutButton);
        logout.setOnClickListener(v -> {

        });
    }

    private void notifyAdapterAtPosition(int position) {
        adapter.notifyItemChanged(position);
    }

    private void notifyAdapterNewSet() {
        adapter.notifyDataSetChanged();
    }

    private void setBarState(boolean visible, ProgressBar bar) {
        if (visible)
            bar.setVisibility(View.VISIBLE);
        else
            bar.setVisibility(View.GONE);
    }

    private void setButtonState(boolean clickable, Button button) {
        if (clickable)
            button.setEnabled(true);
        else
            button.setEnabled(false);
    }

    private void blockButtons(boolean enabled) {
        getServices.setEnabled(enabled);
        refresh.setEnabled(enabled);
        logout.setEnabled(enabled);
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

    private void newServicesList(ArrayList<Service> list) {
        model.clear();
        notifyAdapterNewSet();
        model.addAll(list);
        notifyAdapterNewSet();
    }

    private Service getService(int position) {
        return model.getService(position);
    }

    private void setValAtService(float val, int position) {
        model.setValAtPos(val, position);
    }

    //handler for reading button
    public void readButtonHandler(View v) {

    }

    //handler for setting button
    public void setButtonHandler(View v) {

    }

    //handler for switch click
    public void setSwitchHandler(View v) {

    }

    private void cleanAfterError(String message, String title, ClientSocket socket) {
        socket.close();
        handler.post(new AlertRun(message,title));
    }

    private ClientSocket connectionTrial() {
        ClientSocket clientSocket;
        try {
            clientSocket = new ClientSocket(adress,portNumber);
            clientSocket.connect();
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException) {
                showAlert(getResources().getString(R.string.connectionTimeout),"Brak odpowiedzi");
            }
            else {
                showAlert(getResources().getString(R.string.connectionError),"Błąd połączenia");
            }
            return null;
        }
        return clientSocket;
    }


    class ToastRun implements Runnable {

        private String message;
        public ToastRun(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        }
    }

    class AlertRun implements Runnable {
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

    class GetServicesTask implements Runnable {

        private byte SSID;

        ClientSocket clientSocket = null;

        public GetServicesTask(byte SSID) {
            this.SSID = SSID;
        }

        @Override
        public void run() {
            clientSocket = connectionTrial();
            if (clientSocket == null) {
                handler.post(new UnblockView());
            }
            //send request with ssid
            AbstractMessage message = MessageProvider.buildMessage(new ServicesPacket(), SSID);
            if (clientSocket.writeToSocket(message.getBytes())) {
                AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                if (packet == null) {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                    return;
                }
                if (packet.getPacketHeader() == AbstractPacket.HEADER_NAK) {
                    NakPacket nakPacket = (NakPacket) packet;
                    //wrong session id
                    //todo
                    if (nakPacket.getVal() == (byte) 0x00) {
                        //show infromation about wrong ssid and close activity to get back to the logging activity
                        return;
                    }
                    //nak packet with wrong value
                    else {
                        cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                        return;
                    }
                }
                //it's not NAK, we should start receiving pairs of DESC and VAL
                if (packet.getPacketHeader() != AbstractPacket.HEADER_DESC) {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                    return;
                }
                AbstractPacket val = MessageReceiver.decryptedPacket(clientSocket);
                if ((val == null) || (val.getPacketHeader() != AbstractPacket.HEADER_VAL)) {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                    return;
                }
                //start building a list of services
                int received = 1;
                boolean transmission = true;
                boolean failure = false;
                ArrayList<Service> services = new ArrayList<>();
                Service serv = new Service((DescPacket) packet,((ValPacket)val).getValue());
                services.add(serv);
                while (transmission && (received < 255)) {
                    //try to receive another pair of packets
                    AbstractPacket p1 = MessageReceiver.decryptedPacket(clientSocket);
                    if ((p1 == null) || ((p1.getPacketHeader() != AbstractPacket.HEADER_DESC) && (p1.getPacketHeader() != AbstractPacket.HEADER_EOT))) {
                        transmission = false;
                        failure = true;
                    }
                    else if (p1.getPacketHeader() == AbstractPacket.HEADER_EOT) {
                        transmission = false;
                        failure = false;
                    }
                    else {
                        AbstractPacket p2 = MessageReceiver.decryptedPacket(clientSocket);
                        if ((p2 == null) || (p2.getPacketHeader() != AbstractPacket.HEADER_VAL)) {
                            transmission = false;
                            failure = true;
                        }
                        else {
                            serv = new Service((DescPacket)p1,((ValPacket)p2).getValue());
                            services.add(serv);
                            received++;
                        }
                    }
                }
                if (failure) {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                    return;
                }
                //data is fine
                else {
                    clientSocket.close();
                    handler.post(new UnblockView());
                    handler.post(() -> {newServicesList(services);});
                    return;
                }
            }
            //writing error
            cleanAndUnblock(getResources().getString(R.string.writingError),"Błąd wysyłania");
        }

        private void cleanAndUnblock(String message, String title) {
            handler.post(new UnblockView());
            cleanAfterError(message,title,clientSocket);
        }

        private class UnblockView implements Runnable {
            @Override
            public void run() {
                blockButtons(true);
                servicesView.setEnabled(true);
                getBar.setVisibility(View.GONE);
            }
        }
    }

    class GetTask implements Runnable {

        private byte SSID;
        private byte serviceID;

        public GetTask(byte SSID, byte serviceID) {
            this.SSID = SSID;
            this.serviceID = serviceID;
        }

        @Override
        public void run() {

        }
    }

    class GetBulkTask implements Runnable {

        private byte SSID;

        @Override
        public void run() {

        }
    }

    class SetTask implements Runnable {

        private byte SSID;
        private byte serviceID;

        public SetTask(byte SSID, byte serviceID) {
            this.SSID = SSID;
            this.serviceID = serviceID;
        }

        @Override
        public void run() {

        }
    }

    class LogoutTask implements Runnable {

        private byte SSID;

        public LogoutTask(byte SSID) {
            this.SSID = SSID;
        }

        @Override
        public void run() {

        }
    }
}
