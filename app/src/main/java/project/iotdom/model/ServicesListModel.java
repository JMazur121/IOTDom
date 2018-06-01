package project.iotdom.model;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ServicesListModel extends ViewModel {

    private List<Service> services;

    private byte SSID;

    public List<Service> getServices() {
        if (services == null)
            services = new ArrayList<>();
        return services;
    }

    public Service getService(int pos) {
        return services.get(pos);
    }

    public void setSSID(byte SSID) {
        this.SSID = SSID;
    }

    public byte getSSID() {
        return SSID;
    }

    public int findServiceByID(byte id) {
        for (int i=0; i<services.size(); i++) {
            if (services.get(i).getServiceID() == id)
                return i;
        }
        return -1;
    }

    public void addAll(List<Service> toAdd) {
        services.addAll(toAdd);
    }

    public void clear() {
        services.clear();
    }

    public void edit(Service newService, int position) {
        services.set(position,newService);
    }

    public void setValAtPos(float val, int position) {
        services.get(position).setCurrentValue(val);
    }
}
