package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class ParamPasswdActivity extends AppCompatActivity {

    private EditText mPasswdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param_passwd);

        mPasswdEditText = findViewById(R.id.password_edittext_mdp);


    }
}