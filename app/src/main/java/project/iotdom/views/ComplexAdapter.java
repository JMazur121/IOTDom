package project.iotdom.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import project.iotdom.R;
import project.iotdom.model.Service;

public class ComplexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Service> services;

    public ComplexAdapter(List<Service> services) {
        this.services = services;
    }

    @Override
    public int getItemViewType(int position) {
        return services.get(position).getDeviceClass();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case Service.ANALOG_IN: {
                View v1 = inflater.inflate(R.layout.analog_in_item, parent, false);
                viewHolder = new AnalogInViewHolder(v1);
                break;
            }
            case Service.ANALOG_OUT: {
                View v2 = inflater.inflate(R.layout.analog_out_item, parent,false);
                viewHolder = new AnalogOutViewHolder(v2);
                break;
            }
            case Service.DIGITAL_IN: {
                View v3 = inflater.inflate(R.layout.digital_in_item, parent, false);
                viewHolder = new DigitalInViewHolder(v3);
                break;
            }
            default:{
                View v4 = inflater.inflate(R.layout.digital_out_item, parent, false);
                viewHolder = new DigitalOutViewHolder(v4);
                break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case Service.ANALOG_IN: {
                AnalogInViewHolder analogInViewHolder = (AnalogInViewHolder)holder;
                configureAnalogInHolder(analogInViewHolder, position);
                break;
            }
            case Service.ANALOG_OUT: {
                AnalogOutViewHolder analogOutViewHolder = (AnalogOutViewHolder)holder;
                configureAnalogOutHolder(analogOutViewHolder, position);
                break;
            }
            case Service.DIGITAL_IN: {
                DigitalInViewHolder digitalInViewHolder = (DigitalInViewHolder)holder;
                configureDigitalInHolder(digitalInViewHolder, position);
                break;

            }
            case Service.DIGITAL_OUT: {
                DigitalOutViewHolder digitalOutViewHolder = (DigitalOutViewHolder)holder;
                configureDigitalOutHolder(digitalOutViewHolder, position);
                break;
            }
        }
    }

    private String analogMin(Service serv) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Minimalna wartość : ");
        stringBuilder.append(serv.getMinValue());
        stringBuilder.append(" ");
        stringBuilder.append(serv.getUnitName());
        return stringBuilder.toString();
    }

    private String analogMax(Service serv) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Maksymalna wartość : ");
        stringBuilder.append(serv.getMaxValue());
        stringBuilder.append(" ");
        stringBuilder.append(serv.getUnitName());
        return stringBuilder.toString();
    }

    private void configureAnalogInHolder(AnalogInViewHolder holder, int position) {
        Service service = services.get(position);
        holder.getServiceName().setText(service.getHumanReadableName());
        holder.getMaxValue().setText(analogMax(service));
        holder.getMinValue().setText(analogMin(service));
        holder.getCurrentValue().setText(String.valueOf(service.getCurrentValue()));
    }

    private void configureAnalogOutHolder(AnalogOutViewHolder holder, int position) {
        Service service = services.get(position);
        holder.getServiceName().setText(service.getHumanReadableName());
        holder.getMaxValue().setText(analogMax(service));
        holder.getMinValue().setText(analogMin(service));
        holder.getCurrentValue().setText(String.valueOf(service.getCurrentValue()));
    }

    private void configureDigitalInHolder(DigitalInViewHolder holder, int position) {
        Service service = services.get(position);
        holder.getServiceName().setText(service.getHumanReadableName());
        if (service.getCurrentValue() > 0)
            holder.getCurrentValue().setChecked(true);
        else
            holder.getCurrentValue().setChecked(false);
    }

    private void configureDigitalOutHolder(DigitalOutViewHolder holder, int position) {
        Service service = services.get(position);
        holder.getServiceName().setText(service.getHumanReadableName());
        if (service.getCurrentValue() > 0)
            holder.getCurrentValue().setChecked(true);
        else
            holder.getCurrentValue().setChecked(false);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }
}
