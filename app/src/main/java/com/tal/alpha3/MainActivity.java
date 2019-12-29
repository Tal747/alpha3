package com.tal.alpha3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";

    EditText et_email,et_phone;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        et_email = (EditText)findViewById(R.id.et_email);
        et_phone = (EditText)findViewById(R.id.et_phone);

    }


    private void createAccount(String email,String password){
        Log.d(TAG, "createAccount:"+email);
        if(!validateForm()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent t;
        if(id == R.id.menuUpdate){
            t = new Intent(this,UpdateActivity.class);
            startActivity(t);
        }
        if(id == R.id.menuGallery){
            t = new Intent(this,GalleryActivity.class);
            startActivity(t);
        }
        if(id == R.id.menuLocation){
            t = new Intent(this,LocationActivity.class);
            startActivity(t);
        }
        return super.onOptionsItemSelected(item);
    }

    public void RegisterAccount(View view) {
        createAccount(et_email.getText().toString(),et_phone.getText().toString());
        et_phone.setText("");
        et_email.setText("");
    }

    private boolean validateForm() {
        //Validate if email and password are not missing

        boolean valid = true;

        String email = et_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            et_email.setError("Required.");
            valid = false;
        } else {
            et_email.setError(null);
        }

        String password = et_phone.getText().toString();
        if (TextUtils.isEmpty(password)) {
            et_phone.setError("Required.");
            valid = false;
        } else {
            et_phone.setError(null);
        }

        return valid;
    }
}
