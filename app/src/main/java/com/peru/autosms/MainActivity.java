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
    private MaterialButton sendSmsButton;
    private TextView statusText;
    private TextView messagesPreview;

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

        initViews();
        displayAvailableMessages();
        setupSendButton();
    }

    private void initViews() {
        phoneInput = findViewById(R.id.phoneInput);
        sendSmsButton = findViewById(R.id.sendSmsButton);
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(new BroadcastReceiver() {
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
                }, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(new BroadcastReceiver() {
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
                }, new IntentFilter(SENT));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(new BroadcastReceiver() {
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
                }, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(new BroadcastReceiver() {
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
                }, new IntentFilter(DELIVERED));
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
}
