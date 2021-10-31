package com.example.aadhaar.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aadhaar.Object.UserObject;
import com.example.aadhaar.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity
{
    Button btn_address_change, btn_requests, btn_logout;
    TextView txt_user_Name, txt_user_aadhaar;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        assert firebaseUser != null;
        final String phoneNo = firebaseUser.getPhoneNumber();

        btn_address_change = findViewById(R.id.btn_address_change);
        btn_requests = findViewById(R.id.btn_requests);
        btn_logout = findViewById(R.id.btn_logout);
        txt_user_Name = findViewById(R.id.txt_user_Name);

        notificationManager = NotificationManagerCompat.from(this);

        assert phoneNo != null;
        mFirestore.collection("Users").document(phoneNo).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                UserObject userObject = documentSnapshot.toObject(UserObject.class);

                assert userObject != null;
                txt_user_Name.setText(userObject.getUsername());
            }
        });

        btn_address_change.setOnClickListener(view -> {
            Intent statusIntent = new Intent(HomeActivity.this, StatusActivity.class);
            startActivity(statusIntent);
        });

        btn_requests.setOnClickListener(view -> {
            Intent requestIntent = new Intent(HomeActivity.this, RequestsActivity.class);
            startActivity(requestIntent);
            //send();

        });

        btn_logout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Logout").setMessage("Are you sure, you want to Logout?");
            builder.setPositiveButton("Yes", (dialog, id) -> {
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, StartActivity.class);
                startActivity(intent);
                deleteFCMToken();
                finish();
            });
            builder.setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert11 = builder.create();
            alert11.show();
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null)
            {
                sendFMCTokenToDatabase(task.getResult().getToken());
            }
        });
    }

    public void sendFMCTokenToDatabase(String token)
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection("Users").document(Objects.requireNonNull(firebaseUser.getPhoneNumber()));

        documentReference.update("FMC_Token", token).addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Token Updated Successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Unable To Send Token" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void deleteFCMToken()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("FMC_Token", FieldValue.delete());

        mFirestore.collection("Users").document(Objects.requireNonNull(firebaseUser.getPhoneNumber())).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HomeActivity.this, "FMC Token Deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Unsuccessful" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}