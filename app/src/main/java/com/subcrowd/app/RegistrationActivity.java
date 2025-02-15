package com.subcrowd.app;

import android.content.Intent;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;
    private ProgressBar spinner;
    private EditText mEmail, mPassword, mName, mBudget;

    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String TAG = "RegisterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        spinner = (ProgressBar)findViewById(R.id.pBar);
        spinner.setVisibility(View.GONE);
        TextView existing = (TextView) findViewById(R.id.existing);
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                spinner.setVisibility(View.VISIBLE);
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user !=null && user.isEmailVerified()){
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    spinner.setVisibility(View.GONE);
                    return;
                }
                spinner.setVisibility(View.GONE);

            }
        };


        existing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent btnClick = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(btnClick);
                finish();
                return;
            }
        });
        //mBudget = (EditText) findViewById(R.id.budget);
        mRegister = (Button) findViewById(R.id.register);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mName = (EditText) findViewById(R.id.name);
        final CheckBox checkbox = (CheckBox)findViewById(R.id.checkBox1);
        TextView textView = (TextView)findViewById(R.id.textView2);

        checkbox.setText("");
        textView.setText(Html.fromHtml("I have read and agree to the " +
                "<a href='https://www.websitepolicies.com/policies/view/nizu0aVh'>Terms & Conditions</a>"));
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

//        final Spinner spinner_need = (Spinner) findViewById(R.id.spinner_need);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.services, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinner_need.setAdapter(adapter);
//        spinner_need.setSelection(0);
//        final Spinner spinner_give = (Spinner) findViewById(R.id.spinner_give);
//        ArrayAdapter<CharSequence> adapter_give = ArrayAdapter.createFromResource(this,
//                R.array.services, android.R.layout.simple_spinner_item);
//        adapter_give.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinner_give.setAdapter(adapter_give);
//        spinner_give.setSelection(0);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                spinner.setVisibility(View.VISIBLE);

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                final Boolean tnc = checkbox.isChecked();
                //final String budget = mBudget.getText().toString();

                if (checkInputs(email, name, password, tnc)) {

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(RegistrationActivity.this, "Registration successfully. Please check your email for verification. ", Toast.LENGTH_LONG).show();
                                            String userId = mAuth.getCurrentUser().getUid();
                                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                                            Log.d("DB_debug", FirebaseDatabase.getInstance().getReference().getDatabase() + "");
                                            Map userInfo = new HashMap<>();
                                            userInfo.put("name", name);
                                            userInfo.put("profileImageUrl", "default");
                                            currentUserDb.updateChildren(userInfo);

                                            //clear the fields
                                            mEmail.setText("");
                                            mName.setText("");
                                            mPassword.setText("");
                                            Intent btnClick = new Intent(RegistrationActivity.this, ChooseLoginRegistrationActivity.class);
                                            startActivity(btnClick);
                                            finish();
                                            return;

                                        } else {
                                            Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });


                            }
                        }
                    });
                }
                spinner.setVisibility(View.GONE);

            }
        });
    }

    private boolean checkInputs(String email, String username, String password, Boolean tnc) {
        Log.d(TAG, "checkInputs: checking inputs for null values.");
        if (email.equals("") || username.equals("") || password.equals("")) {
            Toast.makeText(RegistrationActivity.this, "All fields must be filed out.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Below code checks if the email id is valid or not.
        if (!email.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid email address, enter valid email id and click on Continue", Toast.LENGTH_SHORT).show();
            return false;

        }

        if(!tnc){
            Toast.makeText(getApplicationContext(), "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        Intent btnClick = new Intent(RegistrationActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(btnClick);
        finish();
    }
}
