package project.iotdom;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.ClientSocket;
import project.iotdom.connection.MessageProvider;
import project.iotdom.connection.MessageReceiver;
import project.iotdom.model.Service;
import project.iotdom.model.ServicesListModel;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.DescPacket;
import project.iotdom.packets.EotPacket;
import project.iotdom.packets.GetPacket;
import project.iotdom.packets.NakPacket;
import project.iotdom.packets.SSIDPacket;
import project.iotdom.packets.ServicesPacket;
import project.iotdom.packets.SetPacket;
import project.iotdom.packets.ValPacket;
import project.iotdom.views.AnalogOutViewHolder;
import project.iotdom.views.ComplexAdapter;
import project.iotdom.views.DigitalOutViewHolder;

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
            blockButtons(false);
            servicesView.setEnabled(false);
            refreshBar.setVisibility(View.VISIBLE);
            GetBulkTask task = new GetBulkTask(model.getSSID());
            Thread thread = new Thread(task);
            thread.start();
        });

        logout = (Button)findViewById(R.id.logOutButton);
        logout.setOnClickListener(v -> {
            blockButtons(false);
            servicesView.setEnabled(false);
            logoutBar.setVisibility(View.VISIBLE);
        });
    }

    private void closeDialog(String message, String title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void notifyAdapterAtPosition(int position) {
        adapter.notifyItemChanged(position);
    }

    private void notifyAdapterNewSet() {
        adapter.notifyDataSetChanged();
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

    private void newValuesList(ArrayList<ValPacket> list) {
        for (ValPacket packet : list) {
            int pos = model.findServiceByID(packet.getServiceID());
            if (pos >= 0) {
                if (model.getService(pos).isInBoundary(packet.getValue()))
                    model.setValAtPos(packet.getValue(), pos);
            }
        }
        notifyAdapterNewSet();
    }

    private void setValAtService(float val, int position) {
        model.setValAtPos(val, position);
        notifyAdapterAtPosition(position);
    }

    //handler for reading button
    public void readButtonHandler(View v) {
        int position = servicesView.getChildAdapterPosition(v);
        Service service = model.getService(position);
        GetTask task = new GetTask(model.getSSID(), service, position);
        Thread thread = new Thread(task);
        thread.start();
    }

    //handler for setting button
    public void setButtonHandler(View v) {
        int position = servicesView.getChildAdapterPosition(v);
        RecyclerView.ViewHolder holder = servicesView.findContainingViewHolder(v);
        if ((holder != null) && (holder.getItemViewType() == Service.ANALOG_OUT)) {
            AnalogOutViewHolder analog = (AnalogOutViewHolder)holder;
            float val;
            try {
                val = Float.parseFloat(analog.getValueToSet().getText().toString());
                Service service = model.getService(position);
                if (service.isInBoundary(val)) {
                    SetTask task = new SetTask(model.getSSID(), position, service.getServiceID(), val);
                    Thread thread = new Thread(task);
                    thread.start();
                }
            }
            catch (Exception e) { }
        }
    }

    //handler for switch click
    public void setSwitchHandler(View v) {
        int position = servicesView.getChildAdapterPosition(v);
        RecyclerView.ViewHolder holder = servicesView.findContainingViewHolder(v);
        if ((holder != null) && (holder.getItemViewType() == Service.DIGITAL_OUT)) {
            DigitalOutViewHolder digital = (DigitalOutViewHolder)holder;
            float toSet;
            Service service = model.getService(position);
            toSet = digital.getValueToSet().isChecked() ? 1.0f : 0.0f;
            SetTask task = new SetTask(model.getSSID(), position, service.getServiceID(), toSet);
            Thread thread = new Thread(task);
            thread.start();
        }
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

    private class UnblockView implements Runnable {

        private ProgressBar bar;

        public UnblockView(ProgressBar bar) {
            this.bar = bar;
        }

        @Override
        public void run() {
            blockButtons(true);
            servicesView.setEnabled(true);
            bar.setVisibility(View.GONE);
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
                handler.post(new UnblockView(getBar));
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
                    if (nakPacket.getVal() == (byte) 0x00) {
                        clientSocket.close();
                        handler.post(() -> {
                            closeDialog(getResources().getString(R.string.wrongSSID),"Niepoprawne SSID");
                        });
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
                    handler.post(new UnblockView(getBar));
                    handler.post(() -> {newServicesList(services);});
                    return;
                }
            }
            //writing error
            cleanAndUnblock(getResources().getString(R.string.writingError),"Błąd wysyłania");
        }

        private void cleanAndUnblock(String message, String title) {
            handler.post(new UnblockView(getBar));
            cleanAfterError(message,title,clientSocket);
        }

    }

    class GetTask implements Runnable {

        private byte SSID;
        private Service service;
        private int position;

        private ClientSocket clientSocket = null;

        public GetTask(byte SSID, Service service, int position) {
            this.SSID = SSID;
            this.service = service;
            this.position = position;
        }

        @Override
        public void run() {
            clientSocket = connectionTrial();
            if (clientSocket == null) {
                return;
            }
            GetPacket getPacket = new GetPacket(service.getServiceID());
            AbstractMessage message = MessageProvider.buildMessage(getPacket, SSID);
            if (clientSocket.writeToSocket(message.getBytes())) {
                AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                if (packet == null)
                    cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź",clientSocket);
                else if ((packet.getPacketHeader() != AbstractPacket.HEADER_NAK) && (packet.getPacketHeader() != AbstractPacket.HEADER_VAL))
                    cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź",clientSocket);
                else if (packet.getPacketHeader() == AbstractPacket.HEADER_VAL) {
                    clientSocket.close();
                    ValPacket valPacket = (ValPacket)packet;
                    if ((service.getServiceID() != valPacket.getServiceID()) || (!service.isInBoundary(valPacket.getValue())))
                        cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź",clientSocket);
                    //correct answer
                    handler.post(() -> { setValAtService(valPacket.getValue(), position);});
                }
                else {//nak packet
                    NakPacket nakPacket = (NakPacket)packet;
                    if (nakPacket.getVal() == (byte)0x00) {
                        clientSocket.close();
                        handler.post(() -> {
                            closeDialog(getResources().getString(R.string.wrongSSID),"Niepoprawne SSID");
                        });
                    }
                    else if (nakPacket.getVal() == (byte)0x01) {
                        cleanAfterError(getResources().getString(R.string.serviceUnavailible),"Usługa niedostępna",clientSocket);
                    }
                    else
                        cleanAfterError(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź",clientSocket);
                }
            }
            else
                cleanAfterError(getResources().getString(R.string.writingError),"Błąd wysyłania",clientSocket);
        }
    }

    class GetBulkTask implements Runnable {

        private byte SSID;

        ClientSocket clientSocket = null;

        public GetBulkTask(byte SSID) {
            this.SSID = SSID;
        }

        @Override
        public void run() {
            clientSocket = connectionTrial();
            if (clientSocket == null) {
                handler.post(new UnblockView(refreshBar));
            }
            GetPacket getPacket = new GetPacket((byte)0x00);
            AbstractMessage message = MessageProvider.buildMessage(getPacket, SSID);
            if (clientSocket.writeToSocket(message.getBytes())) {
                AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                if (packet == null) {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                }
                else if (packet.getPacketHeader() == AbstractPacket.HEADER_NAK) {
                    NakPacket nakPacket = (NakPacket)packet;
                    if (nakPacket.getVal() == (byte)0x00) {
                        clientSocket.close();
                        handler.post(() -> {
                            closeDialog(getResources().getString(R.string.wrongSSID),"Niepoprawne SSID");
                        });
                    }
                    else
                        cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                }
                else if (packet.getPacketHeader() == AbstractPacket.HEADER_VAL) {
                    //start building a list of values
                    int received = 1;
                    boolean transmission = true;
                    boolean failure = false;
                    ArrayList<ValPacket> values = new ArrayList<>();
                    ValPacket valPacket = (ValPacket)packet;
                    values.add(valPacket);
                    while (transmission && (received < 255)) {
                        AbstractPacket p1 = MessageReceiver.decryptedPacket(clientSocket);
                        if ((p1 == null) || ((p1.getPacketHeader() != AbstractPacket.HEADER_VAL) && (p1.getPacketHeader() != AbstractPacket.HEADER_EOT))) {
                            transmission = false;
                            failure = true;
                        }
                        else if (p1.getPacketHeader() == AbstractPacket.HEADER_EOT) {
                            transmission = false;
                            failure = false;
                        }
                        else {
                            ValPacket valPacket1 = (ValPacket)p1;
                            values.add(valPacket1);
                            received++;
                        }
                    }
                    if (failure) {
                        cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                    }
                    //data is fine
                    else {
                        clientSocket.close();
                        handler.post(new UnblockView(refreshBar));
                        handler.post(() -> {newValuesList(values);});
                    }
                }
                else {
                    cleanAndUnblock(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                }
            }
            else
                cleanAndUnblock(getResources().getString(R.string.writingError),"Błąd wysyłania");
        }

        private void cleanAndUnblock(String message, String title) {
            handler.post(new UnblockView(refreshBar));
            cleanAfterError(message,title,clientSocket);
        }
    }

    class SetTask implements Runnable {

        private byte SSID;
        private int position;
        private byte serviceID;
        private float valueToSet;

        private ClientSocket clientSocket = null;

        public SetTask(byte SSID, int position, byte serviceID, float valueToSet) {
            this.SSID = SSID;
            this.position = position;
            this.serviceID = serviceID;
            this.valueToSet = valueToSet;
        }

        @Override
        public void run() {
            clientSocket = connectionTrial();
            if (clientSocket == null) {
                handler.post(() -> { notifyAdapterAtPosition(position);});
                return;
            }
            SetPacket setPacket = new SetPacket(serviceID, valueToSet);
            AbstractMessage message = MessageProvider.buildMessage(setPacket, SSID);
            if (clientSocket.writeToSocket(message.getBytes())) {
                AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                if (packet == null)
                    clean(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                else if ((packet.getPacketHeader() != AbstractPacket.HEADER_NAK) && (packet.getPacketHeader() != AbstractPacket.HEADER_ACK))
                    clean(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                else if (packet.getPacketHeader() == AbstractPacket.HEADER_ACK) {
                    clientSocket.close();
                    handler.post(() -> { setValAtService(valueToSet, position);});
                }
                else {//nak packet
                    NakPacket nakPacket = (NakPacket)packet;
                    if (nakPacket.getVal() == (byte)0x00) {
                        clientSocket.close();
                        handler.post(() -> {
                            closeDialog(getResources().getString(R.string.wrongSSID),"Niepoprawne SSID");
                        });
                    }
                    else if (nakPacket.getVal() == (byte)0x01) {
                        clean(getResources().getString(R.string.serviceUnavailible),"Usługa niedostępna");
                    }
                    else
                        clean(getResources().getString(R.string.serverResponseError), "Błędna odpowiedź");
                }
            }
            else
                clean(getResources().getString(R.string.writingError),"Błąd wysyłania");
        }

        private void clean(String message, String title) {
            cleanAfterError(message, title, clientSocket);
            handler.post(() -> { notifyAdapterAtPosition(position);});
        }
    }

    class LogoutTask implements Runnable {

        private byte SSID;

        public LogoutTask(byte SSID) {
            this.SSID = SSID;
        }

        private ClientSocket clientSocket = null;

        @Override
        public void run() {
            clientSocket = connectionTrial();
            if (clientSocket == null) {
                handler.post(() -> {
                    closeDialog("Nie można połączyć się z serwerem. Aktywność zostanie zamknięta","Błąd połączenia");
                });
            }
            EotPacket eotPacket = new EotPacket();
            AbstractMessage message = MessageProvider.buildMessage(eotPacket, SSID);
            if (clientSocket.writeToSocket(message.getBytes())) {
                AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
                if ((packet == null)) {
                    handler.post(() -> {
                        closeDialog("Nie uzyskano odpowiedzi od serwera. Aktywność zostanie zamknięta","Błędna odpowiedź");
                    });
                }
                else if (packet.getPacketHeader() != AbstractPacket.HEADER_ACK) {
                    handler.post(() -> {
                       closeDialog("Niepoprawna odpowiedź serwera. Aktywność zostanie zamknięta","Błędna odpowiedź");
                    });
                }
                else {
                    handler.post(() -> {
                       closeDialog("Nastąpiło wylogowanie", "Wylogowano");
                    });
                }
            }
            else {
                handler.post(() -> {
                    closeDialog("Nie można skontaktować się z serwerem. Aktywność zostanie zamknięta","Błąd wysyłania");
                });
            }
        }
    }
}
