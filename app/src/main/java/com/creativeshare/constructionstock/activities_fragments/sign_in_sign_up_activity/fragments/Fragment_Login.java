package com.creativeshare.constructionstock.activities_fragments.sign_in_sign_up_activity.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.creativeshare.constructionstock.R;
import com.creativeshare.constructionstock.activities_fragments.sign_in_sign_up_activity.activity.Login_Activity;
import com.creativeshare.constructionstock.models.UserModel;
import com.creativeshare.constructionstock.preferences.Preferences;
import com.creativeshare.constructionstock.remote.Api;
import com.creativeshare.constructionstock.share.Common;
import com.creativeshare.constructionstock.tags.Tags;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;

import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Fragment_Login extends Fragment implements OnCountryPickerListener {
    private Button btn_login;
    private TextView tv_skip, tv_sign_up , tv_code;
    private CountryPicker picker;
    private EditText edt_phone, edt_password;
    private Login_Activity activity;
    private Preferences preferences;
    private String code = "";
    private ImageView image_phone_code;
    private String current_language;

    public static Fragment_Login newInstance() {

        return new Fragment_Login();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initView(view);
        return view;
    }

    private void initView(final View view) {
        activity = (Login_Activity) getActivity();
        Paper.init(activity);
        current_language = Paper.book().read("lang", Locale.getDefault().getLanguage());

        btn_login = view.findViewById(R.id.btn_login);
        tv_skip = view.findViewById(R.id.tv_skip);
        tv_sign_up = view.findViewById(R.id.tv_sign_up);
        edt_phone = view.findViewById(R.id.edt_phone);
        edt_password = view.findViewById(R.id.edt_password);
        tv_code = view.findViewById(R.id.tv_code);
        image_phone_code = view.findViewById(R.id.image_phone_code);


        if (current_language.equals("ar")) {
            image_phone_code.setRotation(180.0f);
        }

        CreateCountryDialog();


        image_phone_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.showDialog(activity);
            }
        });
        tv_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.DisplayFragmentSignUp();
            }
        });

        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.NavigateToHomeActivity();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });
    }
    private void CreateCountryDialog() {
        CountryPicker.Builder builder = new CountryPicker.Builder()
                .canSearch(true)
                .with(activity)
                .listener(this);
        picker = builder.build();

        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);


        if (picker.getCountryFromSIM() != null) {
            updateUi(picker.getCountryFromSIM());

        } else if (telephonyManager != null && picker.getCountryByISO(telephonyManager.getNetworkCountryIso()) != null) {
            updateUi(picker.getCountryByISO(telephonyManager.getNetworkCountryIso()));


        } else if (picker.getCountryByLocale(Locale.getDefault()) != null) {
            updateUi(picker.getCountryByLocale(Locale.getDefault()));

        } else {
            tv_code.setText("+966");
            code = "966";
        }


    }
    private void updateUi(Country country) {

        tv_code.setText(country.getDialCode());
        code = country.getDialCode();


    }
    private void checkData() {
        String m_phone = edt_phone.getText().toString().trim();
        String m_password = edt_password.getText().toString().trim();

        if (!TextUtils.isEmpty(m_phone) &&
                !TextUtils.isEmpty(m_password)&&
                !TextUtils.isEmpty(code)
        ) {
            edt_phone.setError(null);
            edt_password.setError(null);
            Common.CloseKeyBoard(activity, edt_phone);
            Login(m_phone,code, m_password);
        } else {


            if (TextUtils.isEmpty(m_phone)) {
                edt_phone.setError(getString(R.string.field_req));
            } else {
                edt_phone.setError(null);

            }


            if (TextUtils.isEmpty(m_password)) {
                edt_password.setError(getString(R.string.field_req));
            } else {
                edt_password.setError(null);

            }

            if (TextUtils.isEmpty(code)) {
                tv_code.setError(getString(R.string.field_req));
            } else {
                tv_code.setError(null);

            }
        }
    }

    private void Login(String m_phone, String code, String m_password) {
        final ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .sign_in( m_phone,code.replace("+", "00"), m_password)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null) {

                            preferences = Preferences.getInstance();
                            preferences.create_update_userdata(activity,response.body());
                            activity.NavigateToHomeActivity();
                        } else if (response.code() == 404) {
                            Common.CreateSignAlertDialog(activity,getString(R.string.inc_em_pas));
                        } else {

                            try {
                                Log.e("Error_code",response.code()+"_"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(activity,getString(R.string.something), Toast.LENGTH_SHORT).show();
                            Log.e("Error",t.getMessage());
                        } catch (Exception e) {
                        }
                    }
                });
    }


    @Override
    public void onSelectCountry(Country country) {
        updateUi(country);
    }
}
