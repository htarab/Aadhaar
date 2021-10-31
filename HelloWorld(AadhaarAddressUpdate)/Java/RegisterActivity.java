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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity
{
    //Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    Button btn_send_otp, btn_confirm;
    EditText user_name, user_phone_no, user_aadhaar_no, otp;
    CountryCodePicker ccp;

    String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        btn_send_otp = findViewById(R.id.btn_send_otp);
        btn_confirm = findViewById(R.id.btn_confirm);

        user_name = findViewById(R.id.user_name);
        user_phone_no = findViewById(R.id.user_phone_no);
        user_aadhaar_no = findViewById(R.id.user_aadhaar_no);
        otp = findViewById(R.id.otp);

        ccp = findViewById(R.id.ccp);

        btn_send_otp.setOnClickListener(view -> {
            if(!user_name.getText().toString().isEmpty())
            {
                if(!user_phone_no.getText().toString().isEmpty() && user_phone_no.getText().toString().length() == 10)
                {
                    phoneNum = "+" + ccp.getSelectedCountryCode() + user_phone_no.getText().toString();

                    mFirestore.collection("Users").document(phoneNum).get().addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists())
                        {
                            user_phone_no.setError("PhoneNo Already Exists");
                        }
                        else
                        {
                            newRequestOtp(phoneNum);
                        }
                    });
                }
                else
                {
                    user_phone_no.setError("Invalid Phone No");
                }
            }
            else
            {
                user_name.setError("Invalid Name");
            }

        });

        btn_confirm.setOnClickListener(view -> {
            String oneTimePass = otp.getText().toString();

            if (otp.getText().toString().isEmpty())
            {
                otp.setError("Enter the One Time Password");
            }
            else if(otp.getText().toString().length() < 6)
            {
                otp.setError("Invalid OTP");
            }
            else
            {
                Log.d("TAG", "VerificationId " + mVerificationId);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,oneTimePass);

                newRegister(credential);
            }
        });
    }

    private void newRequestOtp(String phoneNum)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
                        {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
                            {
                                Toast.makeText(RegisterActivity.this,"verification completed",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e)
                            {
                                Toast.makeText(RegisterActivity.this, "Unable to Register" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("TAG", "Unable to Register " + e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
                            {
                                mVerificationId = verificationId;
                                mResendToken = token;

                                user_name.setVisibility(View.GONE);
                                user_phone_no.setVisibility(View.GONE);
                                user_aadhaar_no.setVisibility(View.GONE);
                                btn_send_otp.setVisibility(View.GONE);
                                ccp.setVisibility(View.GONE);
                                otp.setVisibility(View.VISIBLE);
                                btn_confirm.setVisibility(View.VISIBLE);

                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void newRegister(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        final String userid = firebaseUser.getUid();

                        String phone = phoneNum.toString();
                        String userName = user_name.getText().toString();

                        Map<String,Object> map = new HashMap<>();
                        map.put("Username", userName);
                        map.put("UserID",userid);
                        map.put("PhoneNumber",phoneNum);
                        map.put("AadhaarNumber", "123412341234");

                        mFirestore.collection("Users").document(phoneNum).set(map).addOnSuccessListener(aVoid -> {
                            Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                            i.putExtra("Phone Global", phoneNum);
                            startActivity(i);
                            finish();
                        });
                    }
                    else
                    {
                        otp.setError("Check your OTP");
                    }
                });
    }

}