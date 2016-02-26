package com.oliviercoue.nameless.network.security;

import android.util.Base64;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.network.NamelessRestClient;
import com.oliviercoue.nameless.network.session.SessionManager;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 23/02/2016.
 *
 */
public class Security {

    private String ka = "bXSkSuMLSz8sQZUdbBF5KEiL4C8DeS";
    private String kb = "VP0vrOQwDXXbIGoqS7cBOUVBe7j6aj69";
    private String kc = "sMK4qm3lhanp967ZcXeIt5CfePxIhAwUeLV0rUF6oAb0";
    private SecurityImp securityImp;

    public Security(SessionManager startNetwork){
        securityImp = startNetwork;
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
        byte[] cipherText = cipher.doFinal(Base64.decode(ka+kb+kc, Base64.NO_WRAP));

        return Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

}
