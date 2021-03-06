package com.nexenio.bleindoorpositioning.ble.beacon;

import com.nexenio.bleindoorpositioning.ble.advertising.EddystoneAdvertisingPacket;
import com.nexenio.bleindoorpositioning.location.provider.EddystoneLocationProvider;
import com.nexenio.bleindoorpositioning.location.provider.LocationProvider;

/**
 * Created by steppschuh on 15.11.17.
 */

public class Eddystone extends Beacon<EddystoneAdvertisingPacket> {

    public static final int CALIBRATION_DISTANCE_DEFAULT = 0;

    public Eddystone() {
        this.calibratedDistance = CALIBRATION_DISTANCE_DEFAULT;
    }

    @Override
    public LocationProvider createLocationProvider() {
        return new EddystoneLocationProvider(this);
    }

}
