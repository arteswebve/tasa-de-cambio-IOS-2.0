//
//  DolarVzlaService.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import Foundation

public final class DolarVzlaService {
    public static let shared = DolarVzlaService()

    private let session: URLSession

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 12
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        self.session = URLSession(configuration: config)
    }

    public struct ApiDto: Codable {
        public let usd: Double?
        public let eur: Double?
        public let usdt: Double?
        public let date: String?
    }

    /// Fetches official exchange rates from public dolarvzla.com API
    public func fetchRates() async throws -> CurrentRates {
        guard let url = URL(string: "https://api.dolarvzla.com/v1/rates") else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("TasaCambio-iOS/2.0 (ArtesWebVE)", forHTTPHeaderField: "User-Agent")

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            // Fallback graceful values if offline
            return CurrentRates()
        }

        let decoder = JSONDecoder()
        if let dto = try? decoder.decode(ApiDto.self, from: data) {
            let usd = dto.usd ?? 61.20
            let eur = dto.eur ?? 66.85
            let usdt = dto.usdt ?? 68.90
            let spread = usd > 0 ? ((usdt - usd) / usd) * 100.0 : 0.0

            let formatter = DateFormatter()
            formatter.dateFormat = "dd/MM/yyyy HH:mm"
            let nowStr = formatter.string(from: Date())

            return CurrentRates(
                usdBcv: usd,
                eurBcv: eur,
                usdtBinance: usdt,
                lastUpdatedFormatted: "Hoy, \(nowStr)",
                spreadPercentage: spread
            )
        }

        return CurrentRates()
    }
}
