import { DeviceEventEmitter } from 'react-native';
import HoneywellScanner from 'react-native-honeywell-scanner-v2';


const startReader = () => {
    if (HoneywellScanner.isCompatible) {
        return HoneywellScanner.startReader().then((claimed: any) => {
            console.log(claimed ? 'Barcode reader is claimed' : 'Barcode reader is busy');
        });
    }
}

const stopReader = () => {
    if (HoneywellScanner.isCompatible) {
        return HoneywellScanner.stopReader();
    }
}

const addListener = (fn: (receivedData: any) => void) => {

    if (HoneywellScanner.isCompatible) {
        HoneywellScanner.onBarcodeReadSuccess((event: any) => {
            DeviceEventEmitter.emit('onScanReceive', { scanCode: event.data });
        });
    }

    DeviceEventEmitter.addListener('onScanReceive', fn);
}

const removeListener = () => {
    if (HoneywellScanner.isCompatible) {
        HoneywellScanner.offBarcodeReadSuccess();
    }
    return DeviceEventEmitter.removeAllListeners('onScanReceive');
}

export { startReader, stopReader, addListener, removeListener }
