package br.com.echitey.android.sunshineapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.com.echitey.android.sunshineapp.MainActivity;
import br.com.echitey.android.sunshineapp.R;
import br.com.echitey.android.sunshineapp.utils.SunshineDateUtils;
import br.com.echitey.android.sunshineapp.utils.SunshineWeatherUtils;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{

    //private String[] mWeatherData;
    private final ForecastAdapterOnClickHandler mClickHandler;

    private final Context mContext;
    private Cursor mCursor;

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler handler) {
        mContext = context;
        mClickHandler = handler;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutIdForListItem = R.layout.forecast_list_item;
        View view = LayoutInflater.from(mContext).inflate(layoutIdForListItem, viewGroup, false);
        view.setFocusable(true);
        return new ForecastAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        /* Use the weatherId to obtain the proper description */
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String highAndLowTemperature =
                SunshineWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);

        String weatherSummary = dateString + " - " + description + " - " + highAndLowTemperature;

        holder.mWeatherTextView.setText(weatherSummary);
    }

    @Override
    public int getItemCount() {
        if(null == mCursor) return 0;
        return mCursor.getCount();
    }

    // USED TO SET THE WEATHER DATA SET
    public void setWeatherData(Cursor newCursor) {
       mCursor = newCursor;
        notifyDataSetChanged();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
//      COMPLETED (12) After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }

    // VIEWHOLDER INNER CLASS
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView mWeatherTextView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mWeatherTextView = (TextView) view.findViewById(R.id.tv_weather_data);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }

    public interface ForecastAdapterOnClickHandler {
        void onClick(long date);
    }
}
