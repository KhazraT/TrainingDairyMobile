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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private MaterialButton loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = AuthRepository.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        // Валидация
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
        } else {
            passwordInputLayout.setError(null);
        }

        // Выполняем вход
        setLoading(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
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
            loginButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
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
