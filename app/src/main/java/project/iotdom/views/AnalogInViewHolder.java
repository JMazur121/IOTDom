package project.iotdom.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import project.iotdom.R;

public class AnalogInViewHolder extends RecyclerView.ViewHolder {

    private TextView serviceName;
    private TextView minValue;
    private TextView maxValue;
    private TextView currentValue;
    private Button readButton;

    public AnalogInViewHolder(View itemView) {
        super(itemView);
        serviceName = (TextView)itemView.findViewById(R.id.analogInName);
        minValue = (TextView)itemView.findViewById(R.id.analogInMinVal);
        maxValue = (TextView)itemView.findViewById(R.id.analogInMaxVal);
        currentValue = (TextView)itemView.findViewById(R.id.analogInCurVal);
        readButton = (Button)itemView.findViewById(R.id.analogInReadButton);
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

}
