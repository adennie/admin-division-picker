package com.example.admindivpicker;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

import com.andydennie.admindivpicker.AdminDivision;
import com.andydennie.admindivpicker.AdminDivisionPicker;
import com.andydennie.admindivpicker.AdminDivisionPickerListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        final AdminDivisionPicker picker = AdminDivisionPicker.newInstance("US");
        // preselect by code
        picker.setPreselectedAdminDivision(new AdminDivision("MA", null));
        picker.setListener(new AdminDivisionPickerListener() {

            @Override
            public void onAdminDivisionSelected(AdminDivision adminDivision) {
                Toast.makeText(
                        MainActivity.this,
                        "Administrative Division Name: " + adminDivision.getName()
                                + " - Code: " + adminDivision.getCode(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        transaction.replace(R.id.content_frame, picker);

        transaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.show_dialog);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AdminDivisionPicker picker = AdminDivisionPicker.newInstance("US", "Select State");
                // preselect by name
                picker.setPreselectedAdminDivision(new AdminDivision(null, "Massachusetts"));
                picker.setListener(new AdminDivisionPickerListener() {

                    @Override
                    public void onAdminDivisionSelected(AdminDivision adminDivision) {
                        Toast.makeText(
                                MainActivity.this,
                                "Administrative Division Name: " + adminDivision.getName()
                                        + " - Code: " + adminDivision.getCode(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
                picker.show(getSupportFragmentManager(), null);
                return false;
            }
        });
        return true;
    }
}
