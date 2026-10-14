//
//  RatesViewModel.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import Foundation
import Combine
import SwiftUI

@MainActor
public final class RatesViewModel: ObservableObject {
    @Published public var currentRates: CurrentRates = CurrentRates()
    @Published public var isRefreshing: Bool = false

    // Calculator State
    @Published public var inputAmountText: String = "10"
    @Published public var selectedCurrency: CurrencyType = .usd
    @Published public var isInverseDirection: Bool = false

    // Trends & History
    @Published public var trendCurrency: CurrencyType = .usd
    @Published public var trendPeriodDays: Int = 30
    @Published public var historyList: [CalculationRecord] = []
    @Published public var alertsList: [RateAlert] = []
    @Published public var historicalPoints: [HistoricalRatePoint] = []

    public init() {
        loadMockHistoryAndAlerts()
        generateHistoricalSnapshots()
    }

    public var currentRate: Double {
        currentRates.getRate(for: selectedCurrency)
    }

    public var parsedInputAmount: Double {
        let clean = inputAmountText.replacingOccurrences(of: ",", with: ".")
        return Double(clean) ?? 0.0
    }

    public var calculatedResult: Double {
        let amount = parsedInputAmount
        let rate = currentRate
        if isInverseDirection {
            return rate > 0 ? (amount / rate) : 0.0
        } else {
            return amount * rate
        }
    }

    public func refreshRates() async {
        isRefreshing = true
        defer { isRefreshing = false }
        do {
            let rates = try await DolarVzlaService.shared.fetchRates()
            self.currentRates = rates
            generateHistoricalSnapshots()
        } catch {
            print("Rates refresh error: \(error.localizedDescription)")
        }
    }

    public func toggleDirection() {
        isInverseDirection.toggle()
    }

    public func setQuickAmount(_ amount: Double) {
        inputAmountText = String(format: "%.0f", amount)
    }

    public func saveCalculation() {
        let sourceCurr = isInverseDirection ? CurrencyType.ves : selectedCurrency
        let targetCurr = isInverseDirection ? selectedCurrency : CurrencyType.ves

        let record = CalculationRecord(
            fromCurrency: sourceCurr,
            toCurrency: targetCurr,
            inputAmount: parsedInputAmount,
            resultAmount: calculatedResult,
            rateApplied: currentRate
        )
        historyList.insert(record, at: 0)
    }

    public func deleteHistory(id: UUID) {
        historyList.removeAll { $0.id == id }
    }

    public func clearAllHistory() {
        historyList.removeAll()
    }

    public func addAlert(currency: CurrencyType, targetRate: Double, condition: AlertCondition) {
        let newAlert = RateAlert(currency: currency, targetRate: targetRate, condition: condition)
        alertsList.append(newAlert)
    }

    public func deleteAlert(id: UUID) {
        alertsList.removeAll { $0.id == id }
    }

    public func toggleAlert(id: UUID) {
        if let idx = alertsList.firstIndex(where: { $0.id == id }) {
            alertsList[idx].isActive.toggle()
        }
    }

    private func loadMockHistoryAndAlerts() {
        historyList = [
            CalculationRecord(fromCurrency: .usd, toCurrency: .ves, inputAmount: 100, resultAmount: 6120, rateApplied: 61.20),
            CalculationRecord(fromCurrency: .usdt, toCurrency: .ves, inputAmount: 50, resultAmount: 3445, rateApplied: 68.90),
            CalculationRecord(fromCurrency: .ves, toCurrency: .eur, inputAmount: 5000, resultAmount: 74.79, rateApplied: 66.85)
        ]

        alertsList = [
            RateAlert(currency: .usd, targetRate: 65.00, condition: .above, isActive: true),
            RateAlert(currency: .usdt, targetRate: 70.00, condition: .above, isActive: true)
        ]
    }

    public func generateHistoricalSnapshots() {
        let base = currentRates.getRate(for: trendCurrency)
        var points: [HistoricalRatePoint] = []
        let calendar = Calendar.current
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM"

        for i in (0..<trendPeriodDays).reversed() {
            if let date = calendar.date(byAdding: .day, value: -i, to: Date()) {
                let factor = 1.0 - (Double(i) * 0.004) + Double.random(in: -0.005...0.005)
                let val = base * factor
                points.append(HistoricalRatePoint(date: date, dateStr: formatter.string(from: date), rate: max(1.0, val)))
            }
        }
        self.historicalPoints = points
    }
}
