package br.com.echitey.android.sunshineapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import br.com.echitey.android.sunshineapp.adapters.ForecastAdapter;
import br.com.echitey.android.sunshineapp.data.SunshinePreferences;
import br.com.echitey.android.sunshineapp.utils.NetworkUtils;
import br.com.echitey.android.sunshineapp.utils.OpenWeatherJsonUtils;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<String[]>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWeatherTextView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    public static final String WEATHER_DATA_KEY = "weather_data_key";
    private static final String TAG = "Main Activity";
    private static final int FORECAST_LOADER_ID = 0;

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherTextView = findViewById(R.id.tv_weather_data);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        setUpRecyclerView();

        Bundle loaderBundle = null;

        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER_ID, loaderBundle, this);

        setupSharedPreferences();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(TAG, "onStart: preferences were updated");
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    private void setupSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Set Values
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            invalidateData();
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);
            return true;
        }

        if (id == R.id.action_map) {
            openLocationInMap();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void setUpRecyclerView(){
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);
    }

    private void openLocationInMap() {
        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }

    @Override
    public void onClick(String weatherStringData) {
        Intent intent = new Intent(this, ForecastDetailActivity.class);
        intent.putExtra(WEATHER_DATA_KEY, weatherStringData);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if(mWeatherData != null){
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(MainActivity.this);

                URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);

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
            public void deliverResult(@Nullable String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);

        if(null == data){
            showErrorMessage();
        } else {
            showWeatherDataView();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    private void invalidateData() {
        mForecastAdapter.setWeatherData(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }
}
