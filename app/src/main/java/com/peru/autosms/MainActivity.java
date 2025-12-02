package com.peru.autosms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private TextInputEditText phoneInput;
    private TextInputEditText intervalInput;
    private MaterialButton sendSmsButton;
    private MaterialButton startRepeatButton;
    private MaterialButton stopRepeatButton;
    private TextView statusText;
    private TextView messagesPreview;

    // Handler for repeat functionality
    private Handler repeatHandler;
    private Runnable repeatRunnable;
    private boolean isRepeating = false;
    private int repeatIntervalSeconds = 10;
    private int smsCount = 0;

    // 5 frases personalizadas para Perú
    private final String[] messages = {
            "¡Hola! Espero que estés teniendo un día increíble. ¡Saludos desde Perú! 🇵🇪",
            "¡Qué tal! Solo quería enviarte un saludo y desearte mucha suerte en todo. ¡Arriba Perú!",
            "¡Hola amigo/a! Que tengas un excelente día lleno de bendiciones. ¡Un abrazo!",
            "¡Saludos cordiales! Espero que todo te esté yendo muy bien. ¡Éxitos siempre!",
            "¡Hola! Solo un mensaje para recordarte que eres increíble. ¡Que tengas un gran día!"
    };

    private String sentToNumber = "";
    private String sentMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repeatHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        displayAvailableMessages();
        setupSendButton();
        setupRepeatButtons();
    }

    private void initViews() {
        phoneInput = findViewById(R.id.phoneInput);
        intervalInput = findViewById(R.id.intervalInput);
        sendSmsButton = findViewById(R.id.sendSmsButton);
        startRepeatButton = findViewById(R.id.startRepeatButton);
        stopRepeatButton = findViewById(R.id.stopRepeatButton);
        statusText = findViewById(R.id.statusText);
        messagesPreview = findViewById(R.id.messagesPreview);
    }

    private void displayAvailableMessages() {
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            preview.append((i + 1)).append(". ").append(messages[i]).append("\n\n");
        }
        messagesPreview.setText(preview.toString());
    }

    private void setupSendButton() {
        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString().trim();

                if (validatePhoneNumber(phoneNumber)) {
                    if (checkSmsPermission()) {
                        sendRandomSms(phoneNumber);
                    } else {
                        requestSmsPermission();
                    }
                } else {
                    statusText.setText(R.string.invalid_phone);
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                    Toast.makeText(MainActivity.this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupRepeatButtons() {
        startRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString().trim();
                String intervalStr = intervalInput.getText().toString().trim();

                if (!validatePhoneNumber(phoneNumber)) {
                    statusText.setText(R.string.invalid_phone);
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                    Toast.makeText(MainActivity.this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (intervalStr.isEmpty()) {
                    intervalInput.setText("10");
                    intervalStr = "10";
                }

                int interval = Integer.parseInt(intervalStr);
                if (interval < 10) {
                    statusText.setText(R.string.invalid_interval);
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                    Toast.makeText(MainActivity.this, R.string.invalid_interval, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkSmsPermission()) {
                    requestSmsPermission();
                    return;
                }

                startRepeatMode(phoneNumber, interval);
            }
        });

        stopRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRepeatMode();
            }
        });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        // En Perú, los números de celular tienen 9 dígitos y empiezan con 9
        return phoneNumber.length() == 9 && phoneNumber.matches("[0-9]+") && phoneNumber.startsWith("9");
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Por favor, intente enviar de nuevo.",
                        Toast.LENGTH_SHORT).show();
            } else {
                statusText.setText(R.string.permission_denied);
                statusText.setTextColor(ContextCompat.getColor(this, R.color.red));
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendRandomSms(String phoneNumber) {
        // Seleccionar una frase aleatoria entre las 5 disponibles
        Random random = new Random();
        int randomIndex = random.nextInt(messages.length);
        String selectedMessage = messages[randomIndex];

        sentToNumber = phoneNumber;
        sentMessage = selectedMessage;

        try {
            // Registrar receptores para saber si el SMS fue enviado y entregado
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);

            BroadcastReceiver sentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case RESULT_OK:
                            String successMsg = "✓ SMS enviado correctamente\n" +
                                    "Número: " + sentToNumber + "\n" +
                                    "Mensaje: \"" + sentMessage + "\"";
                            statusText.setText(successMsg);
                            statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.green));
                            Toast.makeText(MainActivity.this, R.string.sms_sent,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            showError("Error genérico al enviar");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            showError("Sin servicio");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            showError("Error PDU nulo");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            showError("Radio apagada");
                            break;
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(sentReceiver, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(sentReceiver, new IntentFilter(SENT));
            }

            BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case RESULT_OK:
                            Toast.makeText(MainActivity.this, "SMS entregado",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case RESULT_CANCELED:
                            Toast.makeText(MainActivity.this, "SMS no entregado",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));
            }

            // Enviar el SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, selectedMessage, sentPI, deliveredPI);

            // Mostrar información inmediata
            String sendingMsg = "📤 Enviando SMS...\n" +
                    "Número: " + phoneNumber + "\n" +
                    "Mensaje seleccionado (#" + (randomIndex + 1) + "):\n\"" + selectedMessage + "\"";
            statusText.setText(sendingMsg);
            statusText.setTextColor(ContextCompat.getColor(this, R.color.purple_700));

        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void showError(String error) {
        String errorMsg = "✗ " + error + "\n" +
                "Número: " + sentToNumber;
        statusText.setText(errorMsg);
        statusText.setTextColor(ContextCompat.getColor(this, R.color.red));
        Toast.makeText(this, R.string.sms_failed + ": " + error, Toast.LENGTH_LONG).show();
    }

    private void startRepeatMode(final String phoneNumber, int intervalSeconds) {
        isRepeating = true;
        repeatIntervalSeconds = intervalSeconds;
        smsCount = 0;

        // Disable start button, enable stop button
        startRepeatButton.setEnabled(false);
        stopRepeatButton.setEnabled(true);
        sendSmsButton.setEnabled(false);
        phoneInput.setEnabled(false);
        intervalInput.setEnabled(false);

        // Send first SMS immediately
        sendRandomSms(phoneNumber);
        smsCount++;

        // Setup repeat runnable with countdown
        repeatRunnable = new Runnable() {
            int countdown = repeatIntervalSeconds;

            @Override
            public void run() {
                if (!isRepeating) {
                    return;
                }

                if (countdown > 0) {
                    // Update countdown
                    String countMsg = String.format(getString(R.string.repeat_active), countdown) + 
                                     "\n" + String.format(getString(R.string.repeat_count), smsCount);
                    statusText.setText(countMsg);
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.purple_700));
                    countdown--;
                    repeatHandler.postDelayed(this, 1000); // Update every second
                } else {
                    // Send SMS
                    sendRandomSms(phoneNumber);
                    smsCount++;
                    countdown = repeatIntervalSeconds;
                    repeatHandler.postDelayed(this, 1000);
                }
            }
        };

        // Start countdown after first SMS
        repeatHandler.postDelayed(repeatRunnable, 1000);

        Toast.makeText(this, "Envío repetido iniciado cada " + intervalSeconds + " segundos", 
                      Toast.LENGTH_LONG).show();
    }

    private void stopRepeatMode() {
        isRepeating = false;

        if (repeatHandler != null && repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
        }

        // Re-enable controls
        startRepeatButton.setEnabled(true);
        stopRepeatButton.setEnabled(false);
        sendSmsButton.setEnabled(true);
        phoneInput.setEnabled(true);
        intervalInput.setEnabled(true);

        String stopMsg = getString(R.string.repeat_stopped) + "\n" + 
                        String.format(getString(R.string.repeat_count), smsCount);
        statusText.setText(stopMsg);
        statusText.setTextColor(ContextCompat.getColor(this, R.color.red));

        Toast.makeText(this, "Envío repetido detenido. Total enviados: " + smsCount, 
                      Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatMode();
    }
}
