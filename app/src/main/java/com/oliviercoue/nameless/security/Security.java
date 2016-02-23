package com.oliviercoue.nameless.security;

import android.util.Base64;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.activities.StartActivity;
import com.oliviercoue.nameless.api.NamelessRestClient;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 23/02/2016.
 */
public class Security {

    private String api_key = "v6d8ia1dtqsiegdkyr54mizb74pn6li8h2662eplun";
    private SecurityImp securityImp;

    public Security(StartActivity startActivity){
        securityImp = startActivity;
    }

    public void authentication() {
        NamelessRestClient.getAuthentificationKey(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                try {
                    HashMap<String, String> paramMap = new HashMap<>();
                    paramMap.put("x_key", RSAEncrypt(response.getString("public_key")));
                    RequestParams params = new RequestParams(paramMap);
                    NamelessRestClient.get("chat/hello", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, final JSONObject response) {
                            securityImp.onAuthenticationSuccess();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String RSAEncrypt(String encodedKey) throws Exception {

        encodedKey = encodedKey.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
        byte[] keyBytes = Base64.decode(encodedKey, Base64.NO_WRAP);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        byte[] cipherText = cipher.doFinal(Base64.decode(api_key, Base64.NO_WRAP));

        return Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

}
