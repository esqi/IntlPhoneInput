package net.rimoto.intlphoneinput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class CountrySpinnerAdapter extends ArrayAdapter<Country> {
    private final LayoutInflater mLayoutInflater;

    /**
     * Constructor
     *
     * @param context   Context
     * @param countries country list
     */
    public CountrySpinnerAdapter(@NonNull Context context, @NonNull List<Country> countries) {
        super(context, R.layout.item_country, R.id.intl_phone_edit__country__item_name, countries);
        mLayoutInflater = LayoutInflater.from(getContext());
    }

    /**
     * Drop down item view
     *
     * @param position    position of item
     * @param convertView View of item
     * @param parent      parent view of item's view
     * @return covertView
     */
    @Override
    @NonNull
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_country, parent, false);
        }
        Country country = getItem(position);
        if (country != null) {
            TextView textView = (TextView) convertView;
            textView.setText(String.format("%s (+%s)", country.getName(), country.getDialCode()));
            textView.setCompoundDrawablesWithIntrinsicBounds(country.getResId(getContext()), 0, 0, 0);
        }
        return convertView;
    }

    /**
     * Drop down selected view
     *
     * @param position    position of selected item
     * @param convertView View of selected item
     * @param parent      parent of selected view
     * @return convertView
     */
    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.spinner_value, parent, false);
        }
        Country country = getItem(position);
        if (country != null) {
            TextView textView = (TextView) convertView;
            textView.setText(String.format("+%d", country.getDialCode()));
            textView.setCompoundDrawablesWithIntrinsicBounds(country.getResId(getContext()), 0, 0, 0);
        }
        return convertView;
    }
}
