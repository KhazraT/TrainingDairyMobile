package ru.squidory.trainingdairymobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.AuthResponse;
import ru.squidory.trainingdairymobile.data.repository.AuthRepository;
import ru.squidory.trainingdairymobile.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private MaterialButton registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = AuthRepository.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegister());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString().trim() : "";

        // Валидация
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_empty_name));
            return;
        } else {
            nameInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            return;
        } else {
            emailInputLayout.setError(null);
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            return;
        } else {
            emailInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            return;
        } else if (password.length() < 8) {
            passwordInputLayout.setError(getString(R.string.error_short_password));
            return;
        } else {
            passwordInputLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_passwords_mismatch));
            return;
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        // Выполняем регистрацию
        setLoading(true);
        authRepository.register(email, password, name, null, null, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                setLoading(false);
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError(error);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String error) {
        String message = error != null && !error.isEmpty() ? error : getString(R.string.error_unknown);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
