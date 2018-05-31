package project.iotdom.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import project.iotdom.R;

public class DigitalOutViewHolder extends RecyclerView.ViewHolder {

    private TextView serviceName;

    private Switch currentValue;
    private Button readButton;

    private Switch valueToSet;

    public DigitalOutViewHolder(View itemView) {
        super(itemView);
        serviceName = (TextView)itemView.findViewById(R.id.digitalOutName);
        currentValue = (Switch)itemView.findViewById(R.id.digitalOutCurVal);
        readButton = (Button)itemView.findViewById(R.id.digitalOutReadButton);
        valueToSet = (Switch)itemView.findViewById(R.id.digitalOutSetVal);
    }

    public TextView getServiceName() {
        return serviceName;
    }

    public Switch getCurrentValue() {
        return currentValue;
    }

    public Button getReadButton() {
        return readButton;
    }

    public Switch getValueToSet() {
        return valueToSet;
    }
}
