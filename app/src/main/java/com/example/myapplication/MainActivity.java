package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_BT = 1001;
    private static final long SCAN_PERIOD = 8000;

    // ✅ UUID confirmés pour Makeblock BLE v1.0
    private static final UUID UART_SERVICE =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID UART_CHAR_WRITE =
            UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
    private static final UUID UART_CHAR_NOTIFY =
            UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ArrayAdapter<String> listAdapter;
    private Map<String, BluetoothDevice> devicesMap = new HashMap<>();
    private BluetoothGatt currentGatt;

    private Button btnScan, btnSendOne, btnSendZero;
    private ListView lvDevices;
    private TextView tvStatus;

    private BluetoothGattCharacteristic writeCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btn_scan);
        btnSendOne = findViewById(R.id.btn_send_one);
        btnSendZero = findViewById(R.id.btn_send_zero);
        lvDevices = findViewById(R.id.lv_devices);
        tvStatus = findViewById(R.id.tv_id);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvDevices.setAdapter(listAdapter);

        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bm != null) bluetoothAdapter = bm.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non supporté ❌", Toast.LENGTH_LONG).show();
            btnScan.setEnabled(false);
            return;
        }

        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        btnScan.setOnClickListener(v -> ensureBluetoothPermissions());

        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String entry = listAdapter.getItem(position);
            if (entry == null) return;

            String mac = entry.substring(entry.lastIndexOf("(") + 1, entry.lastIndexOf(")"));
            BluetoothDevice device = devicesMap.get(mac);
            if (device != null) {
                stopBleScan();
                tvStatus.setText("Connexion à " + entry + " ...");
                connectToDevice(device);
            }
        });

        btnSendOne.setOnClickListener(v -> sendData("1"));
        btnSendZero.setOnClickListener(v -> sendData("0"));

        ensureBluetoothPermissions();
    }

    /** Permissions runtime */
    private void ensureBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        REQ_CODE_BT);
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQ_CODE_BT);
                return;
            }
        }
        startBleScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_BT) {
            boolean granted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) { granted = false; break; }
            }
            if (granted) startBleScan();
            else Toast.makeText(this, "Permissions refusées ❌", Toast.LENGTH_SHORT).show();
        }
    }

    /** Scan BLE */
    private void startBleScan() {
        if (bleScanner == null) bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bleScanner == null) {
            Toast.makeText(this, "Scanner BLE non dispo ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        listAdapter.clear();
        devicesMap.clear();
        btnSendOne.setVisibility(Button.GONE);
        btnSendZero.setVisibility(Button.GONE);
        writeCharacteristic = null;

        btnScan.setEnabled(false);
        Toast.makeText(this, "Scan démarré...", Toast.LENGTH_SHORT).show();

        handler.postDelayed(this::stopBleScan, SCAN_PERIOD);
        bleScanner.startScan(scanCallback);
    }

    private void stopBleScan() {
        if (bleScanner != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bleScanner.stopScan(scanCallback);
            }
        }
        btnScan.setEnabled(true);
        Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice d = result.getDevice();
            String name = d.getName();

            if (name == null || !name.toLowerCase().startsWith("makeblock")) return;

            String key = d.getAddress();
            runOnUiThread(() -> {
                if (!devicesMap.containsKey(key)) {
                    devicesMap.put(key, d);
                    listAdapter.add(name + " (" + key + ")");
                    tvStatus.append("\nTrouvé: " + name + " [" + key + "]");
                }
            });
        }
    };

    /** Connexion GATT */
    private void connectToDevice(BluetoothDevice device) {
        if (currentGatt != null) {
            currentGatt.disconnect();
            currentGatt.close();
            currentGatt = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentGatt = device.connectGatt(getApplicationContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            currentGatt = device.connectGatt(getApplicationContext(), false, gattCallback);
        }
    }

    /** Callback GATT */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread(() -> tvStatus.setText("Connexion échouée ❌ status=" + status));
                gatt.close();
                return;
            }

            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> tvStatus.setText("Connecté ✅, découverte des services..."));
                gatt.discoverServices();
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> {
                    tvStatus.setText("Déconnecté ❌");
                    btnSendOne.setVisibility(Button.GONE);
                    btnSendZero.setVisibility(Button.GONE);
                });
                writeCharacteristic = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) return;

            BluetoothGattService uart = gatt.getService(UART_SERVICE);
            if (uart != null) {
                // FFE3 (WRITE)
                writeCharacteristic = uart.getCharacteristic(UART_CHAR_WRITE);

                // 🔹 Activer notifications sur FFE2
                BluetoothGattCharacteristic notifyChar = uart.getCharacteristic(UART_CHAR_NOTIFY);
                if (notifyChar != null) {
                    gatt.setCharacteristicNotification(notifyChar, true);
                    BluetoothGattDescriptor desc = notifyChar.getDescriptor(CCCD);
                    if (desc != null) {
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(desc);
                    }
                    runOnUiThread(() -> tvStatus.append("\nNotif activée sur FFE2 ✅"));
                }

                runOnUiThread(() -> {
                    tvStatus.append("\nUART prêt ✅ (FFE3=écriture, FFE2=notif)");
                    btnSendOne.setVisibility(Button.VISIBLE);
                    btnSendZero.setVisibility(Button.VISIBLE);
                });
            } else {
                runOnUiThread(() -> tvStatus.append("\n⚠️ Service UART introuvable"));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String msg = new String(characteristic.getValue());
            runOnUiThread(() -> tvStatus.append("\n📩 Reçu de " + characteristic.getUuid() + " : " + msg));
        }
    };

    /** Envoi */
    private void sendData(String data) {
        if (currentGatt == null || writeCharacteristic == null) {
            Toast.makeText(this, "UART non prêt ❌", Toast.LENGTH_SHORT).show();
            return;
        }
        writeCharacteristic.setValue(data.getBytes());
        boolean ok = currentGatt.writeCharacteristic(writeCharacteristic);
        if (ok) runOnUiThread(() -> tvStatus.append("\nEnvoyé: " + data));
        else runOnUiThread(() -> tvStatus.append("\nÉchec envoi ❌"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentGatt != null) {
            currentGatt.disconnect();
            currentGatt.close();
            currentGatt = null;
        }
        stopBleScan();
    }
}
