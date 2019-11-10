package ca.bcit.assignment2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class ReadingListAdapter extends ArrayAdapter<Reading> {
    private Activity context;
    private List<Reading> readingList;

    public ReadingListAdapter(Activity context, List<Reading> readingList) {
        super(context, R.layout.list_layout, readingList);
        this.context = context;
        this.readingList = readingList;
    }

    public ReadingListAdapter(Context context, int resource, List<Reading> objects, Activity context1, List<Reading> readingList) {
        super(context, resource, objects);
        this.context = context1;
        this.readingList = readingList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView tvUser = listViewItem.findViewById(R.id.textViewUser);
        TextView tvDate = listViewItem.findViewById(R.id.textViewDate);
        TextView tvTime = listViewItem.findViewById(R.id.textViewTime);
        TextView tvSystolic = listViewItem.findViewById(R.id.textViewSysReading);
        TextView tvDiastolic = listViewItem.findViewById(R.id.textViewDiasReading);
        TextView tvCondition = listViewItem.findViewById(R.id.textViewCondition);

        Reading reading = readingList.get(position);
        tvUser.setText(reading.getUserid());
        tvDate.setText(reading.getReadingDate());
        tvTime.setText(reading.getReadingTime());

        NumberFormat formatter = new DecimalFormat("#0.00");

        tvSystolic.setText(formatter.format(reading.getSystolicReading()));
        tvDiastolic.setText(formatter.format(reading.getDiastolicReading()));
        tvCondition.setText(reading.getCondition());

        listViewItem.setBackgroundColor(Reading.getColor(reading.getCondition()));

        return listViewItem;
    }

}
