package com.example.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddUserFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private Button btnSave;
    private String userType; // "student" or "teacher"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSave = view.findViewById(R.id.btn_save);

        // Get user type from arguments
        if (getArguments() != null) {
            userType = getArguments().getString("userType");
        }

        btnSave.setOnClickListener(v -> saveUser());

        return view;
    }

    private void saveUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Here you can push to Firebase or your database
        Toast.makeText(getActivity(), userType + " added: " + name, Toast.LENGTH_SHORT).show();

        // Optionally clear fields or navigate back
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
    }
}
