package ca.bcit.assignment2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextUserId;
    EditText editTextSysReading;
    EditText editTextDiasReading;
    Button btnAddReading;

    DatabaseReference databaseReading;

    ListView listViewReading;
    List<Reading> readingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReading = FirebaseDatabase.getInstance().getReference("reading");

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextSysReading = findViewById(R.id.editTextSysReading);
        editTextDiasReading = findViewById(R.id.editTextDiasReading);
        btnAddReading = findViewById(R.id.buttonAddReading);

        listViewReading = findViewById(R.id.listViewReading);
        readingList = new ArrayList<Reading>();

        editTextUserId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    getReadingData();
                }
            }
        });

        btnAddReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReading();
            }
        });

        listViewReading.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Reading reading = readingList.get(position);

                showUpdateDialog(reading.getId(),
                        reading.getUserid(),
                        reading.getReadingDate(),
                        reading.getReadingTime(),
                        reading.getSystolicReading(),
                        reading.getDiastolicReading());
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getReadingData();

    }

    private void getReadingData() {
        databaseReading.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                readingList.clear();
                for (DataSnapshot readingSnapshot : dataSnapshot.getChildren()) {
                    Reading todo = readingSnapshot.getValue(Reading.class);
                    if (editTextUserId.getText().toString().equalsIgnoreCase(todo.getUserid()))
                        readingList.add(todo);
                }

                ReadingListAdapter adapter = new ReadingListAdapter(MainActivity.this, readingList);
                listViewReading.setAdapter(adapter);

                getAverage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void addReading() {
        final String userid = editTextUserId.getText().toString().toLowerCase();
        String sysReading = editTextSysReading.getText().toString();
        String diasReading = editTextDiasReading.getText().toString();
        double systolic = Double.parseDouble(sysReading);
        double diastolic = Double.parseDouble(diasReading);

        if (TextUtils.isEmpty(userid)) {
            Toast.makeText(this, "You must enter a user id.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(sysReading)) {
            Toast.makeText(this, "You must enter a systolic reading.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(diasReading)) {
            Toast.makeText(this, "You must enter a diastolic.", Toast.LENGTH_LONG).show();
            return;
        }

        String id = databaseReading.push().getKey();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        String date = dateFormatter.format(Calendar.getInstance().getTime());
        String time = timeFormatter.format(Calendar.getInstance().getTime());

        final Reading reading = new Reading(id, userid, date, time, systolic, diastolic);

        Task setValueTask = databaseReading.child(id).setValue(reading);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this, "Reading added.", Toast.LENGTH_LONG).show();
                if (reading.getCondition().equals("Hypertensive Crisis")) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Warning: Hypertensive Crisis")
                            .setMessage("You should consult your doctor immediately!")
                            .setPositiveButton(android.R.string.yes, null)
                            .show();
                }
                editTextSysReading.setText("");
                editTextDiasReading.setText("");
            }
        });

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReading(String id, String userid, String date, String time, double sysReading, double diasReading) {
        DatabaseReference dbRef = databaseReading.child(id);

        Reading reading = new Reading(id, userid, date, time, sysReading, diasReading);

        Task setValueTask = dbRef.setValue(reading);

        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this,
                        "Reading Updated.", Toast.LENGTH_LONG).show();
            }
        });

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong.\n" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(final String id, final String userid, final String date, final String time, double sysReading, double diasReading) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextSystolic = dialogView.findViewById(R.id.editTextSysReading);
        editTextSystolic.setText(String.valueOf(sysReading));

        final EditText editTextDiastolic = dialogView.findViewById(R.id.editTextDiasReading);
        editTextDiastolic.setText(String.valueOf(diasReading));

        final Button btnUpdate = dialogView.findViewById(R.id.buttonUpdate);

        dialogBuilder.setTitle("Update Reading: " + userid + " " + date + " " + time);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double systolic = Double.parseDouble(editTextSystolic.getText().toString());
                double diastolic = Double.parseDouble(editTextDiastolic.getText().toString());

                if (TextUtils.isEmpty(editTextSystolic.getText().toString())) {
                    editTextSystolic.setError("Systolic reading is required");
                    return;
                }

                if (TextUtils.isEmpty(editTextDiastolic.getText().toString())) {
                    editTextDiastolic.setError("Diastolic reading is required");
                    return;
                }

                updateReading(id, userid, date, time, systolic, diastolic);
                alertDialog.dismiss();
            }
        });

        final Button btnDelete = dialogView.findViewById(R.id.buttonDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteReading(id);
                alertDialog.dismiss();
            }
        });
    }

    public void deleteReading(String id) {
        DatabaseReference dbRef = databaseReading.child(id);
        Task setRemoveTask = dbRef.removeValue();

        setRemoveTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this, "Reading Deleted.", Toast.LENGTH_LONG).show();
            }
        });

        setRemoveTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Something went wrong.\n" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getAverage() {
        TextView textViewAvgTitle = findViewById(R.id.textViewAvgTitle);
        TextView textViewAvgSys = findViewById(R.id.textViewAvgSystolic);
        TextView textViewAvgDias = findViewById(R.id.textViewAvgDiastolic);
        TextView textViewAvgCond = findViewById(R.id.textViewAvgCondition);
        String user = editTextUserId.getText().toString();

        String month = new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime());
        String year = new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime());
        String title = "Month-to-date average readings of " + user + " for " + month + " " + year;

        textViewAvgTitle.setText(title);

        int size = 0;
        double totalSystolic = 0;
        double totalDiastolic = 0;
        for (Reading reading : readingList) {
            if (reading.getMonth().equalsIgnoreCase(new SimpleDateFormat("MM").format(Calendar.getInstance().getTime()))
                    && reading.getYear().equalsIgnoreCase(new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime()))) {
                size++;
                totalSystolic += reading.getSystolicReading();
                totalDiastolic += reading.getDiastolicReading();
            }
        }
        double avgSystolic = 0;
        double avgDiastolic = 0;
        if (size > 0) {
            avgSystolic = totalSystolic / (double) size;
            avgDiastolic = totalDiastolic / (double) size;
        }
        String condition = Reading.calculateCondition(avgSystolic, avgDiastolic);

        NumberFormat formatter = new DecimalFormat("#0.00");

        textViewAvgSys.setText(formatter.format(avgSystolic));
        textViewAvgDias.setText(formatter.format(avgDiastolic));
        textViewAvgCond.setText(condition);
        findViewById(R.id.linearLayoutAverage).setBackgroundColor(Reading.getColor(condition));

    }
}
