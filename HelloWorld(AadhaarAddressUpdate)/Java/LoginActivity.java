package com.example.aadhaar.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aadhaar.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity
{
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    String VerificationId;
    PhoneAuthProvider.ForceResendingToken token;
    String phoneNum;

    EditText user_phone_no, otp;
    Button btn_send_otp, btn_confirm;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        user_phone_no = findViewById(R.id.user_phone_no);
        otp = findViewById(R.id.otp);
        btn_send_otp = findViewById(R.id.btn_send_otp);
        btn_confirm = findViewById(R.id.btn_confirm);
        ccp = findViewById(R.id.ccp);

        btn_send_otp.setOnClickListener(view -> {

            if(!user_phone_no.getText().toString().isEmpty() && user_phone_no.getText().toString().length() == 10)
            {
                phoneNum = "+" + ccp.getSelectedCountryCode() + user_phone_no.getText().toString();

                mFirestore.collection("Users").document(phoneNum).get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists())
                    {
                        requestOTP(phoneNum);
                    }
                    else
                    {
                        user_phone_no.setError("PhoneNo Does Not Exists");
                    }
                });

            }
            else
            {
                user_phone_no.setError("Invalid Phone No");
            }
        });

        btn_confirm.setOnClickListener(view -> {
            String oneTimePass = otp.getText().toString();

            if (user_phone_no.getText().toString().isEmpty())
            {
                user_phone_no.setError("Empty");
            }
            else if(user_phone_no.getText().toString().length() < 6)
            {
                user_phone_no.setError("Invalid OTP");
            }
            else
            {
                Log.d("TAG", "VerificationId " + VerificationId);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationId,oneTimePass);

                login(credential);
            }
        });
    }

    private void requestOTP(String phoneNum)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);

                                btn_send_otp.setVisibility(View.GONE);
                                btn_confirm.setVisibility(View.VISIBLE);

                                otp.setVisibility(View.VISIBLE);
                                user_phone_no.setVisibility(View.GONE);
                                ccp.setVisibility(View.GONE);

                                VerificationId = s;
                                token = forceResendingToken;
                            }

                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                                super.onCodeAutoRetrievalTimeOut(s);
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Toast.makeText(LoginActivity.this,"verification completed",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, "Unable to Register" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void login(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }
            else
            {
                otp.setError("Check your OTP");
            }
        });
    }
}