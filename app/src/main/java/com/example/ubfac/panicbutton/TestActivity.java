package com.example.ubfac.panicbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    public String[] states;
    public String[] statesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        states = this.getResources().getStringArray(R.array.states);
        statesId = this.getResources().getStringArray(R.array.state_ids);

    }


    /*public void test(View view) {
//        new GetLGAsTask().execute();

        getCoordinatesForPollingUnits(getJsonString("AK", "1"));
    }*/





}



