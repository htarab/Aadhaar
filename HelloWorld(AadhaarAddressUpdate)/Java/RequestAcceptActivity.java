package com.example.aadhaar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.aadhaar.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestAcceptActivity extends AppCompatActivity
{
    EditText aadharNumber;
    EditText secretCode;
    EditText captchaCode;
    Button submitButton;
    LinearLayout linearLayout;
    Button button;
    EditText otpInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_accept);

        aadharNumber = findViewById(R.id.aadharnumber);
        secretCode = findViewById(R.id.secretcode);
        captchaCode = findViewById(R.id.captchacode);
        submitButton = findViewById(R.id.button_parse);
        linearLayout = findViewById(R.id.layout_id);
        button = findViewById(R.id.submit_otp);
        otpInput = findViewById(R.id.otp_response);

        final String[] captchaResponse = new String[1];

        Thread thread = new Thread(() -> {
            try  {
                try {
                    captchaResponse[0] = CaptchaRequest();
                    System.out.println(captchaResponse[0]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String myResponse = captchaResponse[0];
        Bitmap finalDecodedByte = captcha_response(myResponse);
        String finalCaptchaTxnId = captchaTxnResponse(myResponse);

        ImageView image = findViewById(R.id.imageview);
        image.setImageBitmap(Bitmap.createScaledBitmap(finalDecodedByte, 300, 100, false));
        final String[] response = new String[1];
        final String[] shareCode = new String[1];
        Bundle bundle = new Bundle();

        submitButton.setOnClickListener(v -> {
            String uidNumber = aadharNumber.getText().toString();
            String captchaValue = captchaCode.getText().toString();
            shareCode[0] = secretCode.getText().toString();
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();

            Thread thread1 = new Thread(() -> {
                try  {
                    try {
                        response[0] = OtpRequest(finalCaptchaTxnId,uidNumber,captchaValue,uuidAsString);
                        System.out.println(response[0]);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();

            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String myresponse = response[0];
            String[] otpresponse = otpResponse(myresponse);
            String status = otpresponse[0];
            String txnId = otpresponse[1];

            if(status.equals("Success")){
                linearLayout.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                otpInput.setVisibility(View.VISIBLE);
            }
            else{
                Toast.makeText(getApplicationContext(),"Hello Javatpoint", Toast.LENGTH_SHORT).show();
            }

            System.out.println(otpresponse[1]);
            bundle.putString("txnId",otpresponse[1]);

        });

        final String[] ekycresponse = new String[1];

        button.setOnClickListener(v -> {
            String otp = otpInput.getText().toString();
            String myresponse = response[0];
            String[] otpresponse = otpResponse(myresponse);
            String status = otpresponse[0];
            String message = otpresponse[2];
            String uidNumber = otpresponse[3];
            System.out.println(otpresponse[1]);
            String txnId = bundle.getString("txnId");

            Thread thread12 = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        try {
                            ekycresponse[0] = FetchEKYCRequest(txnId,otp,shareCode[0],uidNumber);
                            System.out.println(ekycresponse[0]);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread12.start();

            try {
                thread12.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String fileresponse = ekycresponse[0];

            System.out.println(fileresponse);
            String FileBase64 = FetchEKYCResponse(fileresponse);
            FileOutputStream fos = null;
            try {
                //FileOutputStream fos = new FileOutputStream(file);
                fos = openFileOutput("output.zip",MODE_PRIVATE);
                byte[] outputByte = Base64.decode(FileBase64, Base64.DEFAULT);
                fos.write(outputByte);
                System.out.println("successfully written" + getFilesDir() + "/" + "output.zip");

            } catch (IOException e) {
                e.printStackTrace();
            }
            String filepath = getFilesDir() + "/output.zip";
            String sharecode = shareCode[0];
            bundle.putString("FileBase64",FileBase64);

            try {
                unCompressPasswordProtectedFiles(filepath,sharecode);
            } catch (ZipException e) {
                e.printStackTrace();
            }

        });
    }

    private void unCompressPasswordProtectedFiles(String sourcePath,String shareCode) throws ZipException {
        String destPath = getFileName(sourcePath);
        System.out.println("Destination " + destPath);
        ZipFile zipFile = new ZipFile(sourcePath);
        // If it is encrypted then provide password
        if(zipFile.isEncrypted()){
            zipFile.setPassword(shareCode);
        }
        zipFile.extractAll(destPath);
    }

    private String getFileName(String filePath)
    {
        return filePath.substring(0, filePath.lastIndexOf("."));
    }

    public Bitmap captcha_response(String myResponse){
        JSONObject root = null;
        String captchaBase64String = null;
        Bitmap decodedByte = null;
        String captchaTxnId = null;
        try {
            root = new JSONObject(myResponse);
            String status = root.getString("status");
            captchaBase64String = root.getString("captchaBase64String");
            captchaTxnId = root.getString("captchaTxnId");
            System.out.println(status + captchaBase64String + "    " + captchaTxnId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String base64String = captchaBase64String;
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap finalDecodedByte = decodedByte;

        String finalCaptchaTxnId = captchaTxnId;
        return finalDecodedByte;
    }

    public String captchaTxnResponse(String myResponse){
        JSONObject root = null;
        String captchaTxnId = null;
        try {
            root = new JSONObject(myResponse);
            captchaTxnId = root.getString("captchaTxnId");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalCaptchaTxnId = captchaTxnId;
        return finalCaptchaTxnId;
    }

    public String[] otpResponse(String myResponse){

        JSONObject root = null;
        String txnId = null;
        String message = null;
        String status = null;
        String uidNumber = null;

        try
        {
            root = new JSONObject(myResponse);
            status = root.getString("status");
            message = root.getString("message");

            if (status.equals("Success"))
            {
                uidNumber = root.getString("uidNumber");
                txnId = root.getString("txnId");
                System.out.println(status + txnId +"    " + message);
            }
            else
            {
                System.out.println(status);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        String[] otpresponse = {status,txnId,message,uidNumber};
        return otpresponse;
    }

    public String FetchEKYCRequest(String txnNumber,String otp,String shareCode,String uid) throws IOException
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("txnNumber", txnNumber);
            jsonObject.put("otp", otp);
            jsonObject.put("shareCode",shareCode);
            jsonObject.put("uid",uid);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://stage1.uidai.gov.in/eAadhaarService/api/downloadOfflineEkyc")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        okhttp3.Response response = client.newCall(request).execute();

        return response.body().string();
    }

    public String FetchEKYCResponse(String myResponse){
        JSONObject root = null;
        String ekycXML = null;
        try {
            root = new JSONObject(myResponse);
            ekycXML = root.getString("eKycXML");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalEkycXML = ekycXML;
        return finalEkycXML;
    }

    public String OtpRequest(String captchaTxnId,String uidNumber,String captchaValue,String transactionId) throws IOException
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("uidNumber", uidNumber);
            jsonObject.put("captchaTxnId", captchaTxnId);
            jsonObject.put("captchaValue",captchaValue);
            jsonObject.put("transactionId","MYAADHAAR:" + transactionId);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/generate/aadhaar/otp")
                .method("POST", body)
                .addHeader("appid", "MYAADHAAR")
                .addHeader("Accept-Language", "en_in")
                .addHeader("x-request-id", transactionId)
                .addHeader("Content-Type", "application/json")
                .build();
        okhttp3.Response response = client.newCall(request).execute();

        return response.body().string();

    }

    public String CaptchaRequest() throws IOException
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            jsonObject.put("langCode", "en");
            jsonObject.put("captchaLength", "3");
            jsonObject.put("captchaType","2");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/get/captcha")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }
}
