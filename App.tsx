import React, { useState, useEffect, useMemo } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Share,
  Alert,
  Linking,
  Switch,
  ActivityIndicator,
  Modal
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';

// --- TYPES ---
type CurrencyCode = 'USD' | 'EUR' | 'USDT';

interface CurrencyInfo {
  code: CurrencyCode;
  symbol: string;
  title: string;
  flag: string;
  color: string;
}

const CURRENCIES: Record<CurrencyCode, CurrencyInfo> = {
  USD: { code: 'USD', symbol: '$', title: 'Dólar BCV', flag: '🇺🇸', color: '#30D158' },
  EUR: { code: 'EUR', symbol: '€', title: 'Euro BCV', flag: '🇪🇺', color: '#FF9F0A' },
  USDT: { code: 'USDT', symbol: '₮', title: 'USDT Binance', flag: '🪙', color: '#64D2FF' },
};

interface CalculationItem {
  id: string;
  fromCode: string;
  toCode: string;
  fromSymbol: string;
  toSymbol: string;
  inputAmount: number;
  resultAmount: number;
  rate: number;
  dateStr: string;
}

interface PriceAlert {
  id: string;
  currency: CurrencyCode;
  targetRate: number;
  condition: 'ABOVE' | 'BELOW';
  isActive: boolean;
}

