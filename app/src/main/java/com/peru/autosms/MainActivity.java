package com.peru.autosms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
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
    private static final long SMS_INTERVAL = 60000; // 60 segundos en milisegundos

    private TextInputEditText phoneInput;
    private MaterialButton startAutoSmsButton;
    private MaterialButton stopAutoSmsButton;
    private TextView statusText;
    private TextView messagesPreview;
    private TextView timerText;

    // 5 frases personalizadas para Perú
    private final String[] messages = {
            "¡Hola! Espero que estés teniendo un día increíble. ¡Saludos desde Perú! 🇵🇪",
            "¡Qué tal! Solo quería enviarte un saludo y desearte mucha suerte en todo. ¡Arriba Perú!",
            "¡Hola amigo/a! Que tengas un excelente día lleno de bendiciones. ¡Un abrazo!",
            "¡Saludos cordiales! Espero que todo te esté yendo muy bien. ¡Éxitos siempre!",
            "¡Hola! Solo un mensaje para recordarte que eres increíble. ¡Que tengas un gran día!"
    };

    private Handler handler;
    private Runnable smsRunnable;
    private Runnable timerRunnable;
    private boolean isAutoSmsRunning = false;
    private int messagesSent = 0;
    private int secondsRemaining = 60;
    private String currentPhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());

        initViews();
        displayAvailableMessages();
        setupButtons();
    }

    private void initViews() {
        phoneInput = findViewById(R.id.phoneInput);
        startAutoSmsButton = findViewById(R.id.startAutoSmsButton);
        stopAutoSmsButton = findViewById(R.id.stopAutoSmsButton);
        statusText = findViewById(R.id.statusText);
        messagesPreview = findViewById(R.id.messagesPreview);
        timerText = findViewById(R.id.timerText);
    }

    private void displayAvailableMessages() {
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            preview.append((i + 1)).append(". ").append(messages[i]).append("\n\n");
        }
        messagesPreview.setText(preview.toString());
    }

    private void setupButtons() {
        startAutoSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString().trim();

                if (validatePhoneNumber(phoneNumber)) {
                    if (checkSmsPermission()) {
                        startAutoSms(phoneNumber);
                    } else {
                        requestSmsPermission();
                    }
                } else {
                    statusText.setText(R.string.invalid_phone);
                    statusText.setTextColor(getResources().getColor(R.color.red));
                    Toast.makeText(MainActivity.this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopAutoSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoSms();
            }
        });
    }

    private void startAutoSms(String phoneNumber) {
        if (isAutoSmsRunning) {
            return;
        }

        currentPhoneNumber = phoneNumber;
        isAutoSmsRunning = true;
        messagesSent = 0;
        secondsRemaining = 60;

        // Actualizar UI
        startAutoSmsButton.setEnabled(false);
        stopAutoSmsButton.setEnabled(true);
        phoneInput.setEnabled(false);

        // Enviar el primer SMS inmediatamente
        sendRandomSms(currentPhoneNumber);
        messagesSent++;

        // Configurar envío automático cada minuto
        smsRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoSmsRunning) {
                    sendRandomSms(currentPhoneNumber);
                    messagesSent++;
                    secondsRemaining = 60;
                    handler.postDelayed(this, SMS_INTERVAL);
                }
            }
        };
        handler.postDelayed(smsRunnable, SMS_INTERVAL);

        // Configurar contador de tiempo
        secondsRemaining = 60;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoSmsRunning && secondsRemaining > 0) {
                    secondsRemaining--;
                    updateTimerDisplay();
                    handler.postDelayed(this, 1000);
                } else if (isAutoSmsRunning) {
                    secondsRemaining = 60;
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timerRunnable);

        Toast.makeText(this, R.string.auto_sms_running, Toast.LENGTH_SHORT).show();
    }

    private void stopAutoSms() {
        if (!isAutoSmsRunning) {
            return;
        }

        isAutoSmsRunning = false;

        // Cancelar todos los handlers
        if (handler != null) {
            if (smsRunnable != null) {
                handler.removeCallbacks(smsRunnable);
            }
            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
        }

        // Actualizar UI
        startAutoSmsButton.setEnabled(true);
        stopAutoSmsButton.setEnabled(false);
        phoneInput.setEnabled(true);

        timerText.setText(R.string.auto_sms_stopped);
        timerText.setTextColor(getResources().getColor(R.color.red));

        String stoppedMsg = "⏹️ Envío automático detenido\n" +
                "Total de mensajes enviados: " + messagesSent + "\n" +
                "Número: " + currentPhoneNumber;
        statusText.setText(stoppedMsg);
        statusText.setTextColor(getResources().getColor(R.color.purple_700));

        Toast.makeText(this, R.string.auto_sms_stopped, Toast.LENGTH_SHORT).show();
    }

    private void updateTimerDisplay() {
        String timerMsg = "⏱️ Próximo SMS en: " + secondsRemaining + " segundos";
        timerText.setText(timerMsg);
        timerText.setTextColor(getResources().getColor(R.color.green));

        String statusMsg = "🔄 Envío automático activo\n" +
                "Mensajes enviados: " + messagesSent + "\n" +
                "Destinatario: " + currentPhoneNumber;
        statusText.setText(statusMsg);
        statusText.setTextColor(getResources().getColor(R.color.purple_700));
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
                Toast.makeText(this, "Permiso concedido. Por favor, intente iniciar de nuevo.",
                        Toast.LENGTH_SHORT).show();
            } else {
                statusText.setText(R.string.permission_denied);
                statusText.setTextColor(getResources().getColor(R.color.red));
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendRandomSms(String phoneNumber) {
        // Seleccionar una frase aleatoria entre las 5 disponibles
        Random random = new Random();
        int randomIndex = random.nextInt(messages.length);
        String selectedMessage = messages[randomIndex];

        try {
            // Registrar receptores para saber si el SMS fue enviado y entregado
            String SENT = "SMS_SENT_" + System.currentTimeMillis();
            String DELIVERED = "SMS_DELIVERED_" + System.currentTimeMillis();

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);

            final String finalMessage = selectedMessage;
            final int finalIndex = randomIndex;

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "";
                    switch (getResultCode()) {
                        case RESULT_OK:
                            result = "✓ SMS #" + messagesSent + " enviado correctamente";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "✗ Error genérico al enviar SMS #" + messagesSent;
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "✗ Sin servicio - SMS #" + messagesSent;
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "✗ Error PDU nulo - SMS #" + messagesSent;
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "✗ Radio apagada - SMS #" + messagesSent;
                            break;
                    }

                    if (isAutoSmsRunning) {
                        String statusMsg = "🔄 Envío automático activo\n" +
                                result + "\n" +
                                "Mensaje (#" + (finalIndex + 1) + "): \"" + finalMessage + "\"";
                        statusText.setText(statusMsg);
                    }

                    // Desregistrar el receptor
                    try {
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        // Ignorar si ya fue desregistrado
                    }
                }
            }, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Desregistrar el receptor
                    try {
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        // Ignorar si ya fue desregistrado
                    }
                }
            }, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);

            // Enviar el SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, selectedMessage, sentPI, deliveredPI);

        } catch (Exception e) {
            Toast.makeText(this, "Error al enviar SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener envío automático al destruir la actividad
        stopAutoSms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Nota: El envío automático continuará en background
        // Si quieres detenerlo cuando la app va a background, descomenta la siguiente línea:
        // stopAutoSms();
    }
}
