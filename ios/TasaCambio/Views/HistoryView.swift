//
//  HistoryView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI

public struct HistoryView: View {
    @ObservedObject public var viewModel: RatesViewModel

    public init(viewModel: RatesViewModel) {
        self.viewModel = viewModel
    }

    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "dd/MM/yyyy HH:mm"
        return f
    }()

    public var body: some View {
        NavigationStack {
            Group {
                if viewModel.historyList.isEmpty {
                    ContentUnavailableView(
                        "Sin conversiones recientes",
                        systemImage: "clock.arrow.circlepath",
                        description: Text("Los cálculos realizados en la calculadora se guardarán aquí para consultarlos o reutilizarlos.")
                    )
                } else {
                    List {
                        ForEach(viewModel.historyList) { item in
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Text("\(item.fromCurrency.flagEmoji) \(item.fromCurrency.code) ➔ \(item.toCurrency.flagEmoji) \(item.toCurrency.code)")
                                        .font(.caption)
                                        .fontWeight(.bold)
                                        .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                                    Spacer()
                                    Text(dateFormatter.string(from: item.timestamp))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }

                                HStack {
                                    VStack(alignment: .leading) {
                                        Text("\(item.fromCurrency.symbol) \(String(format: "%.2f", item.inputAmount))")
                                            .font(.subheadline)
                                            .foregroundColor(.secondary)
                                        Text("\(item.toCurrency.symbol) \(String(format: "%.2f", item.resultAmount))")
                                            .font(.title3)
                                            .fontWeight(.bold)
                                    }

                                    Spacer()

                                    Button {
                                        viewModel.inputAmountText = String(format: "%.2f", item.inputAmount)
                                        viewModel.selectedCurrency = (item.fromCurrency == .ves) ? item.toCurrency : item.fromCurrency
                                        viewModel.isInverseDirection = (item.fromCurrency == .ves)
                                    } label: {
                                        Text("Reutilizar")
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .padding(.horizontal, 10)
                                            .padding(.vertical, 6)
                                            .background(Color(red: 0.17, green: 0.17, blue: 0.18))
                                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                                            .cornerRadius(8)
                                    }
                                    .buttonStyle(.plain)
                                }

                                Text("Tasa aplicada: Bs. \(String(format: "%.2f", item.rateApplied))")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            .padding(.vertical, 4)
                            .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                Button(role: .destructive) {
                                    viewModel.deleteHistory(id: item.id)
                                } label: {
                                    Label("Eliminar", systemImage: "trash")
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Historial")
            .toolbar {
                if !viewModel.historyList.isEmpty {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button(role: .destructive) {
                            viewModel.clearAllHistory()
                        } label: {
                            Text("Limpiar")
                                .foregroundColor(.red)
                        }
                    }
                }
            }
        }
    }
}
