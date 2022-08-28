import { DeviceEventEmitter, NativeModules } from 'react-native';
import { Platform } from 'react-native';
const { PdaScan } = NativeModules;

const startReader = () => {
    if (Platform.OS === "android") {
        const HoneywellScanner = require("react-native-honeywell-scanner-v2");

        if (HoneywellScanner.isCompatible) {
            return HoneywellScanner.startReader().then((claimed: any) => {
                console.log(claimed ? 'Barcode reader is claimed' : 'Barcode reader is busy');
            });
        }
    }
}

const stopReader = () => {
    if (Platform.OS === "android") {
        const HoneywellScanner = require("react-native-honeywell-scanner-v2");
        if (HoneywellScanner.isCompatible) {
            return HoneywellScanner.stopReader();
        }
    }
}

const setScanSize = (scanSize: number, scanLen: number) => {
    PdaScan.setScanSize(scanSize, scanLen)
}

const addListener = (fn: (receivedData: any) => void) => {
    if (Platform.OS === "android") {
        const HoneywellScanner = require("react-native-honeywell-scanner-v2");

        if (HoneywellScanner.isCompatible) {
            HoneywellScanner.onBarcodeReadSuccess((event: any) => {
                DeviceEventEmitter.emit('onScanReceive', { scanCode: event.data });
            });
        }
    }
    DeviceEventEmitter.addListener('onScanReceive', fn);
}

const removeListener = () => {
    if (Platform.OS === "android") {
        const HoneywellScanner = require("react-native-honeywell-scanner-v2");
        if (HoneywellScanner.isCompatible) {
            HoneywellScanner.offBarcodeReadSuccess();
        }
    }
    return DeviceEventEmitter.removeAllListeners('onScanReceive');
}

export { startReader, stopReader, addListener, removeListener, setScanSize }
