package com.example.ubfac.panicbutton;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class CoffeeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        //start an asynctask
        SampleAsyncTask sampleAsyncTask = new SampleAsyncTask();
        sampleAsyncTask.execute();
    }

    // Today
    // AsyncTask
    // Main Thread and Background Thread
    //Connnecting to server
    // Reading files fo

    //Java How to Program


    //Monday
    //Connecting to Internet
    //Loaders
    //Parsing JSON data
    //




    class SampleAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> {

        //onPreExecute
        //doInBackground
        //onPostExecute
        //onProgressUpdate


        @Override
        protected ArrayList<String> doInBackground(String... strings) {

            //we are doing a very heavy work here
            ArrayList<String> arrayList = new ArrayList<>();

            arrayList.add("Hello");
            arrayList.add("World");


            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);

            Toast.makeText(CoffeeActivity.this, "" + strings, Toast.LENGTH_SHORT).show();
        }
    }



}
