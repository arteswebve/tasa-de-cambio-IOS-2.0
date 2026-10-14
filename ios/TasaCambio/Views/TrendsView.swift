//
//  TrendsView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI
import Charts

public struct TrendsView: View {
    @ObservedObject public var viewModel: RatesViewModel

    public init(viewModel: RatesViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        NavigationStack {
            Form {
                // Section: Selectors
                Section {
                    Picker("Divisa", selection: ${'$'}viewModel.trendCurrency) {
                        Text("🇺🇸 USD").tag(CurrencyType.usd)
                        Text("🇪🇺 EUR").tag(CurrencyType.eur)
                        Text("🪙 USDT").tag(CurrencyType.usdt)
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: viewModel.trendCurrency) { _ in
                        viewModel.generateHistoricalSnapshots()
                    }

                    Picker("Periodo", selection: ${'$'}viewModel.trendPeriodDays) {
                        Text("7 días").tag(7)
                        Text("15 días").tag(15)
                        Text("30 días").tag(30)
                        Text("60 días").tag(60)
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: viewModel.trendPeriodDays) { _ in
                        viewModel.generateHistoricalSnapshots()
                    }
                } header: {
                    Text("FILTROS")
                }

                // Section: Interactive Chart
                Section {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("Curva de Cotización (\(viewModel.trendCurrency.code))")
                                .font(.headline)
                            Spacer()
                            Text("Bs. \(String(format: "%.2f", viewModel.historicalPoints.last?.rate ?? viewModel.currentRate))")
                                .font(.headline)
                                .foregroundColor(viewModel.trendCurrency.accentColor)
                        }

                        if #available(iOS 16.0, *) {
                            Chart(viewModel.historicalPoints) { point in
                                AreaMark(
                                    x: .value("Fecha", point.date),
                                    y: .value("Tasa", point.rate)
                                )
                                .foregroundStyle(
                                    LinearGradient(
                                        colors: [viewModel.trendCurrency.accentColor.opacity(0.35), .clear],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )

                                LineMark(
                                    x: .value("Fecha", point.date),
                                    y: .value("Tasa", point.rate)
                                )
                                .foregroundStyle(viewModel.trendCurrency.accentColor)
                                .lineStyle(StrokeStyle(lineWidth: 2.5))
                            }
                            .frame(height: 180)
                            .chartYScale(domain: .automatic(includesZero: false))
                        } else {
                            Text("Gráfico disponible en iOS 16 o superior.")
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 4)
                } header: {
                    Text("GRÁFICO INTERACTIVO")
                }

                // Section: Historical Table
                Section {
                    ForEach(viewModel.historicalPoints.reversed()) { point in
                        HStack {
                            Text(point.dateStr)
                                .font(.subheadline)
                            Spacer()
                            Text("Bs. \(String(format: "%.2f", point.rate))")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(viewModel.trendCurrency.accentColor)
                        }
                    }
                } header: {
                    Text("COTIZACIONES REGISTRADAS (\(viewModel.historicalPoints.count) DÍAS)")
                }
            }
            .navigationTitle("Tendencias")
        }
    }
}
