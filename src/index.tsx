import { NativeModules } from 'react-native';

type PdaScanType = {
  multiply(a: number, b: number): Promise<number>;
};

const { PdaScan } = NativeModules;

export default PdaScan as PdaScanType;
