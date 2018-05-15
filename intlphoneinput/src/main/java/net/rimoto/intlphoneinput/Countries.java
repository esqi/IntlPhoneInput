package net.rimoto.intlphoneinput;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class Countries {

    @NonNull
    public static final List<Country> COUNTRIES = new ArrayList<>();

    static {
        COUNTRIES.add(new Country("Indonesia", "id", 62));
    }

}
