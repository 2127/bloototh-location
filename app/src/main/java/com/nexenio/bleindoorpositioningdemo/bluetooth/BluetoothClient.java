package com.nexenio.bleindoorpositioningdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nexenio.bleindoorpositioning.ble.advertising.AdvertisingPacket;
import com.nexenio.bleindoorpositioning.ble.beacon.Beacon;
import com.nexenio.bleindoorpositioning.ble.beacon.BeaconManager;
import com.nexenio.bleindoorpositioning.ble.beacon.IBeacon;
import com.nexenio.bleindoorpositioning.location.Location;
import com.nexenio.bleindoorpositioning.location.provider.LocationProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import rx.Observer;
import rx.Subscription;

/**
 * Created by steppschuh on 24.11.17.
 */

public class BluetoothClient {

    private static final String TAG = BluetoothClient.class.getSimpleName();
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 10;

    private static BluetoothClient instance;

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BeaconManager beaconManager = BeaconManager.getInstance();

    private RxBleClient rxBleClient;
    private Subscription scanningSubscription;

    private BluetoothClient() {

    }

    public static BluetoothClient getInstance() {
        if (instance == null) {
            instance = new BluetoothClient();
        }
        return instance;
    }

    public static void initialize(@NonNull Context context) {
        Log.v(TAG, "Initializing with context: " + context);
        BluetoothClient instance = getInstance();
        instance.rxBleClient = RxBleClient.create(context);
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        instance.bluetoothAdapter = bluetoothManager.getAdapter();
        if (instance.bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter is not available");
        }
    }

    public static void startScanning() {
        if (isScanning()) {
            return;
        }

        final BluetoothClient instance = getInstance();
        Log.d(TAG, "Starting to scan for beacons");

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        instance.scanningSubscription = instance.rxBleClient.scanBleDevices(scanSettings)
                .subscribe(new Observer<ScanResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Bluetooth scanning error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ScanResult scanResult) {
                        instance.processScanResult(scanResult);
                    }
                });
    }

    public static void stopScanning() {
        if (!isScanning()) {
            return;
        }

        BluetoothClient instance = getInstance();
        Log.d(TAG, "Stopping to scan for beacons");
        instance.scanningSubscription.unsubscribe();
    }

    public static boolean isScanning() {
        Subscription subscription = getInstance().scanningSubscription;
        return subscription != null && !subscription.isUnsubscribed();
    }

    public static boolean isBluetoothEnabled() {
        BluetoothClient instance = getInstance();
        return instance.bluetoothAdapter != null && instance.bluetoothAdapter.isEnabled();
    }

    public static void requestBluetoothEnabling(@NonNull Activity activity) {
        Log.d(TAG, "Requesting bluetooth enabling");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    private void processScanResult(@NonNull ScanResult scanResult) {
        String macAddress = scanResult.getBleDevice().getMacAddress();

        byte[] data = scanResult.getScanRecord().getBytes();
        AdvertisingPacket advertisingPacket = AdvertisingPacket.from(data);

        if (advertisingPacket != null) {
            advertisingPacket.setRssi(scanResult.getRssi());

            Beacon beacon = BeaconManager.getBeacon(macAddress, advertisingPacket.getBeaconClass());
            AdvertisingPacket lastAdvertisingPacket = beacon == null ? null : beacon.getLatestAdvertisingPacket();

            boolean isNewBeacon = beacon == null;
            boolean isNewAdvertisingData = lastAdvertisingPacket == null || !advertisingPacket.dataEquals(lastAdvertisingPacket);

            BeaconManager.processAdvertisingPacket(macAddress, advertisingPacket);

            if (isNewBeacon) {
                beacon = BeaconManager.getBeacon(macAddress, advertisingPacket.getBeaconClass());
                if (beacon instanceof IBeacon) {
                    beacon.setLocationProvider(createDebuggingLocationProvider((IBeacon) beacon));
                }
                Log.d(TAG, macAddress + " data received for the first time: " + advertisingPacket);
            } else if (isNewAdvertisingData) {
                //Log.v(TAG, macAddress + " data changed to: " + advertisingPacket);
            } else {
                //Log.v(TAG, macAddress + " data unchanged: " + advertisingPacket);
            }

        }

    }

    private static LocationProvider createDebuggingLocationProvider(IBeacon iBeacon) {
        final Location location = new Location();
        switch (iBeacon.getMinor()) {
            case 1: {
                location.setLatitude(52.512437);
                location.setLongitude(13.391124);
                location.setAltitude(36);
                break;
            }
            case 2: {
                location.setLatitude(52.512411788476356);
                location.setLongitude(13.390875654442985);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 3: {
                location.setLatitude(52.51240486636751);
                location.setLongitude(13.390770270005437);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 4: {
                location.setLatitude(52.512426);
                location.setLongitude(13.390887);
                location.setElevation(2);
                location.setAltitude(36);
                break;
            }
            case 5: {
                location.setLatitude(52.512347534813834);
                location.setLongitude(13.390780437281524);
                location.setElevation(2.9);
                location.setAltitude(36);
                break;
            }
            case 12: {
                location.setLatitude(52.51239708899507);
                location.setLongitude(13.390878261276518);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 13: {
                location.setLatitude(52.51242692608082);
                location.setLongitude(13.390872969910035);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 14: {
                location.setLatitude(52.51240825552749);
                location.setLongitude(13.390821867681456);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 15: {
                location.setLatitude(52.51240194910502);
                location.setLongitude(13.390725856632926);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 16: {
                location.setLatitude(52.512390301005595);
                location.setLongitude(13.39077285305359);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 17: {
                location.setLatitude(52.51241817994876);
                location.setLongitude(13.390767908948872);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
            case 18: {
                location.setLatitude(52.51241494408066);
                location.setLongitude(13.390923696709294);
                location.setElevation(2.65);
                location.setAltitude(36);
                break;
            }
        }
        return new LocationProvider() {
            @Override
            public Location getLocation() {
                return location;
            }
        };
    }

}
