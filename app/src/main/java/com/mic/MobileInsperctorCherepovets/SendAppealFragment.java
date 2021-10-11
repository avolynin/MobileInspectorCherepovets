 package com.mic.MobileInsperctorCherepovets;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.internal.GoogleApiManager;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.app.Activity.RESULT_OK;
import static android.media.tv.TvTrackInfo.TYPE_VIDEO;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SendAppealFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendAppealFragment extends Fragment implements View.OnClickListener, LocationListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    File directory;
    final int TYPE_PHOTO = 1;
    final int REQUEST_CODE_PHOTO = 1;

    private final static String TAG = "TAG";

    ImageButton ib_photo;
    ImageView iv_test;
    Button b_sendAppeal;
    ImageView iv_captcha;
    EditText it_captcha;
    ProgressBar progressBar;

    TextView tv_dataTime, tv_location;

    String url = "https://гибдд.рф/request_main";

    String region_code = "35";
    String session;
    String sputnik_session;
    String cookie;

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    public SendAppealFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SendAppealFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SendAppealFragment newInstance(String param1, String param2) {
        SendAppealFragment fragment = new SendAppealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_send_appeal, container, false);

        CookieManager cookieManager = new CookieManager(
                new PersistentCookieStore(getActivity().getApplicationContext()),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);

        CookieHandler.setDefault(cookieManager);

        iv_test = (ImageView) v.findViewById(R.id.test);
        iv_captcha = (ImageView) v.findViewById(R.id.iv_captcha);
        tv_dataTime = (TextView) v.findViewById(R.id.tv_dataTime);
        tv_location = (TextView) v.findViewById(R.id.tv_location);
        it_captcha = (EditText) v.findViewById(R.id.it_captcha);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        b_sendAppeal = (Button) v.findViewById(R.id.b_sendAppeal);
        b_sendAppeal.setOnClickListener(this);

        ib_photo = (ImageButton) v.findViewById(R.id.ib_photo);
        ib_photo.setOnClickListener(this);

        //createDirectory();

        // Текущее время
        Date currentDate = new Date();
        // Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        // Форматирование времени как "часы:минуты:секунды"
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);

        tv_dataTime.setText(dateText+" "+timeText);

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        progressBar.setVisibility(ProgressBar.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Document html = (Document) Jsoup.parse(response);
                progressBar.setVisibility(ProgressBar.GONE);
                new DownloadImageTask(iv_captcha).execute("https://гибдд.рф"+html.body().getElementsByClass("captcha-img").attr("src"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "requestQueue.onErrorResponse: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("agree", "on");
                params.put("step", "2");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> params = new HashMap<String, String>();
                params.put("content-type", "application/x-www-form-urlencoded");
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.41 " +
                        "YaBrowser/21.5.0.579 Yowser/2.5 Safari/537.36");
                params.put("cache-control", "0");
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                session = response.allHeaders.get(4).getValue().split(";")[0].split("=")[1];
                Map headers = response.headers;
                cookie = "session="+session+"; " + (String)headers.get("Set-Cookie");
                saveCookie(getActivity().getApplicationContext(), cookie);
                return super.parseNetworkResponse(response);
            }
        };

        requestQueue.add(stringRequest);

        return v;
    }

    public static void saveCookie(Context context, String cookie) {
        if (cookie == null) {
            return;
        }

        // Save in the preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (null == sharedPreferences) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cookie", cookie);
        editor.commit();
    }

    public static String getCookie(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String cookie = sharedPreferences.getString("cookie", "");
        if (cookie.contains("expires")) {
            //removeCookie(context);
            return "";
        }
        return cookie;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_photo:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
                break;
            case R.id.b_sendAppeal:
                sendPOSTRequest();
                sendConfirmEmail();
                break;
        }
    }

    private void sendPOSTRequest(){
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url_appeal = "https://гибдд.рф/request_main/pre_check_appeal";

        StringRequest stringRequestAppeal = new StringRequest(Request.Method.POST, url_appeal, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse2: "+ StringEscapeUtils.unescapeJava(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "sendPOSTRequest2: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("agree", "on");
                params.put("step", "3");
                params.put("surname", "Фамилия");
                params.put("firstname", "Имя");
                params.put("region_code", "35");
                params.put("subunit", "32");
                params.put("email", "volynin.a@mail.ru");
                params.put("message", "Текст");
                params.put("captcha", it_captcha.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> params = new HashMap<String, String>();
                params.put("cookie", cookie);
                params.put("x-requested-with", "XMLHttpRequest");
                return params;
            }
        };

        requestQueue.add(stringRequestAppeal);
    }

    private void sendConfirmEmail(){
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url_appeal = "https://xn--90adear.xn--p1ai/request_main/confirm_mail";

        StringRequest stringRequestAppeal = new StringRequest(Request.Method.POST, url_appeal, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse3: "+ StringEscapeUtils.unescapeJava(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "sendPOSTRequest3: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("patronymic", "Отчество");
                params.put("firstname", "Имя");
                params.put("email", "volynin.a@mail.ru");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> params = new HashMap<String, String>();
                params.put("cookie", cookie);
                params.put("x-requested-with", "XMLHttpRequest");
                return params;
            }
        };

        requestQueue.add(stringRequestAppeal);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Log.w(TAG, "onActivityResult: Data is null");
                } else {
                    Log.d(TAG, "onActivityResult: PhotoUri is " + data.getData());
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Object obj = data.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            //bitmap.setHeight(64);
                            //bitmap.setWidth(64);
                            Log.d(TAG, "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                            iv_test.setImageBitmap(bitmap);
                        }
                    }
                }
            }
        }
    }

    private Uri generateFileUri(int type){
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(directory.getPath() + "/" + "photo_"
                        + System.currentTimeMillis() + ".jpg");
                break;
        }
        Log.d(TAG, "fileName = " + file);
        return Uri.fromFile(file);
    }

    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyFolder");
        if (!directory.exists())
            directory.mkdirs();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Ошибка передачи изображения", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}

























