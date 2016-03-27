package de.interoberlin.poisondartfrog.model.service;

import com.google.gson.Gson;

import de.interoberlin.poisondartfrog.model.EBluetoothDeviceType;
import de.interoberlin.poisondartfrog.model.parser.AccelGyroscope;
import de.interoberlin.poisondartfrog.model.parser.DataPackage;
import de.interoberlin.poisondartfrog.model.parser.LightColorProx;

public abstract class BleDataParser {

    public static String getFormattedValue(EBluetoothDeviceType type, byte[] value) {
        if (value == null) return "";
        switch (type) {
            case WUNDERBAR_LIGHT: {
                return BleDataParser.getLIGHTSensorData(value);
            }
            case WUNDERBAR_GYRO: {
                return BleDataParser.getGYROSensorData(value);
            }
            case WUNDERBAR_HTU: {
                return BleDataParser.getHTUSensorData(value);
            }
            case WUNDERBAR_MIC: {
                return BleDataParser.getMICSensorData(value);
            }
            case WUNDERBAR_BRIDG: {
                return getBridgeSensorData(value);
            }
            default:
                return "";
        }
    }

    private static String getLIGHTSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EBluetoothDeviceType.WUNDERBAR_LIGHT.getId();
        dataPackage.received = System.currentTimeMillis();

        int red = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);
        int green = (byteToUnsignedInt(value[3]) << 8) | byteToUnsignedInt(value[2]);
        int blue = (byteToUnsignedInt(value[5]) << 8) | byteToUnsignedInt(value[4]);
        LightColorProx.Color color = new LightColorProx.Color(red, green, blue);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "color", "", color));

        int proximity = (byteToUnsignedInt(value[9]) << 8) | byteToUnsignedInt(value[8]);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "proximity", "", proximity));

        int luminosity = (byteToUnsignedInt(value[7]) << 8) | byteToUnsignedInt(value[6]);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "luminosity", "", luminosity));

        return new Gson().toJson(dataPackage);
    }

    private static String getGYROSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EBluetoothDeviceType.WUNDERBAR_GYRO.getId();
        dataPackage.received = System.currentTimeMillis();

        int gyroscopeX = byteToUnsignedInt(value[0]) |
                (byteToUnsignedInt(value[1]) << 8) |
                (byteToUnsignedInt(value[2]) << 16) |
                (byteToUnsignedInt(value[3]) << 24);
        int gyroscopeY = byteToUnsignedInt(value[4]) |
                (byteToUnsignedInt(value[5]) << 8) |
                (byteToUnsignedInt(value[6]) << 16) |
                (byteToUnsignedInt(value[7]) << 24);
        int gyroscopeZ = byteToUnsignedInt(value[8]) |
                (byteToUnsignedInt(value[9]) << 8) |
                (byteToUnsignedInt(value[10]) << 16) |
                (byteToUnsignedInt(value[11]) << 24);

        int accelerationX = (byteToUnsignedInt(value[13]) << 8) | byteToUnsignedInt(value[12]);
        int accelerationY = (byteToUnsignedInt(value[15]) << 8) | byteToUnsignedInt(value[14]);
        int accelerationZ = (byteToUnsignedInt(value[17]) << 8) | byteToUnsignedInt(value[16]);

        AccelGyroscope.Acceleration acceleration = new AccelGyroscope.Acceleration();
        acceleration.x = (float) accelerationX / 100.0f;
        acceleration.y = (float) accelerationY / 100.0f;
        acceleration.z = (float) accelerationZ / 100.0f;
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "acceleration", "", acceleration));

        AccelGyroscope.AngularSpeed angularSpeed = new AccelGyroscope.AngularSpeed();
        angularSpeed.x = (float) gyroscopeX / 100.0f;
        angularSpeed.y = (float) gyroscopeY / 100.0f;
        angularSpeed.z = (float) gyroscopeZ / 100.0f;
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "angularSpeed", "", angularSpeed));

        return new Gson().toJson(dataPackage);
    }

    private static String getHTUSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EBluetoothDeviceType.WUNDERBAR_HTU.getId();
        dataPackage.received = System.currentTimeMillis();

        int temperature = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);
        int humidity = (byteToUnsignedInt(value[3]) << 8) | byteToUnsignedInt(value[2]);

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "humidity", "", (int) ((float) humidity / 100.0f)));
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "temperature", "", (float) temperature / 100.0f));
        return new Gson().toJson(dataPackage);
    }

    private static String getMICSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EBluetoothDeviceType.WUNDERBAR_MIC.getId();
        dataPackage.received = System.currentTimeMillis();

        int noiseLevel = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "noiseLevel", "", noiseLevel));
        return new Gson().toJson(dataPackage);
    }

    private static String getBridgeSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EBluetoothDeviceType.WUNDERBAR_BRIDG.getId();
        dataPackage.received = System.currentTimeMillis();

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "up_ch_payload", "", value));
        return new Gson().toJson(dataPackage);
    }

    private static int byteToUnsignedInt(byte b) {
        return (int) b & 0xff;
    }
}
