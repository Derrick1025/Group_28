package my.edu.utar.grp_nav;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

public class PassengerFragment extends Fragment {

    private AutoCompleteTextView fromLoc;
    private EditText toLoc, time, date;
    private Button btnSearch;

    public PassengerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_passenger, container, false);

        // Initialize Views
        fromLoc = rootView.findViewById(R.id.from);
        toLoc = rootView.findViewById(R.id.to);
        date = rootView.findViewById(R.id.Date);
        time = rootView.findViewById(R.id.time);
        btnSearch = rootView.findViewById(R.id.search);

        // Retrieve saved data from SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("savelocationtext", Context.MODE_PRIVATE);
        String savedStartingLoc = sharedPref.getString("startingloc", "");
        if (!savedStartingLoc.isEmpty()) {
            fromLoc.setText(savedStartingLoc);
        }

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String startinglocData = intent.getStringExtra("searchQueryKey");
            if (startinglocData != null && !startinglocData.isEmpty()) {
                if (checkpreferences()) {
                    fromLoc.setText(startinglocData);
                    saveLocationToPreferences(startinglocData);
                } else {
                    toLoc.setText(startinglocData);
                }
            }
        }

        final View mainView = rootView.findViewById(R.id.offer_info); // Use the id of the root view
        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                view.requestFocus(); // Clear focus from the current focused view
                return false;
            }
        });

        fromLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(v);
                launchMapsActivity("fromLoc");
                return true; // This returns true to indicate that the touch event is handled.
            }
        });

        toLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(v);
                launchMapsActivity("toLoc");
                return true; // This returns true to indicate that the touch event is handled.
            }
        });

        date.setOnClickListener(v -> showDatePicker());
        time.setOnClickListener(v -> showTimePicker());

        btnSearch.setOnClickListener(v -> handleSearchClick());

        return rootView;
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void launchMapsActivity(String type) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        if ("fromLoc".equals(type)) {
            startActivityForResult(intent, 1); // Request code for 'fromLoc'
        } else {
            startActivityForResult(intent, 2); // Request code for 'toLoc'
        }
    }

    private void saveLocationToPreferences(String location) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("savelocationtext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("startingloc", location);
        editor.apply();
    }

    private boolean checkpreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("checktextvalid", Context.MODE_PRIVATE);
        return !sharedPreferences.getAll().isEmpty(); //true if data exists; false otherwise
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> time.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour)),
                hour, minute, false);

        timePickerDialog.show();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.DatePicker_Style,
                (view, yearSelected, monthSelected, dayOfMonth) -> {
                    // Handle the selected date here
                    String Date = dayOfMonth + "/" + (monthSelected + 1) + "/" + yearSelected;
                    date.setText(Date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void handleSearchClick() {
        if (areAllFieldsFilled()) {
            saveLocationToPreferences(""); // Clear location after search

            Intent intent = new Intent(getContext(), CarpoolOffer.class);
            intent.putExtra("SELECTED_DATE", date.getText().toString());
            intent.putExtra("SELECTED_TIME", time.getText().toString());
            intent.putExtra("FROM_LOCATION", fromLoc.getText().toString());
            intent.putExtra("TO_LOCATION", toLoc.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean areAllFieldsFilled() {
        return !fromLoc.getText().toString().trim().isEmpty() &&
                !toLoc.getText().toString().trim().isEmpty() &&
                !time.getText().toString().trim().isEmpty();
    }

    private void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String selectedLocation = data.getStringExtra("selectedLocation");
                if (requestCode == 1) {
                    fromLoc.setText(selectedLocation);
                } else if (requestCode == 2) {
                    toLoc.setText(selectedLocation);
                }
            }
        }
    }
}
