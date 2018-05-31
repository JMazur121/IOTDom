package project.iotdom.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import project.iotdom.R;

public class DigitalInViewHolder extends RecyclerView.ViewHolder {

    private TextView serviceName;
    private Switch currentValue;
    private Button readButton;

    public DigitalInViewHolder(View itemView) {
        super(itemView);
        serviceName = (TextView)itemView.findViewById(R.id.digitalInName);
        currentValue = (Switch)itemView.findViewById(R.id.digitalInCurVal);
        readButton = (Button)itemView.findViewById(R.id.digitalInReadButton);
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

}
