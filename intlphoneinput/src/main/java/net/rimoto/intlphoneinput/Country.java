package net.rimoto.intlphoneinput;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Locale;

@SuppressWarnings({"WeakerAccess", "unused", "NullableProblems"})
public class Country {
    /**
     * Name of country
     */
    @NonNull
    private String name;
    /**
     * ISO2 of country
     */
    @NonNull
    private String iso;
    /**
     * Dial code prefix of country
     */
    private int dialCode;


    /**
     * Constructor
     *
     * @param name     String
     * @param iso      String of ISO2
     * @param dialCode int
     */
    public Country(@NonNull String name, @NonNull String iso, int dialCode) {
        setName(name);
        setIso(iso);
        setDialCode(dialCode);
    }

    /**
     * Get name of country
     *
     * @return String
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Set name of country
     *
     * @param name String
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Get ISO2 of country
     *
     * @return String
     */
    @NonNull
    public String getIso() {
        return iso;
    }

    /**
     * Set ISO2 of country
     *
     * @param iso String
     */
    public void setIso(@NonNull String iso) {
        this.iso = iso.toUpperCase();
    }

    /**
     * Get dial code prefix of country (like +1)
     *
     * @return int
     */
    public int getDialCode() {
        return dialCode;
    }

    /**
     * Set dial code prefix of country (like +1)
     *
     * @param dialCode int (without + prefix!)
     */
    public void setDialCode(int dialCode) {
        this.dialCode = dialCode;
    }

    @NonNull
    public String getDisplayName() {
        return new Locale("", iso).getDisplayCountry(Locale.US);
    }

    /**
     * Check if equals
     *
     * @param o Object to compare
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Country) && (((Country) o).getIso().toUpperCase().equals(this.getIso().toUpperCase()));
    }

    public int getResId(@NonNull Context context) {
        return context.getResources().getIdentifier(String.format("country_%s", iso.toLowerCase()), "drawable", context.getPackageName());
    }
}
