package br.com.echitey.android.sunshineapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.net.URL;

import br.com.echitey.android.sunshineapp.data.SunshinePreferences;
import br.com.echitey.android.sunshineapp.utils.NetworkUtils;
import br.com.echitey.android.sunshineapp.utils.OpenWeatherJsonUtils;

public class MainActivity extends AppCompatActivity {

    private TextView mWeatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);

        loadWeatherData();
    }

    private void loadWeatherData() {
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    // ASYNC TASK TO PERFORM NETWORK REQUEST
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length ==0){
                return null;
            }

            String location = params[0];

            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try{
                String jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return simpleJsonWeatherData;
            } catch (Exception e){
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            if(weatherData != null){

                for(String weatherString : weatherData){
                    mWeatherTextView.append((weatherString)+ "\n\n\n");
                }
            } else {
                mWeatherTextView.setText("Unable to fetch the results");
            }
        }
    }
}
