import { DeviceEventEmitter } from 'react-native';
import HoneywellScanner from 'react-native-honeywell-scanner-v2';


const startReader = (fn: (receivedData: any) => void) => {
    if (HoneywellScanner.isCompatible) {
        HoneywellScanner.startReader().then((claimed: any) => {
            console.log(claimed ? 'Barcode reader is claimed' : 'Barcode reader is busy');

            HoneywellScanner.onBarcodeReadSuccess((event: any) => {
                console.log('Received data', event.data);
                DeviceEventEmitter.emit('onScanReceive', event.data);
            });
        });
    }

    return DeviceEventEmitter.addListener('onScanReceive', fn);
}

const stopReader = () => {
    if (HoneywellScanner.isCompatible) {
        HoneywellScanner.stopReader().then(() => {
            console.log("Freedom!!");
            HoneywellScanner.offBarcodeReadSuccess();
        });
    }

    return DeviceEventEmitter.removeAllListeners('onScanReceive');
}

export { startReader, stopReader }
