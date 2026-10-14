//
//  AlertsView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI

public struct AlertsView: View {
    @ObservedObject public var viewModel: RatesViewModel

    @State private var selectedCurrency: CurrencyType = .usd
    @State private var selectedCondition: AlertCondition = .above
    @State private var targetRateText: String = ""

    public init(viewModel: RatesViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        NavigationStack {
            Form {
                // Section: Create Alert
                Section {
                    Picker("Divisa", selection: ${'$'}selectedCurrency) {
                        Text("🇺🇸 USD").tag(CurrencyType.usd)
                        Text("🇪🇺 EUR").tag(CurrencyType.eur)
                        Text("🪙 USDT").tag(CurrencyType.usdt)
                    }
                    .pickerStyle(.segmented)

                    Picker("Condición", selection: ${'$'}selectedCondition) {
                        Text("📈 Suba de (≥)").tag(AlertCondition.above)
                        Text("📉 Baje de (≤)").tag(AlertCondition.below)
                    }
                    .pickerStyle(.segmented)

                    HStack {
                        Text("Bs.")
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                        TextField("Ej: 65.00", text: ${'$'}targetRateText)
                            .keyboardType(.decimalPad)
                    }

                    Button {
                        let clean = targetRateText.replacingOccurrences(of: ",", with: ".")
                        if let rate = Double(clean), rate > 0 {
                            viewModel.addAlert(currency: selectedCurrency, targetRate: rate, condition: selectedCondition)
                            targetRateText = ""
                        }
                    } label: {
                        Label("Crear Alerta", systemImage: "bell.badge.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(Color(red: 1.0, green: 0.84, blue: 0.04))
                    .foregroundColor(.black)
                } header: {
                    Text("NUEVA ALERTA DE PRECIO")
                }

                // Section: Active Alerts
                Section {
                    if viewModel.alertsList.isEmpty {
                        Text("No tienes alertas configuradas.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(viewModel.alertsList) { alert in
                            let liveRate = viewModel.currentRates.getRate(for: alert.currency)
                            let isTriggered = (alert.condition == .above) ? (liveRate >= alert.targetRate) : (liveRate <= alert.targetRate)

                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        Text(alert.currency.flagEmoji)
                                        Text("\(alert.currency.code) \(alert.condition == .above ? "≥" : "≤") Bs. \(String(format: "%.2f", alert.targetRate))")
                                            .font(.headline)
                                    }

                                    Text("Tasa actual: Bs. \(String(format: "%.2f", liveRate))")
                                        .font(.caption)
                                        .foregroundColor(alert.currency.accentColor)

                                    if isTriggered && alert.isActive {
                                        Text("🔔 ¡Condición alcanzada!")
                                            .font(.caption2)
                                            .fontWeight(.bold)
                                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                                    }
                                }

                                Spacer()

                                Toggle("", isOn: Binding(
                                    get: { alert.isActive },
                                    set: { _ in viewModel.toggleAlert(id: alert.id) }
                                ))
                                .labelsHidden()
                            }
                            .padding(.vertical, 4)
                            .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                Button(role: .destructive) {
                                    viewModel.deleteAlert(id: alert.id)
                                } label: {
                                    Label("Eliminar", systemImage: "trash")
                                }
                            }
                        }
                    }
                } header: {
                    Text("ALERTAS ACTIVAS (\(viewModel.alertsList.count))")
                }
            }
            .navigationTitle("Alertas")
        }
    }
}
