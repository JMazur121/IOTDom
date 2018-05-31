package project.iotdom.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import project.iotdom.R;

public class AnalogOutViewHolder extends RecyclerView.ViewHolder {

    private TextView serviceName;
    private TextView minValue;
    private TextView maxValue;

    private TextView currentValue;
    private Button readButton;

    private EditText valueToSet;
    private Button setButton;

    public AnalogOutViewHolder(View itemView) {
        super(itemView);
        serviceName = (TextView)itemView.findViewById(R.id.analogOutName);
        minValue = (TextView)itemView.findViewById(R.id.analogOutMinVal);
        maxValue = (TextView)itemView.findViewById(R.id.analogOutMaxVal);
        currentValue = (TextView)itemView.findViewById(R.id.analogOutCurVal);
        readButton = (Button)itemView.findViewById(R.id.analogOutReadButton);
        valueToSet = (EditText)itemView.findViewById(R.id.analogOutSetVal);
        setButton = (Button)itemView.findViewById(R.id.analogOutSetButton);
    }

    public TextView getServiceName() {
        return serviceName;
    }

    public TextView getMinValue() {
        return minValue;
    }

    public TextView getMaxValue() {
        return maxValue;
    }

    public TextView getCurrentValue() {
        return currentValue;
    }

    public Button getReadButton() {
        return readButton;
    }

    public EditText getValueToSet() {
        return valueToSet;
    }

    public Button getSetButton() {
        return setButton;
    }
}
