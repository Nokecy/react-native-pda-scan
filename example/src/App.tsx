import * as React from 'react';

import { StyleSheet, View, Text, NativeModules, NativeEventEmitter } from 'react-native';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>("abc");

  React.useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.PdaScan);
    const listener = eventEmitter.addListener('onScanReceive', (data) => {
      setResult(data.scanCode);
    })

    return listener.remove();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
