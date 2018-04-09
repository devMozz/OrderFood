package com.example.milymozz.orderfood.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.milymozz.orderfood.Model.User;
import com.example.milymozz.orderfood.Remote.APIService;
import com.example.milymozz.orderfood.Remote.IGeoRetrofit;
import com.example.milymozz.orderfood.Remote.IGoogleService;
import com.example.milymozz.orderfood.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by milymozz on 2018. 2. 7..
 */

public class Common {
    public static User currentUser;

    public static String PHONE_TEXT = "userPhone";
    public static String TOPICNAME = "News";

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String INTENT_FOOD_ID = "FoodId";

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapApi() {
        return IGeoRetrofit.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    //This function will convert currency to number base on Locale
    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat) format).setParseBigDecimal(true);

        return (BigDecimal) format.parse(amount.replace("[^\\d.,]", ""));
    }

    public static String convertCodeToStatus(String status) {
        switch (status) {
            case "0":
                return "Placed";
            case "1":
                return "On my way";
            default:
                return "Shipped";
        }
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            Log.d("INFO1", "" + Arrays.toString(info));

            if (info != null) {

                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        Log.d("INFO2", "" + info[i].getState());
                    return true;
                }
            }
        }

        return false;
    }
}
