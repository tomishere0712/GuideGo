package com.example.guidego.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply();
        decodeAndSaveClaims(token);
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty() && !isTokenExpired(token);
    }

    public void clearToken() {
        prefs.edit().clear().apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "Người dùng");
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, Constants.ROLE_TOURIST);
    }

    public String getUserPhone() {
        return prefs.getString(Constants.KEY_USER_PHONE, "");
    }

    /** Returns guide_id extracted from JWT (non-empty only for Guide role). */
    public String getGuideId() {
        return prefs.getString("guide_id", "");
    }

    public void saveUserPhone(String phone) {
        prefs.edit().putString(Constants.KEY_USER_PHONE, phone).apply();
    }

    public boolean isTourist() {
        return Constants.ROLE_TOURIST.equals(getUserRole());
    }

    public boolean isGuide() {
        return Constants.ROLE_GUIDE.equals(getUserRole());
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(getUserRole());
    }

    private void decodeAndSaveClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return;

            // Add padding if needed.
            // Do NOT combine with NO_PADDING flag — that flag corrupts decoding when '=' chars are present.
            String payload = parts[1];
            int mod4 = payload.length() % 4;
            if (mod4 != 0) payload += "====".substring(mod4);

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String jsonStr = new String(decodedBytes, "UTF-8");
            Log.d(TAG, "JWT payload JSON: " + jsonStr);
            JSONObject json = new JSONObject(jsonStr);

            // Extract fields using explicit has()/getString() to avoid optString() quirks
            String userId = extractString(json, "id", "nameid", "sub",
                    "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");

            String role = extractString(json, "role",
                    "http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
            if (role.isEmpty()) role = Constants.ROLE_TOURIST;

            String email = extractString(json, "email",
                    "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");

            String name = extractString(json, "full_name", "unique_name", "name", "given_name");

            // guide_id is present in JWT for Guide role
            String guideId = extractString(json, "guide_id");

            prefs.edit()
                    .putString(Constants.KEY_USER_ID, userId)
                    .putString(Constants.KEY_USER_ROLE, role)
                    .putString(Constants.KEY_USER_EMAIL, email)
                    .putString(Constants.KEY_USER_NAME, name)
                    .putString("guide_id", guideId)
                    .apply();

            Log.d(TAG, "Decoded JWT: userId=" + userId + ", role=" + role + ", email=" + email);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode JWT", e);
        }
    }

    /** Returns the first non-empty string value found for any of the given keys, or "" if none. */
    private String extractString(JSONObject json, String... keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    String val = json.getString(key);
                    if (val != null && !val.isEmpty()) return val;
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;
            String payload = parts[1];
            int mod4 = payload.length() % 4;
            if (mod4 != 0) payload += "====".substring(mod4);
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String jsonStr = new String(decodedBytes, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);
            long exp = json.optLong("exp", 0);
            if (exp == 0) return false;
            long now = System.currentTimeMillis() / 1000;
            return now >= exp;
        } catch (Exception e) {
            return false;
        }
    }
}

