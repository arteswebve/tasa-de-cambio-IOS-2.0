//
//  CalculatorView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI

public struct CalculatorView: View {
    @ObservedObject public var viewModel: RatesViewModel
    @State private var showShareSheet: Bool = false
    @State private var shareText: String = ""
    @State private var showInfoModal: Bool = false

    public init(viewModel: RatesViewModel) {
        self.viewModel = viewModel
    }

    private var sourceCurrency: CurrencyType {
        viewModel.isInverseDirection ? .ves : viewModel.selectedCurrency
    }

    private var targetCurrency: CurrencyType {
        viewModel.isInverseDirection ? viewModel.selectedCurrency : .ves
    }

    public var body: some View {
        NavigationStack {
            Form {
                // Section: Currency Selection
                Section {
                    Picker("Divisa", selection: ${'$'}viewModel.selectedCurrency) {
                        Text("🇺🇸 USD").tag(CurrencyType.usd)
                        Text("🇪🇺 EUR").tag(CurrencyType.eur)
                        Text("🪙 USDT").tag(CurrencyType.usdt)
                    }
                    .pickerStyle(.segmented)
                } header: {
                    Text("SELECCIÓN DE DIVISA")
                }

                // Section: Direction & Swap
                Section {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(sourceCurrency.title)
                                .font(.headline)
                            Text(sourceCurrency.code)
                                .font(.caption)
                                .foregroundColor(sourceCurrency.accentColor)
                        }

                        Spacer()

                        Button {
                            withAnimation(.spring()) {
                                viewModel.toggleDirection()
                            }
                        } label: {
                            Image(systemName: "arrow.up.arrow.down.circle.fill")
                                .font(.system(size: 32))
                                .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                        }
                        .buttonStyle(.plain)

                        Spacer()

                        VStack(alignment: .trailing, spacing: 2) {
                            Text(targetCurrency.title)
                                .font(.headline)
                            Text(targetCurrency.code)
                                .font(.caption)
                                .foregroundColor(targetCurrency.accentColor)
                        }
                    }
                    .padding(.vertical, 4)
                } header: {
                    Text("DIRECCIÓN DE CONVERSIÓN")
                }

                // Section: Input Amount
                Section {
                    HStack {
                        Text(sourceCurrency.symbol)
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))

                        TextField("0.00", text: ${'$'}viewModel.inputAmountText)
                            .font(.title2)
                            .keyboardType(.decimalPad)
                    }

                    // Quick Chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            let quick = viewModel.isInverseDirection ? [200.0, 500.0, 1000.0, 2500.0, 5000.0] : [5.0, 10.0, 20.0, 50.0, 100.0]
                            ForEach(quick, id: \.self) { amt in
                                Button {
                                    viewModel.setQuickAmount(amt)
                                } label: {
                                    Text(viewModel.isInverseDirection ? "Bs. \(Int(amt))" : "\(sourceCurrency.symbol)\(Int(amt))")
                                        .font(.caption)
                                        .fontWeight(.semibold)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(Color(red: 0.17, green: 0.17, blue: 0.18))
                                        .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                                        .cornerRadius(12)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                } header: {
                    Text("MONTO A CALCULAR")
                }

                // Section: Calculated Result
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("TOTAL A RECIBIR")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                            Spacer()
                            Text("Tasa: Bs. \(String(format: "%.2f", viewModel.currentRate))")
                                .font(.caption2)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(red: 0.17, green: 0.17, blue: 0.18))
                                .cornerRadius(6)
                        }

                        Text("\(targetCurrency.symbol) \(String(format: "%.2f", viewModel.calculatedResult))")
                            .font(.system(size: 34, weight: .bold, design: .rounded))
                            .foregroundColor(.white)

                        Text("Conversión de \(String(format: "%.2f", viewModel.parsedInputAmount)) \(sourceCurrency.code) a \(targetCurrency.code)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 4)

                    // Action buttons
                    HStack(spacing: 12) {
                        Button {
                            UIPasteboard.general.string = "\(sourceCurrency.symbol) \(String(format: "%.2f", viewModel.parsedInputAmount)) = \(targetCurrency.symbol) \(String(format: "%.2f", viewModel.calculatedResult)) (Tasa: Bs. \(String(format: "%.2f", viewModel.currentRate)) - TasaCambio 2.0 iOS)"
                            viewModel.saveCalculation()
                        } label: {
                            Label("Copiar", systemImage: "doc.on.doc")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)

                        Button {
                            shareText = "💱 *Cálculo de TasaCambio 2.0 iOS*\n\n" +
                                        "• Monto: \(sourceCurrency.symbol) \(String(format: "%.2f", viewModel.parsedInputAmount))\n" +
                                        "• Tasa: Bs. \(String(format: "%.2f", viewModel.currentRate))\n" +
                                        "• Total: *\(targetCurrency.symbol) \(String(format: "%.2f", viewModel.calculatedResult))*\n\n" +
                                        "ArtesWebVE | https://arteswebve.com"
                            showShareSheet = true
                            viewModel.saveCalculation()
                        } label: {
                            Label("Compartir", systemImage: "square.and.arrow.up")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(Color(red: 1.0, green: 0.84, blue: 0.04))
                        .foregroundColor(.black)
                    }
                } header: {
                    Text("RESULTADO")
                }

                // Section: Developer Info
                Section {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Desarrollada por ArtesWebVE")
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                        Text("Soluciones digitales profesionales para Venezuela y el mundo.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Link("Visitar arteswebve.com ↗", destination: URL(string: "https://arteswebve.com/")!)
                            .font(.caption)
                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.04))
                    }
                    .padding(.vertical, 4)
                } header: {
                    Text("ACERCA DE")
                }
            }
            .navigationTitle("TasaCambio")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        Task { await viewModel.refreshRates() }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .sheet(isPresented: ${'$'}showShareSheet) {
                ShareSheet(activityItems: [shareText])
            }
        }
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    var activityItems: [Any]
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
