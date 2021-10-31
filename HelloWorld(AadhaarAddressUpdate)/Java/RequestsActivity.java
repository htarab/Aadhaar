package com.example.aadhaar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.aadhaar.Adapter.RequestsAdapter;
import com.example.aadhaar.Object.SendRequestObject;
import com.example.aadhaar.Object.UserObject;
import com.example.aadhaar.R;
import com.example.aadhaar.Utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RequestsActivity extends AppCompatActivity
{
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    RecyclerView recycler_requests;
    List<SendRequestObject> sendRequestObjects;
    private final HashSet<String> hashSet = new HashSet<String>();

    RequestsAdapter requestsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        final String phoneNo = firebaseUser.getPhoneNumber();

        recycler_requests = findViewById(R.id.recycler_requests);
        recycler_requests.setHasFixedSize(true);
        recycler_requests.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sendRequestObjects = new ArrayList<>();

        assert phoneNo != null;
        mFirestore.collection("Users").document(phoneNo).collection("Requests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                sendRequestObjects.clear();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {

                    SendRequestObject sendRequestObject = documentSnapshot.toObject(SendRequestObject.class);

                    if (!hashSet.contains(sendRequestObject.getPhoneNumber()))
                    {
                        hashSet.add(sendRequestObject.getPhoneNumber());
                        sendRequestObjects.add(sendRequestObject);
                    }


                    Log.d("TAG", "UserObject " + sendRequestObject.getPhoneNumber());
                    Log.d("TAG", "MyUserObject " + phoneNo);

                }

                requestsAdapter = new RequestsAdapter(getApplicationContext(), sendRequestObjects);
                requestsAdapter.notifyDataSetChanged();
                recycler_requests.setAdapter(requestsAdapter);

                //saveData(context);
            }
        });
    }
}