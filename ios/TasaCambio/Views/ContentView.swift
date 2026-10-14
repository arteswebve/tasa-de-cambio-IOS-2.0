//
//  ContentView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI

public struct ContentView: View {
    @StateObject private var viewModel = RatesViewModel()
    @State private var selectedTab: Int = 0

    public init() {}

    public var body: some View {
        TabView(selection: $selectedTab) {
            CalculatorView(viewModel: viewModel)
                .tabItem {
                    Label("Calculadora", systemImage: "plus.forwardslash.minus")
                }
                .tag(0)

            TrendsView(viewModel: viewModel)
                .tabItem {
                    Label("Tendencias", systemImage: "chart.line.uptrend.xyaxis")
                }
                .tag(1)

            HistoryView(viewModel: viewModel)
                .tabItem {
                    Label("Historial", systemImage: "clock.arrow.circlepath")
                }
                .tag(2)

            AlertsView(viewModel: viewModel)
                .tabItem {
                    Label("Alertas", systemImage: "bell.badge.fill")
                }
                .tag(3)
        }
        .tint(Color(red: 1.0, green: 0.84, blue: 0.04)) // iOS System Yellow
        .preferredColorScheme(.dark)
        .task {
            await viewModel.refreshRates()
        }
    }
}

#Preview {
    ContentView()
}
