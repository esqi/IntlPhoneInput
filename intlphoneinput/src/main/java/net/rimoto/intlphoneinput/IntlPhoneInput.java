package net.rimoto.intlphoneinput;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess", "NullableProblems"})
public class IntlPhoneInput extends RelativeLayout {
    @NonNull
    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();

    public static final int COUNTRY_ID = 0;

    // UI Views
    @NonNull
    private Spinner mCountrySpinner;
    @NonNull
    private TextInputLayout mTextInputLayout;
    @NonNull
    private EditText mPhoneEdit;

    //Adapters
    @NonNull
    private CountrySpinnerAdapter mCountrySpinnerAdapter;
    @NonNull
    private PhoneNumberWatcher mPhoneNumberWatcher = new PhoneNumberWatcher(DEFAULT_COUNTRY);

    //Util
    @NonNull
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();

    // Fields
    @Nullable
    private Country mSelectedCountry;
    @Nullable
    private IntlPhoneInputListener mIntlPhoneInputListener;

    /**
     * Constructor
     *
     * @param context Context
     */
    public IntlPhoneInput(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public IntlPhoneInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntlPhoneInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.intl_phone_input, this);
        init(attrs);
    }

    /**
     * Init after constructor
     */
    private void init(@Nullable AttributeSet attrs) {
        /*
         * Country spinner
         */
        mCountrySpinner = findViewById(R.id.intl_phone_edit__country);
        mCountrySpinnerAdapter = new CountrySpinnerAdapter(getContext(), Countries.COUNTRIES);
        mCountrySpinner.setAdapter(mCountrySpinnerAdapter);

        mCountrySpinner.setOnItemSelectedListener(mCountrySpinnerListener);

        mCountrySpinner.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        setFlagDefaults(attrs);

        /*
         * Phone text field
         */
        mPhoneEdit = findViewById(R.id.intl_phone_edit__phone);
        mPhoneEdit.addTextChangedListener(mPhoneNumberWatcher);
        mTextInputLayout = findViewById(R.id.intl_phone_edit__til);

        setDefault();
        setEditTextDefaults(attrs);
    }

    private void setFlagDefaults(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IntlPhoneInput);
        ViewGroup.LayoutParams layoutParams = mCountrySpinner.getLayoutParams();
        layoutParams.width = a.getDimensionPixelSize(R.styleable.IntlPhoneInput_spinnerWidth, getResources().getDimensionPixelSize(R.dimen.spinner_width));
        mCountrySpinner.setLayoutParams(layoutParams);
        mCountrySpinner.setPrompt(a.getString(R.styleable.IntlPhoneInput_prompt));
        if (Countries.COUNTRIES.size() <= 1) {
            mCountrySpinner.setEnabled(false);
        }
        a.recycle();
    }

    private void setEditTextDefaults(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IntlPhoneInput);

        if (Countries.COUNTRIES.size() > 1) {
            int defaultCountry = a.getInteger(R.styleable.IntlPhoneInput_defaultCountry, -1);
            if (defaultCountry >= 0) {
                switch (defaultCountry) {
                    case COUNTRY_ID:
                        setEmptyDefault("id");
                        break;
                }
            }
        } else if (Countries.COUNTRIES.size() == 1) {
            setEmptyDefault(Countries.COUNTRIES.get(0).getIso().toLowerCase());
        }

        int textSize = a.getDimensionPixelSize(R.styleable.IntlPhoneInput_textSize, getResources().getDimensionPixelSize(R.dimen.text_size_default));
        mPhoneEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        int color = a.getColor(R.styleable.IntlPhoneInput_textColor, -1);
        if (color != -1) {
            mPhoneEdit.setTextColor(color);
        }
        int hintColor = a.getColor(R.styleable.IntlPhoneInput_textColorHint, -1);
        if (hintColor != -1) {
            mPhoneEdit.setHintTextColor(color);
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTextInputLayout.getLayoutParams();
        layoutParams.leftMargin = a.getDimensionPixelSize(R.styleable.IntlPhoneInput_spinnerEndMargin, getResources().getDimensionPixelSize(R.dimen.spinner_right_margin));
        mTextInputLayout.setLayoutParams(layoutParams);
        mTextInputLayout.setHint(a.getString(R.styleable.IntlPhoneInput_hint));
        if (a.hasValue(R.styleable.IntlPhoneInput_hintTextAppearance)) {
            int resourceId = a.getResourceId(R.styleable.IntlPhoneInput_hintTextAppearance, 0);
            mTextInputLayout.setHintTextAppearance(resourceId);
        }
        a.recycle();
    }

    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mPhoneEdit.getWindowToken(), 0);
        }
    }

    /**
     * Set default value
     * Will try to retrieve phone number from device
     */
    public void setDefault() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                @SuppressLint({"MissingPermission", "HardwareIds"})
                String phone = telephonyManager.getLine1Number();
                if (phone != null && !phone.isEmpty()) {
                    this.setNumber(phone);
                } else {
                    String iso = telephonyManager.getNetworkCountryIso();
                    setEmptyDefault(iso);
                }
            }
        } catch (SecurityException e) {
            setEmptyDefault();
        }
    }

    /**
     * Set default value with
     *
     * @param iso ISO2 of country
     */
    public void setEmptyDefault(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) {
            iso = DEFAULT_COUNTRY;
        }
        for (int i = 0; i < Countries.COUNTRIES.size(); i++) {
            Country country = Countries.COUNTRIES.get(i);
            if (country.getIso().toUpperCase().equals(iso.toUpperCase())) {
                mSelectedCountry = country;
                mCountrySpinner.setSelection(i);
                break;
            }
        }
    }

    private void selectCountry(int dialCode) {
        for (int i = 0; i < Countries.COUNTRIES.size(); i++) {
            Country country = Countries.COUNTRIES.get(i);
            if (country.getDialCode() == dialCode) {
                mSelectedCountry = country;
                mCountrySpinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private void setEmptyDefault() {
        setEmptyDefault(null);
    }

    /**
     * Set hint number for country
     */
    private void setHint() {
        if (mSelectedCountry != null) {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(mSelectedCountry.getIso(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                mPhoneEdit.setHint(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            }
        }
    }

    /**
     * Sets hint.
     *
     * @param resId the res id
     */
    public void setHint(int resId) {
        mTextInputLayout.setHint(getContext().getString(resId));
    }

    /**
     * Sets hint.
     *
     * @param hint the hint
     */
    public void setHint(@Nullable String hint) {
        mTextInputLayout.setHint(hint);
    }

    /**
     * Spinner listener
     */
    @NonNull
    private AdapterView.OnItemSelectedListener mCountrySpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedCountry = mCountrySpinnerAdapter.getItem(position);

            //Make sure that the watcher is added into the listeners of the edittext
            //after updating the country selected...
            mPhoneEdit.removeTextChangedListener(mPhoneNumberWatcher);
            if (mSelectedCountry != null) {
                mPhoneNumberWatcher = new PhoneNumberWatcher(mSelectedCountry.getIso());
            } else {
                mPhoneNumberWatcher = new PhoneNumberWatcher();
            }
            mPhoneEdit.addTextChangedListener(mPhoneNumberWatcher);

            setHint();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Phone number watcher
     */
    private class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
        private boolean lastValidity;

        public PhoneNumberWatcher() {
            super();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public PhoneNumberWatcher(@NonNull String countryCode) {
            super(countryCode);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            try {
                Phonenumber.PhoneNumber phoneNumber = parsePhoneNumber(s.toString());
                if (mSelectedCountry == null || mSelectedCountry.getDialCode() != phoneNumber.getCountryCode()) {
                    selectCountry(phoneNumber.getCountryCode());
                }
            } catch (NumberParseException ignored) {
            }

            if (mIntlPhoneInputListener != null) {
                boolean validity = isValid();
                if (validity != lastValidity) {
                    mIntlPhoneInputListener.done(IntlPhoneInput.this, validity);
                }
                lastValidity = validity;
            }
        }
    }

    /**
     * Set Number
     *
     * @param number E.164 format or national format
     */
    public void setNumber(@Nullable String number) {
        try {
            Phonenumber.PhoneNumber phoneNumber = parsePhoneNumber(number);
            if (mSelectedCountry == null || mSelectedCountry.getDialCode() != phoneNumber.getCountryCode()) {
                selectCountry(phoneNumber.getCountryCode());
            }
            mPhoneEdit.setText(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
        } catch (NumberParseException ignored) {
        }
    }

    /**
     * Get number
     *
     * @return Phone number in E.164 format | null on error
     */
    @Nullable
    public String getNumber() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();

        if (phoneNumber == null) {
            return null;
        }

        return mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @Nullable
    public String getText() {
        return getNumber();
    }

    @NonNull
    private Phonenumber.PhoneNumber parsePhoneNumber(@Nullable String number) throws NumberParseException {
        String defaultRegion = mSelectedCountry != null ? mSelectedCountry.getIso() : "";
        return mPhoneUtil.parseAndKeepRawInput(number, defaultRegion);
    }

    /**
     * Get PhoneNumber object
     *
     * @return PhonenUmber | null on error
     */
    @Nullable
    public Phonenumber.PhoneNumber getPhoneNumber() {
        try {
            return parsePhoneNumber(mPhoneEdit.getText().toString());
        } catch (NumberParseException ignored) {
            return null;
        }
    }

    /**
     * Get selected country
     *
     * @return Country
     */
    @Nullable
    public Country getSelectedCountry() {
        return mSelectedCountry;
    }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    public boolean isValid() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Add validation listener
     *
     * @param listener IntlPhoneInputListener
     */
    public void setOnValidityChange(@Nullable IntlPhoneInputListener listener) {
        mIntlPhoneInputListener = listener;
    }

    /**
     * Returns the error message that was set to be displayed with
     * {@link #setError}, or <code>null</code> if no error was set
     * or if it the error was cleared by the widget after user input.
     *
     * @return error message if known, null otherwise
     */
    @Nullable
    public CharSequence getError() {
        return mTextInputLayout.getError();
    }

    /**
     * Sets an error message that will be displayed in a popup when the EditText has focus.
     *
     * @param error error message to show
     */
    public void setError(@Nullable CharSequence error) {
        if (error == null || error.length() == 0) {
            mTextInputLayout.setErrorEnabled(false);
        } else {
            mTextInputLayout.setErrorEnabled(true);
        }
        mTextInputLayout.setError(error);
    }

    /**
     * Sets text color.
     *
     * @param resId the res id
     */
    public void setTextColor(int resId) {
        mPhoneEdit.setTextColor(resId);
    }

    /**
     * Simple validation listener
     */
    public interface IntlPhoneInputListener {
        void done(@NonNull View view, boolean isValid);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextInputLayout.setEnabled(enabled);
        mPhoneEdit.setEnabled(enabled);
        if (Countries.COUNTRIES.size() <= 1) {
            mCountrySpinner.setEnabled(false);
        } else {
            mCountrySpinner.setEnabled(enabled);
        }
    }

    @NonNull
    public Spinner getSpinner() {
        return mCountrySpinner;
    }

    @NonNull
    public TextInputLayout getTextInputLayout() {
        return mTextInputLayout;
    }

    @NonNull
    public EditText getEditText() {
        return mPhoneEdit;
    }

    /**
     * Set keyboard done listener to detect when the user click "DONE" on his keyboard
     *
     * @param listener IntlPhoneInputListener
     */
    public void setOnKeyboardDone(@NonNull final IntlPhoneInputListener listener) {
        mPhoneEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener.done(IntlPhoneInput.this, isValid());
                }
                return false;
            }
        });
    }
}
