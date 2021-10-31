package com.example.aadhaar.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aadhaar.Listener.UsersListener;
import com.example.aadhaar.Network.ApiClient;
import com.example.aadhaar.Network.ApiService;
import com.example.aadhaar.Object.SendRequestObject;
import com.example.aadhaar.Object.UserObject;
import com.example.aadhaar.R;
import com.example.aadhaar.Utilities.Constants;
import com.example.aadhaar.Utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.StorageReference;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusActivity extends AppCompatActivity //implements UsersListener
{
    private FirebaseFirestore mFirestore;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    UserObject userObject;

    Button btn_confirm;
    EditText user_phone_no;
    CountryCodePicker ccp;

    Boolean requestOngoing = false;

    RelativeLayout relativeLayout, relativeLayout2;
    Button btn_request_again;

    private UsersListener usersListener;

    private NotificationManagerCompat notificationManager;

    private PreferenceManager preferenceManager;
    private String inviterToken = null;

    private final HashSet<String> hashSet = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        String userid = firebaseUser.getUid();
        String userPhoneNo = firebaseUser.getPhoneNumber();

        btn_confirm = findViewById(R.id.btn_confirm);
        user_phone_no = findViewById(R.id.user_phone_no);
        ccp = findViewById(R.id.ccp);

        relativeLayout = findViewById(R.id.relativeLayout);
        relativeLayout2 = findViewById(R.id.relativeLayout2);
        btn_request_again = findViewById(R.id.btn_request_again);

        mFirestore.collection("Users").document(userPhoneNo).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                UserObject userObject = documentSnapshot.toObject(UserObject.class);

                if(userObject.getRequestTo() != null)
                {
                    relativeLayout.setVisibility(View.GONE);
                    relativeLayout2.setVisibility(View.VISIBLE);
                }

                /*mFirestore.collection("Users").document(userObject.getPhoneNumber()).collection("Requests").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error)
                    {
                        for (QueryDocumentSnapshot documentSnapshot2 : value)
                        {
                            SendRequestObject sendRequestObject = documentSnapshot2.toObject(SendRequestObject.class);

                            Log.d("TAG", "RequestStatus " + documentSnapshot2.get("phoneNumber"));

                            if(sendRequestObject.getPhoneNumber().equals(userPhoneNo))
                            {
                                String documentID = documentSnapshot2.getId();
                                mFirestore.collection("Users").document(userObject.getPhoneNumber()).collection("Requests").document(documentID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot3)
                                    {
                                        SendRequestObject sendRequestObject1 = documentSnapshot3.toObject(SendRequestObject.class);
                                        Log.d("TAG", "RequestStatus " + sendRequestObject1.getRequestStatus());
                                        if(sendRequestObject1.getRequestStatus().equals("Request Denied"))
                                        {
                                            relativeLayout.setVisibility(View.GONE);
                                            relativeLayout2.setVisibility(View.VISIBLE);
                                            btn_request_again.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });*/
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String phoneNo = "+91" + user_phone_no.getText().toString();

                mFirestore.collection("Users").document(phoneNo).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        if(documentSnapshot.exists())
                        {
                            UserObject userObject = documentSnapshot.toObject(UserObject.class);

                            //conform(username, phoneNumber, aadhaarNumber, mUserID, userid);
                            conform(userObject, userid, userPhoneNo);

                            //Log.d("TAG", "UserName " + phoneNumber);
                        }
                        else
                        {
                            user_phone_no.setError("PhoneNo Does Not Exists");
                            Log.d("TAG", "UserName " + phoneNo);
                        }
                    }
                });
            }
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if(task.isSuccessful() && task.getResult() != null)
                {
                    inviterToken = task.getResult().getToken();
                }
            }
        });

        notificationManager = NotificationManagerCompat.from(this);
    }

    public void conform(UserObject userObject, String userid, String userPhoneNo)
    {
        String username = userObject.getUsername();
        String phoneNumber = userObject.getPhoneNumber();
        String aadhaarNumber = userObject.getAadhaarNumber();
        String mUserID = userObject.getUserID();

        if(!mUserID.equals(userid))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(StatusActivity.this);

            View view = LayoutInflater.from(StatusActivity.this).inflate(R.layout.alert_layout, null);

            TextView name = view.findViewById(R.id.name);
            TextView phoneNo = view.findViewById(R.id.phoneNo);
            TextView aadhaarNo = view.findViewById(R.id.aadhaarNo);

            name.setText("Name : " + username);
            phoneNo.setText("Phone No. : " + phoneNumber);
            aadhaarNo.setText("Aadhaar No. : " + aadhaarNumber);

            builder.setTitle("Logout").setMessage("Are the below Information correct?").setView(view);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    mFirestore.collection("Users").document(userPhoneNo).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            if(documentSnapshot.exists())
                            {
                                SendRequestObject sendRequestObject = documentSnapshot.toObject(SendRequestObject.class);

                                sendRequestObject.setRequestStatus("Request Sent");

                                if(!hashSet.contains(sendRequestObject.getPhoneNumber()))
                                {
                                    hashSet.add(sendRequestObject.getPhoneNumber());
                                    mFirestore.collection("Users").document(phoneNumber).collection("Requests").add(sendRequestObject);
                                    mFirestore.collection("Users").document(userPhoneNo).update("RequestTo", phoneNumber);
                                    initiateVideoMeeting(userObject);
                                    initiateMeeting("Requests", userObject.getFMC_Token(), userObject.getUsername());
                                }

                                Log.d("TAG", "Name " + Constants.KEY_FIRST_NAME + " " + userObject.getUsername());

                                /*else
                                {
                                    Toast.makeText(StatusActivity.this, "Request has already been sent.", Toast.LENGTH_SHORT).show();
                                }*/

                            }
                            else
                            {
                                user_phone_no.setError("PhoneNo Does Not Exists");
                                Log.d("TAG", "UserName " + phoneNo);
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder.create();
            alert11.show();
        }
        else
        {
            Toast.makeText(this, "You cannot request Yourself", Toast.LENGTH_SHORT).show();
        }
    }

    public void initiateVideoMeeting(UserObject user)
    {
        if(user.getFMC_Token() == null || user.getFMC_Token().trim().isEmpty())
        {
            Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "Not Available");
        }
        else
        {
            Toast.makeText(this, "Available", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "Available");
        }
    }

    private void initiateMeeting(String meetingType, String receiverToken, String receiverName)
    {
        try
        {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, "Hello");
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            data.put("NotificationReceiverName", receiverName);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION, user_phone_no.getText().toString());

        }
        catch (Exception exception)
        {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type, String user_phone_no)
    {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(Constants.getRemoteMessageHeaders(), remoteMessageBody)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                    {
                        if(response.isSuccessful())
                        {
                            if(type.equals(Constants.REMOTE_MSG_INVITATION))
                            {
                                Toast.makeText(StatusActivity.this, "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();

                                /*mFirestore.collection("Users").document(user_phone_no).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot)
                                    {
                                        if(documentSnapshot.exists())
                                        {
                                            *//*SendRequestObject sendRequestObject = documentSnapshot.toObject(SendRequestObject.class);

                                            assert sendRequestObject != null;
                                            sendRequestObject.setRequestStatus("Request Received by " + sendRequestObject.getUsername());*//*
                                        }
                                    }
                                });*/

                                relativeLayout.setVisibility(View.GONE);
                                relativeLayout2.setVisibility(View.VISIBLE);

                            }
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                    {
                        Toast.makeText(StatusActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }

//    public void request(String phoneNumber)
//    {
//        mFirestore.collection("Users").document(phoneNumber).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//        {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot)
//            {
//                if(documentSnapshot.exists())
//                {
//                    Log.d("TAG", "Notification " + phoneNumber);
//                    Notification notification = new NotificationCompat.Builder(StatusActivity.this, CHANNEL_1_ID)
//                            .setSmallIcon(R.drawable.ic_launcher_foreground)
//                            .setContentTitle("Requests")
//                            .setContentText("Address Update Request from")
//                            .setPriority(NotificationCompat.PRIORITY_HIGH)
//                            .build();
//
//                    notificationManager.notify(1, notification);
//                }
//                else
//                {
//                    user_phone_no.setError("PhoneNo Does Not Exists");
//                    Log.d("TAG", "UserName " + phoneNumber);
//                }
//            }
//        });
//    }

    /*public void sendChannel1(View v)
    {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Requests")
                .setContentText("Address Update Request from")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(1, notification);
    }*/
}