import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { startReader, stopReader, setScanSize } from '@nokecy/react-native-pda-scan';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>("abc");

  React.useEffect(() => {

    setScanSize(2,12)

    stopReader();

    startReader((data) => {
      console.log("收到数据", data)
      setResult(data);
    });

    return () => {
      stopReader();
    }
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
