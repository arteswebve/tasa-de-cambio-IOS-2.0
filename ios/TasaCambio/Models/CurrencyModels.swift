//
//  CurrencyModels.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import Foundation
import SwiftUI

public enum CurrencyType: String, CaseIterable, Identifiable, Codable {
    case usd = "USD"
    case eur = "EUR"
    case usdt = "USDT"
    case ves = "VES"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .usd: return "Dólar BCV"
        case .eur: return "Euro BCV"
        case .usdt: return "USDT Binance"
        case .ves: return "Bolívar Digital"
        }
    }

    public var code: String { rawValue }

    public var symbol: String {
        switch self {
        case .usd: return "$"
        case .eur: return "€"
        case .usdt: return "₮"
        case .ves: return "Bs."
        }
    }

    public var flagEmoji: String {
        switch self {
        case .usd: return "🇺🇸"
        case .eur: return "🇪🇺"
        case .usdt: return "🪙"
        case .ves: return "🇻🇪"
        }
    }

    public var accentColor: Color {
        switch self {
        case .usd: return Color(red: 0.19, green: 0.82, blue: 0.35) // iOS Green
        case .eur: return Color(red: 1.0, green: 0.62, blue: 0.04)  // iOS Orange
        case .usdt: return Color(red: 0.39, green: 0.82, blue: 1.0) // iOS Cyan
        case .ves: return Color(red: 0.04, green: 0.52, blue: 1.0)  // iOS Blue
        }
    }
}

public struct CurrentRates: Codable {
    public var usdBcv: Double
    public var eurBcv: Double
    public var usdtBinance: Double
    public var lastUpdatedFormatted: String
    public var spreadPercentage: Double

    public init(
        usdBcv: Double = 61.20,
        eurBcv: Double = 66.85,
        usdtBinance: Double = 68.90,
        lastUpdatedFormatted: String = "Hoy",
        spreadPercentage: Double = 12.5
    ) {
        self.usdBcv = usdBcv
        self.eurBcv = eurBcv
        self.usdtBinance = usdtBinance
        self.lastUpdatedFormatted = lastUpdatedFormatted
        self.spreadPercentage = spreadPercentage
    }

    public func getRate(for currency: CurrencyType) -> Double {
        switch currency {
        case .usd: return usdBcv
        case .eur: return eurBcv
        case .usdt: return usdtBinance
        case .ves: return 1.0
        }
    }
}

public struct CalculationRecord: Identifiable, Codable {
    public let id: UUID
    public let fromCurrency: CurrencyType
    public let toCurrency: CurrencyType
    public let inputAmount: Double
    public let resultAmount: Double
    public let rateApplied: Double
    public let timestamp: Date

    public init(
        id: UUID = UUID(),
        fromCurrency: CurrencyType,
        toCurrency: CurrencyType,
        inputAmount: Double,
        resultAmount: Double,
        rateApplied: Double,
        timestamp: Date = Date()
    ) {
        self.id = id
        self.fromCurrency = fromCurrency
        self.toCurrency = toCurrency
        self.inputAmount = inputAmount
        self.resultAmount = resultAmount
        self.rateApplied = rateApplied
        self.timestamp = timestamp
    }
}

public struct RateAlert: Identifiable, Codable {
    public let id: UUID
    public let currency: CurrencyType
    public let targetRate: Double
    public let condition: AlertCondition
    public var isActive: Bool
    public let createdAt: Date

    public init(
        id: UUID = UUID(),
        currency: CurrencyType,
        targetRate: Double,
        condition: AlertCondition,
        isActive: Bool = true,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.currency = currency
        self.targetRate = targetRate
        self.condition = condition
        self.isActive = isActive
        self.createdAt = createdAt
    }
}

public enum AlertCondition: String, Codable {
    case above = "ABOVE"
    case below = "BELOW"
}

public struct HistoricalRatePoint: Identifiable, Codable {
    public let id = UUID()
    public let date: Date
    public let dateStr: String
    public let rate: Double
}