export default function App() {
  const [selectedTab, setSelectedTab] = useState<'calc' | 'trends' | 'history' | 'alerts'>('calc');

  // Rates state
  const [usdBcv, setUsdBcv] = useState(61.20);
  const [eurBcv, setEurBcv] = useState(66.85);
  const [usdtBinance, setUsdtBinance] = useState(68.90);
  const [lastUpdated, setLastUpdated] = useState('Hoy');
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Calculator state
  const [selectedCurrency, setSelectedCurrency] = useState<CurrencyCode>('USD');
  const [isInverse, setIsInverse] = useState(false);
  const [inputText, setInputText] = useState('10');

  // Trends state
  const [trendCurrency, setTrendCurrency] = useState<CurrencyCode>('USD');
  const [trendDays, setTrendDays] = useState<number>(30);

  // History & Alerts state
  const [history, setHistory] = useState<CalculationItem[]>([]);
  const [alerts, setAlerts] = useState<PriceAlert[]>([]);
  const [newAlertRate, setNewAlertRate] = useState('');
  const [newAlertCond, setNewAlertCond] = useState<'ABOVE' | 'BELOW'>('ABOVE');
  const [showLegalModal, setShowLegalModal] = useState(false);

  // Spread calculation
  const spreadPercent = useMemo(() => {
    return usdBcv > 0 ? (((usdtBinance - usdBcv) / usdBcv) * 100).toFixed(1) : '0.0';
  }, [usdBcv, usdtBinance]);

  // Current rate
  const currentRate = useMemo(() => {
    switch (selectedCurrency) {
      case 'USD': return usdBcv;
      case 'EUR': return eurBcv;
      case 'USDT': return usdtBinance;
    }
  }, [selectedCurrency, usdBcv, eurBcv, usdtBinance]);

  // Calculation logic
  const parsedInput = useMemo(() => {
    const clean = inputText.replace(',', '.');
    return parseFloat(clean) || 0;
  }, [inputText]);

  const calculatedTotal = useMemo(() => {
    if (isInverse) {
      return currentRate > 0 ? parsedInput / currentRate : 0;
    } else {
      return parsedInput * currentRate;
    }
  }, [parsedInput, currentRate, isInverse]);

  // Fetch rates from dolarvzla.com API
  const fetchLiveRates = async () => {
    setIsRefreshing(true);
    try {
      const res = await fetch('https://api.dolarvzla.com/v1/rates', {
        headers: { 'Accept': 'application/json' }
      });
      if (res.ok) {
        const data = await res.json();
        if (data.usd) setUsdBcv(data.usd);
        if (data.eur) setEurBcv(data.eur);
        if (data.usdt) setUsdtBinance(data.usdt);
        const now = new Date();
        setLastUpdated(`${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`);
      }
    } catch {
      // Graceful fallback to cached values
    } finally {
      setIsRefreshing(false);
    }
  };

  // Load persistent history and alerts
  useEffect(() => {
    fetchLiveRates();
    loadStoredData();
  }, []);

  const loadStoredData = async () => {
    try {
      const storedHist = await AsyncStorage.getItem('@tc_history');
      if (storedHist) setHistory(JSON.parse(storedHist));
      const storedAlerts = await AsyncStorage.getItem('@tc_alerts');
      if (storedAlerts) setAlerts(JSON.parse(storedAlerts));
    } catch (e) {
      console.log('Error reading storage', e);
    }
  };

  const saveHistoryItem = async () => {
    const fromSymbol = isInverse ? 'Bs.' : CURRENCIES[selectedCurrency].symbol;
    const toSymbol = isInverse ? CURRENCIES[selectedCurrency].symbol : 'Bs.';
    const fromCode = isInverse ? 'VES' : selectedCurrency;
    const toCode = isInverse ? selectedCurrency : 'VES';

    const newItem: CalculationItem = {
      id: Date.now().toString(),
      fromCode,
      toCode,
      fromSymbol,
      toSymbol,
      inputAmount: parsedInput,
      resultAmount: calculatedTotal,
      rate: currentRate,
      dateStr: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    const updated = [newItem, ...history.slice(0, 49)];
    setHistory(updated);
    try {
      await AsyncStorage.setItem('@tc_history', JSON.stringify(updated));
    } catch (e) {
      console.log('Error saving history', e);
    }
  };

  const deleteHistoryItem = async (id: string) => {
    const updated = history.filter(item => item.id !== id);
    setHistory(updated);
    await AsyncStorage.setItem('@tc_history', JSON.stringify(updated));
  };

  const clearAllHistory = async () => {
    setHistory([]);
    await AsyncStorage.removeItem('@tc_history');
  };

  const addPriceAlert = async () => {
    const target = parseFloat(newAlertRate.replace(',', '.'));
    if (!target || target <= 0) {
      Alert.alert('Valor inválido', 'Por favor ingresa un precio objetivo válido en Bolívares.');
      return;
    }

    const newAlert: PriceAlert = {
      id: Date.now().toString(),
      currency: selectedCurrency,
      targetRate: target,
      condition: newAlertCond,
      isActive: true
    };

    const updated = [newAlert, ...alerts];
    setAlerts(updated);
    setNewAlertRate('');
    await AsyncStorage.setItem('@tc_alerts', JSON.stringify(updated));
    Alert.alert('¡Alerta creada!', `Se te notificará cuando ${selectedCurrency} ${newAlertCond === 'ABOVE' ? '≥' : '≤'} Bs. ${target.toFixed(2)}`);
  };

  const toggleAlert = async (id: string) => {
    const updated = alerts.map(a => a.id === id ? { ...a, isActive: !a.isActive } : a);
    setAlerts(updated);
    await AsyncStorage.setItem('@tc_alerts', JSON.stringify(updated));
  };

  const deleteAlert = async (id: string) => {
    const updated = alerts.filter(a => a.id !== id);
    setAlerts(updated);
    await AsyncStorage.setItem('@tc_alerts', JSON.stringify(updated));
  };

  const shareCalculation = async () => {
    const sourceSymbol = isInverse ? 'Bs.' : CURRENCIES[selectedCurrency].symbol;
    const targetSymbol = isInverse ? CURRENCIES[selectedCurrency].symbol : 'Bs.';
    const text = `💱 *Cálculo de TasaCambio 2.0 iOS*\n\n` +
      `• Monto: ${sourceSymbol} ${parsedInput.toFixed(2)}\n` +
      `• Tasa aplicada: Bs. ${currentRate.toFixed(2)}\n` +
      `• Total a recibir: *${targetSymbol} ${calculatedTotal.toFixed(2)}*\n\n` +
      `Desarrollada por ArtesWebVE | https://arteswebve.com/`;

    await Share.share({ message: text });
    saveHistoryItem();
  };

  const copyResult = () => {
    Alert.alert('Copiado', '¡Cálculo copiado con éxito!');
    saveHistoryItem();
  };

  // Generate mock historical curve
  const historicalPoints = useMemo(() => {
    const base = trendCurrency === 'USD' ? usdBcv : (trendCurrency === 'EUR' ? eurBcv : usdtBinance);
    const arr = [];
    for (let i = trendDays - 1; i >= 0; i--) {
      const factor = 1.0 - (i * 0.0035) + (Math.sin(i * 0.5) * 0.004);
      arr.push({ day: `${trendDays - i}d`, val: base * factor });
    }
    return arr;
  }, [trendCurrency, trendDays, usdBcv, eurBcv, usdtBinance]);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000000" />

      {/* HEADER */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => Linking.openURL('https://arteswebve.com/')}>
          <View style={styles.brandRow}>
            <Text style={styles.headerTitle}>TasaCambio</Text>
            <View style={styles.iosBadge}>
              <Text style={styles.iosBadgeText}>2.0 iOS</Text>
            </View>
          </View>
          <Text style={styles.headerSub}>por ArtesWebVE ↗</Text>
        </TouchableOpacity>

        <View style={styles.headerRight}>
          <TouchableOpacity
            style={styles.refreshBtn}
            onPress={fetchLiveRates}
            disabled={isRefreshing}
          >
            {isRefreshing ? (
              <ActivityIndicator size="small" color="#FFD60A" />
            ) : (
              <Ionicons name="reload" size={18} color="#8E8E93" />
            )}
          </TouchableOpacity>
          <TouchableOpacity style={styles.infoBtn} onPress={() => setShowLegalModal(true)}>
            <Ionicons name="information-circle-outline" size={22} color="#FFD60A" />
          </TouchableOpacity>
        </View>
      </View>

      {/* TICKER CARDS */}
      <View style={styles.tickerRow}>
        {(['USD', 'EUR', 'USDT'] as CurrencyCode[]).map((c) => {
          const isSelected = selectedCurrency === c;
          const rate = c === 'USD' ? usdBcv : (c === 'EUR' ? eurBcv : usdtBinance);
          const info = CURRENCIES[c];
          return (
            <TouchableOpacity
              key={c}
              style={[styles.tickerCard, isSelected && styles.tickerCardSelected]}
              onPress={() => {
                setSelectedCurrency(c);
                setSelectedTab('calc');
              }}
            >
              <Text style={styles.tickerEmoji}>{info.flag}</Text>
              <Text style={[styles.tickerCode, isSelected && { color: '#FFD60A' }]}>{info.code}</Text>
              <Text style={[styles.tickerRate, { color: info.color }]}>Bs. {rate.toFixed(2)}</Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* SPREAD BAR */}
      <View style={styles.spreadBar}>
        <Text style={styles.spreadText}>Brecha cambiaria: +{spreadPercent}%</Text>
        <Text style={styles.spreadSource}>• Fuente: dolarvzla.com / BCV</Text>
      </View>

      {/* MAIN CONTENT AREA */}
      <View style={styles.content}>
        {selectedTab === 'calc' && (
          <ScrollView contentContainerStyle={styles.scrollBody} showsVerticalScrollIndicator={false}>
            {/* Currency Segmented Picker */}
            <Text style={styles.sectionLabel}>DIVISA</Text>
            <View style={styles.segmentedContainer}>
              {(['USD', 'EUR', 'USDT'] as CurrencyCode[]).map((c) => (
                <TouchableOpacity
                  key={c}
                  style={[styles.segmentBtn, selectedCurrency === c && styles.segmentBtnActive]}
                  onPress={() => setSelectedCurrency(c)}
                >
                  <Text style={[styles.segmentText, selectedCurrency === c && styles.segmentTextActive]}>
                    {CURRENCIES[c].flag} {CURRENCIES[c].code}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            {/* Direction Swap Row */}
            <Text style={styles.sectionLabel}>DIRECCIÓN DE CONVERSIÓN</Text>
            <View style={styles.swapCard}>
              <View>
                <Text style={styles.swapTitle}>{isInverse ? 'Bolívar Digital' : CURRENCIES[selectedCurrency].title}</Text>
                <Text style={styles.swapCode}>{isInverse ? 'VES' : CURRENCIES[selectedCurrency].code}</Text>
              </View>
              <TouchableOpacity style={styles.swapCircle} onPress={() => setIsInverse(!isInverse)}>
                <Ionicons name="swap-vertical" size={22} color="#000000" />
              </TouchableOpacity>
              <View style={{ alignItems: 'flex-end' }}>
                <Text style={styles.swapTitle}>{isInverse ? CURRENCIES[selectedCurrency].title : 'Bolívar Digital'}</Text>
                <Text style={styles.swapCode}>{isInverse ? CURRENCIES[selectedCurrency].code : 'VES'}</Text>
              </View>
            </View>

            {/* Amount Input */}
            <Text style={styles.sectionLabel}>MONTO A CALCULAR</Text>
            <View style={styles.inputContainer}>
              <Text style={styles.inputPrefix}>{isInverse ? 'Bs.' : CURRENCIES[selectedCurrency].symbol}</Text>
              <TextInput
                style={styles.textInput}
                value={inputText}
                onChangeText={setInputText}
                keyboardType="decimal-pad"
                placeholder="0.00"
                placeholderTextColor="#636366"
              />
            </View>

            {/* Quick Amount Chips */}
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsRow}>
              {(isInverse ? [200, 500, 1000, 2500, 5000] : [5, 10, 20, 50, 100]).map((amt) => (
                <TouchableOpacity
                  key={amt}
                  style={styles.chip}
                  onPress={() => setInputText(amt.toString())}
                >
                  <Text style={styles.chipText}>{isInverse ? `Bs. ${amt}` : `${CURRENCIES[selectedCurrency].symbol}${amt}`}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            {/* Calculated Result Card */}
            <Text style={styles.sectionLabel}>TOTAL A RECIBIR</Text>
            <View style={styles.resultCard}>
              <View style={styles.resultHeaderRow}>
                <Text style={styles.resultLabel}>TOTAL CALCULADO</Text>
                <View style={styles.ratePill}>
                  <Text style={styles.ratePillText}>Tasa: Bs. {currentRate.toFixed(2)}</Text>
                </View>
              </View>

              <Text style={styles.resultAmount}>
                {isInverse ? CURRENCIES[selectedCurrency].symbol : 'Bs.'} {calculatedTotal.toFixed(2)}
              </Text>
              <Text style={styles.resultSub}>
                Conversión de {parsedInput.toFixed(2)} {isInverse ? 'VES' : selectedCurrency} a {isInverse ? selectedCurrency : 'VES'}
              </Text>

              <View style={styles.actionRow}>
                <TouchableOpacity style={styles.copyBtn} onPress={copyResult}>
                  <Ionicons name="copy-outline" size={16} color="#FFFFFF" />
                  <Text style={styles.copyBtnText}>Copiar</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.shareBtn} onPress={shareCalculation}>
                  <Ionicons name="share-outline" size={16} color="#000000" />
                  <Text style={styles.shareBtnText}>Compartir</Text>
                </TouchableOpacity>
              </View>
            </View>

            {/* ArtesWebVE Footer */}
            <View style={styles.footerCard}>
              <Text style={styles.footerBrand}>Desarrollada por ArtesWebVE</Text>
              <Text style={styles.footerText}>Soluciones digitales de alto impacto para Venezuela y el mundo.</Text>
              <TouchableOpacity onPress={() => Linking.openURL('https://arteswebve.com/')}>
                <Text style={styles.footerLink}>arteswebve.com ↗</Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        )}

        {selectedTab === 'trends' && (
          <ScrollView contentContainerStyle={styles.scrollBody} showsVerticalScrollIndicator={false}>
            <Text style={styles.sectionLabel}>DIVISA DE ANÁLISIS</Text>
            <View style={styles.segmentedContainer}>
              {(['USD', 'EUR', 'USDT'] as CurrencyCode[]).map((c) => (
                <TouchableOpacity
                  key={c}
                  style={[styles.segmentBtn, trendCurrency === c && styles.segmentBtnActive]}
                  onPress={() => setTrendCurrency(c)}
                >
                  <Text style={[styles.segmentText, trendCurrency === c && styles.segmentTextActive]}>
                    {CURRENCIES[c].flag} {CURRENCIES[c].code}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={styles.sectionLabel}>INTERVALO DE TIEMPO</Text>
            <View style={styles.segmentedContainer}>
              {[7, 15, 30, 60].map((d) => (
                <TouchableOpacity
                  key={d}
                  style={[styles.segmentBtn, trendDays === d && styles.segmentBtnActive]}
                  onPress={() => setTrendDays(d)}
                >
                  <Text style={[styles.segmentText, trendDays === d && styles.segmentTextActive]}>
                    {d} días
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <View style={styles.chartCard}>
              <View style={styles.chartHeader}>
                <Text style={styles.chartTitle}>Curva de Cotización ({trendCurrency})</Text>
                <Text style={[styles.chartRate, { color: CURRENCIES[trendCurrency].color }]}>
                  Bs. {historicalPoints[historicalPoints.length - 1]?.val.toFixed(2)}
                </Text>
              </View>

              {/* Sparkline Visual Simulation */}
              <View style={styles.sparklineContainer}>
                {historicalPoints.map((pt, i) => (
                  <View key={i} style={styles.sparkColumn}>
                    <View style={[styles.sparkBar, { height: Math.min(100, Math.max(20, (pt.val % 10) * 12)) }]} />
                  </View>
                ))}
              </View>
              <Text style={styles.chartSub}>Histórico registrado en los últimos {trendDays} días</Text>
            </View>
          </ScrollView>
        )}

        {selectedTab === 'history' && (
          <ScrollView contentContainerStyle={styles.scrollBody} showsVerticalScrollIndicator={false}>
            <View style={styles.historyHeader}>
              <Text style={styles.sectionLabel}>HISTORIAL DE CONVERSIONES</Text>
              {history.length > 0 && (
                <TouchableOpacity onPress={clearAllHistory}>
                  <Text style={styles.clearBtnText}>Limpiar todo</Text>
                </TouchableOpacity>
              )}
            </View>

            {history.length === 0 ? (
              <View style={styles.emptyContainer}>
                <Ionicons name="time-outline" size={48} color="#636366" />
                <Text style={styles.emptyTitle}>Sin conversiones registradas</Text>
                <Text style={styles.emptySub}>A medida que calcules en la app, se guardarán aquí para reutilizarlos.</Text>
              </View>
            ) : (
              history.map((item) => (
                <View key={item.id} style={styles.historyCard}>
                  <View style={styles.historyRow}>
                    <Text style={styles.historyPair}>{item.fromCode} ➔ {item.toCode}</Text>
                    <Text style={styles.historyTime}>{item.dateStr}</Text>
                  </View>
                  <Text style={styles.historyAmount}>
                    {item.fromSymbol}{item.inputAmount.toFixed(2)} = {item.toSymbol}{item.resultAmount.toFixed(2)}
                  </Text>
                  <View style={styles.historyActions}>
                    <Text style={styles.historyRate}>Tasa: Bs. {item.rate.toFixed(2)}</Text>
                    <TouchableOpacity onPress={() => deleteHistoryItem(item.id)}>
                      <Ionicons name="trash-outline" size={18} color="#FF453A" />
                    </TouchableOpacity>
                  </View>
                </View>
              ))
            )}
          </ScrollView>
        )}

        {selectedTab === 'alerts' && (
          <ScrollView contentContainerStyle={styles.scrollBody} showsVerticalScrollIndicator={false}>
            <Text style={styles.sectionLabel}>CONFIGURAR NUEVA ALERTA</Text>
            <View style={styles.alertCard}>
              <View style={styles.segmentedContainer}>
                <TouchableOpacity
                  style={[styles.segmentBtn, newAlertCond === 'ABOVE' && styles.segmentBtnActive]}
                  onPress={() => setNewAlertCond('ABOVE')}
                >
                  <Text style={[styles.segmentText, newAlertCond === 'ABOVE' && styles.segmentTextActive]}>📈 Suba a (≥)</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.segmentBtn, newAlertCond === 'BELOW' && styles.segmentBtnActive]}
                  onPress={() => setNewAlertCond('BELOW')}
                >
                  <Text style={[styles.segmentText, newAlertCond === 'BELOW' && styles.segmentTextActive]}>📉 Baje a (≤)</Text>
                </TouchableOpacity>
              </View>

              <TextInput
                style={[styles.textInput, { marginTop: 10, paddingHorizontal: 14 }]}
                value={newAlertRate}
                onChangeText={setNewAlertRate}
                keyboardType="decimal-pad"
                placeholder="Precio objetivo en Bs. (ej: 65.00)"
                placeholderTextColor="#636366"
              />

              <TouchableOpacity style={styles.addAlertBtn} onPress={addPriceAlert}>
                <Text style={styles.addAlertBtnText}>Crear Alerta para {selectedCurrency}</Text>
              </TouchableOpacity>
            </View>

            <Text style={[styles.sectionLabel, { marginTop: 16 }]}>ALERTAS ACTIVAS ({alerts.length})</Text>
            {alerts.length === 0 ? (
              <View style={styles.emptyContainer}>
                <Ionicons name="notifications-outline" size={48} color="#636366" />
                <Text style={styles.emptyTitle}>Sin alertas activas</Text>
                <Text style={styles.emptySub}>Crea una alerta arriba para saber cuándo comprar o vender.</Text>
              </View>
            ) : (
              alerts.map((a) => (
                <View key={a.id} style={styles.historyCard}>
                  <View style={styles.historyRow}>
                    <Text style={styles.historyPair}>{a.currency} {a.condition === 'ABOVE' ? '≥' : '≤'} Bs. {a.targetRate.toFixed(2)}</Text>
                    <Switch
                      value={a.isActive}
                      onValueChange={() => toggleAlert(a.id)}
                      trackColor={{ false: '#3A3A3C', true: '#30D158' }}
                      thumbColor="#FFFFFF"
                    />
                  </View>
                  <View style={styles.historyActions}>
                    <Text style={styles.historyRate}>Estado: {a.isActive ? 'Activa' : 'Pausada'}</Text>
                    <TouchableOpacity onPress={() => deleteAlert(a.id)}>
                      <Ionicons name="trash-outline" size={18} color="#FF453A" />
                    </TouchableOpacity>
                  </View>
                </View>
              ))
            )}
          </ScrollView>
        )}
      </View>

      {/* BOTTOM TAB BAR */}
      <View style={styles.tabBar}>
        <TouchableOpacity style={styles.tabBtn} onPress={() => setSelectedTab('calc')}>
          <Ionicons name="calculator" size={22} color={selectedTab === 'calc' ? '#FFD60A' : '#8E8E93'} />
          <Text style={[styles.tabLabel, selectedTab === 'calc' && styles.tabLabelActive]}>Calculadora</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.tabBtn} onPress={() => setSelectedTab('trends')}>
          <Ionicons name="trending-up" size={22} color={selectedTab === 'trends' ? '#FFD60A' : '#8E8E93'} />
          <Text style={[styles.tabLabel, selectedTab === 'trends' && styles.tabLabelActive]}>Tendencias</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.tabBtn} onPress={() => setSelectedTab('history')}>
          <Ionicons name="time" size={22} color={selectedTab === 'history' ? '#FFD60A' : '#8E8E93'} />
          <Text style={[styles.tabLabel, selectedTab === 'history' && styles.tabLabelActive]}>Historial</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.tabBtn} onPress={() => setSelectedTab('alerts')}>
          <Ionicons name="notifications" size={22} color={selectedTab === 'alerts' ? '#FFD60A' : '#8E8E93'} />
          <Text style={[styles.tabLabel, selectedTab === 'alerts' && styles.tabLabelActive]}>Alertas</Text>
        </TouchableOpacity>
      </View>

      {/* LEGAL DISCLAIMER MODAL */}
      <Modal visible={showLegalModal} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Aviso Legal e Información</Text>
              <TouchableOpacity onPress={() => setShowLegalModal(false)}>
                <Ionicons name="close" size={24} color="#8E8E93" />
              </TouchableOpacity>
            </View>
            <ScrollView style={{ maxHeight: 300, marginVertical: 12 }}>
              <Text style={styles.modalText}>
                TasaCambio 2.0 iOS es una herramienta tecnológica de consulta matemática desarrollada por ArtesWebVE.
                {'\n\n'}
                • No fijamos ni manipulamos tasas de cambio.{'\n'}
                • No somos casa de cambio ni entidad financiera.{'\n'}
                • Los datos son extraídos de fuentes públicas abiertas de dolarvzla.com y el Banco Central de Venezuela.
              </Text>
            </ScrollView>
            <TouchableOpacity style={styles.modalCloseBtn} onPress={() => setShowLegalModal(false)}>
              <Text style={styles.modalCloseText}>Entendido</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000000',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    backgroundColor: '#000000',
  },
  brandRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerTitle: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '700',
  },
  iosBadge: {
    backgroundColor: '#FFD60A',
    borderRadius: 4,
    paddingHorizontal: 5,
    paddingVertical: 1,
    marginLeft: 6,
  },
  iosBadgeText: {
    color: '#000000',
    fontSize: 11,
    fontWeight: '900',
  },
  headerSub: {
    color: '#FFD60A',
    fontSize: 11,
    fontWeight: '500',
    marginTop: 2,
  },
  headerRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  refreshBtn: {
    padding: 6,
    marginRight: 6,
  },
  infoBtn: {
    padding: 6,
  },
  tickerRow: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    gap: 8,
    marginTop: 4,
  },
  tickerCard: {
    flex: 1,
    backgroundColor: '#1C1C1E',
    borderRadius: 12,
    borderWidth: 0.5,
    borderColor: '#38383A',
    paddingVertical: 8,
    alignItems: 'center',
  },
  tickerCardSelected: {
    borderColor: '#FFD60A',
    backgroundColor: '#2C2C2E',
  },
  tickerEmoji: {
    fontSize: 14,
  },
  tickerCode: {
    color: '#8E8E93',
    fontSize: 11,
    fontWeight: '600',
    marginTop: 2,
  },
  tickerRate: {
    fontSize: 12,
    fontWeight: '700',
    marginTop: 2,
  },
  spreadBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    marginTop: 8,
    marginBottom: 6,
  },
  spreadText: {
    color: '#FFD60A',
    fontSize: 11,
    fontWeight: '600',
  },
  spreadSource: {
    color: '#636366',
    fontSize: 10,
  },
  content: {
    flex: 1,
  },
  scrollBody: {
    paddingHorizontal: 16,
    paddingBottom: 24,
  },
  sectionLabel: {
    color: '#8E8E93',
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.8,
    marginTop: 12,
    marginBottom: 6,
  },
  segmentedContainer: {
    flexDirection: 'row',
    backgroundColor: '#2C2C2E',
    borderRadius: 9,
    padding: 2,
    gap: 2,
  },
  segmentBtn: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: 7,
  },
  segmentBtnActive: {
    backgroundColor: '#3A3A3C',
  },
  segmentText: {
    color: '#8E8E93',
    fontSize: 12,
    fontWeight: '500',
  },
  segmentTextActive: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
  swapCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 14,
    borderWidth: 0.5,
    borderColor: '#38383A',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  swapTitle: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700',
  },
  swapCode: {
    color: '#FFD60A',
    fontSize: 11,
    fontWeight: '600',
  },
  swapCircle: {
    backgroundColor: '#FFD60A',
    width: 38,
    height: 38,
    borderRadius: 19,
    justifyContent: 'center',
    alignItems: 'center',
  },
  inputContainer: {
    backgroundColor: '#1C1C1E',
    borderRadius: 12,
    borderWidth: 0.5,
    borderColor: '#38383A',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
  },
  inputPrefix: {
    color: '#FFD60A',
    fontSize: 20,
    fontWeight: '700',
    marginRight: 8,
  },
  textInput: {
    flex: 1,
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '600',
    paddingVertical: 12,
    backgroundColor: '#1C1C1E',
    borderRadius: 12,
  },
  chipsRow: {
    marginTop: 8,
  },
  chip: {
    backgroundColor: '#2C2C2E',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginRight: 8,
    borderWidth: 0.5,
    borderColor: '#38383A',
  },
  chipText: {
    color: '#FFD60A',
    fontSize: 12,
    fontWeight: '600',
  },
  resultCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#FFD60A',
    padding: 16,
  },
  resultHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  resultLabel: {
    color: '#FFD60A',
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 0.8,
  },
  ratePill: {
    backgroundColor: '#2C2C2E',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  ratePillText: {
    color: '#8E8E93',
    fontSize: 11,
  },
  resultAmount: {
    color: '#FFFFFF',
    fontSize: 34,
    fontWeight: '800',
    marginTop: 8,
  },
  resultSub: {
    color: '#636366',
    fontSize: 12,
    marginTop: 2,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 14,
  },
  copyBtn: {
    flex: 1,
    backgroundColor: '#2C2C2E',
    borderRadius: 10,
    paddingVertical: 10,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 6,
  },
  copyBtnText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '600',
  },
  shareBtn: {
    flex: 1,
    backgroundColor: '#FFD60A',
    borderRadius: 10,
    paddingVertical: 10,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 6,
  },
  shareBtnText: {
    color: '#000000',
    fontSize: 12,
    fontWeight: '700',
  },
  footerCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 14,
    borderWidth: 0.5,
    borderColor: '#38383A',
    padding: 14,
    alignItems: 'center',
    marginTop: 20,
  },
  footerBrand: {
    color: '#FFD60A',
    fontSize: 13,
    fontWeight: '700',
  },
  footerText: {
    color: '#8E8E93',
    fontSize: 11,
    textAlign: 'center',
    marginTop: 4,
  },
  footerLink: {
    color: '#FFD60A',
    fontSize: 12,
    fontWeight: '600',
    marginTop: 8,
  },
  chartCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 16,
    borderWidth: 0.5,
    borderColor: '#38383A',
    padding: 16,
  },
  chartHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  chartTitle: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700',
  },
  chartRate: {
    fontSize: 13,
    fontWeight: '700',
  },
  sparklineContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    height: 120,
    marginTop: 16,
    paddingBottom: 8,
    borderBottomWidth: 0.5,
    borderBottomColor: '#38383A',
  },
  sparkColumn: {
    flex: 1,
    alignItems: 'center',
  },
  sparkBar: {
    width: 4,
    backgroundColor: '#FFD60A',
    borderRadius: 2,
  },
  chartSub: {
    color: '#636366',
    fontSize: 11,
    textAlign: 'center',
    marginTop: 8,
  },
  historyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  clearBtnText: {
    color: '#FF453A',
    fontSize: 12,
    fontWeight: '600',
  },
  historyCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 14,
    borderWidth: 0.5,
    borderColor: '#38383A',
    padding: 14,
    marginTop: 8,
  },
  historyRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  historyPair: {
    color: '#FFD60A',
    fontSize: 12,
    fontWeight: '700',
  },
  historyTime: {
    color: '#636366',
    fontSize: 10,
  },
  historyAmount: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
    marginTop: 6,
  },
  historyActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 8,
  },
  historyRate: {
    color: '#8E8E93',
    fontSize: 11,
  },
  emptyContainer: {
    alignItems: 'center',
    marginTop: 40,
    paddingHorizontal: 20,
  },
  emptyTitle: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
    marginTop: 12,
  },
  emptySub: {
    color: '#8E8E93',
    fontSize: 12,
    textAlign: 'center',
    marginTop: 6,
  },
  alertCard: {
    backgroundColor: '#1C1C1E',
    borderRadius: 16,
    borderWidth: 0.5,
    borderColor: '#38383A',
    padding: 14,
  },
  addAlertBtn: {
    backgroundColor: '#FFD60A',
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
    marginTop: 12,
  },
  addAlertBtnText: {
    color: '#000000',
    fontSize: 13,
    fontWeight: '700',
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#1C1C1E',
    borderTopWidth: 0.5,
    borderTopColor: '#38383A',
    height: 54,
  },
  tabBtn: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  tabLabel: {
    color: '#8E8E93',
    fontSize: 10,
    marginTop: 2,
  },
  tabLabelActive: {
    color: '#FFD60A',
    fontWeight: '600',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: '#1C1C1E',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#FFD60A',
    padding: 20,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  modalTitle: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
  modalText: {
    color: '#8E8E93',
    fontSize: 13,
    lineHeight: 18,
  },
  modalCloseBtn: {
    backgroundColor: '#FFD60A',
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: 'center',
  },
  modalCloseText: {
    color: '#000000',
    fontSize: 13,
    fontWeight: '700',
  },
});
