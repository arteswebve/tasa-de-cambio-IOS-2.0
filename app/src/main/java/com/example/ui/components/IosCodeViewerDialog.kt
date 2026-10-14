package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground

data class SwiftCodeSnippet(
    val fileName: String,
    val description: String,
    val code: String
)

@Composable
fun IosCodeViewerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val snippets = remember {
        val dollar = "$"
        listOf(
            SwiftCodeSnippet(
                fileName = "ContentView.swift",
                description = "Vista principal con TabView nativo estilo iOS 17/18",
                code = """//
//  ContentView.swift
//  TasaCambio 2.0 iOS
//  Desarrollada por ArtesWebVE (https://arteswebve.com)
//

import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = RatesViewModel()
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: """ + dollar + """selectedTab) {
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
        .tint(Color.yellow)
        .preferredColorScheme(.dark)
        .task {
            await viewModel.refreshRates()
        }
    }
}"""
            ),
            SwiftCodeSnippet(
                fileName = "RatesViewModel.swift",
                description = "Gestor reactivo @MainActor para tasas, cálculo y persistencia",
                code = """//
//  RatesViewModel.swift
//  TasaCambio 2.0 iOS
//

import Foundation
import Combine

@MainActor
final class RatesViewModel: ObservableObject {
    @Published var usdBcv: Double = 61.20
    @Published var eurBcv: Double = 66.85
    @Published var usdtBinance: Double = 68.90
    @Published var lastUpdated: String = "Hoy"
    @Published var isRefreshing: Bool = false

    @Published var inputAmount: String = "10"
    @Published var selectedCurrency: CurrencyType = .usd
    @Published var isInverse: Bool = false

    var currentRate: Double {
        switch selectedCurrency {
        case .usd: return usdBcv
        case .eur: return eurBcv
        case .usdt: return usdtBinance
        case .ves: return 1.0
        }
    }

    var calculatedTotal: Double {
        let amount = Double(inputAmount.replacingOccurrences(of: ",", with: ".")) ?? 0.0
        return isInverse ? (amount / currentRate) : (amount * currentRate)
    }

    func fetchLiveRates() async {
        isRefreshing = true
        defer { isRefreshing = false }
        do {
            let fetched = try await DolarVzlaService.shared.fetchRates()
            self.usdBcv = fetched.usdBcv
            self.eurBcv = fetched.eurBcv
            self.usdtBinance = fetched.usdtBinance
            self.lastUpdated = fetched.lastUpdatedFormatted
        } catch {
            print("Error al sincronizar dolarvzla.com")
        }
    }
}"""
            ),
            SwiftCodeSnippet(
                fileName = "DolarVzlaService.swift",
                description = "Servicio de red URLSession con API pública de dolarvzla.com",
                code = """//
//  DolarVzlaService.swift
//  TasaCambio 2.0 iOS
//

import Foundation

final class DolarVzlaService {
    static let shared = DolarVzlaService()
    private let endpoint = URL(string: "https://api.dolarvzla.com/v1/rates")!

    func fetchRates() async throws -> CurrentRates {
        var request = URLRequest(url: endpoint)
        request.httpMethod = "GET"
        request.timeoutInterval = 12

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            return CurrentRates()
        }
        return try JSONDecoder().decode(CurrentRates.self, from: data)
    }
}"""
            ),
            SwiftCodeSnippet(
                fileName = "CalculatorView.swift",
                description = "Vista estilo Inset Grouped con UISegmentedControl y ShareSheet",
                code = """//
//  CalculatorView.swift
//  TasaCambio 2.0 iOS
//

import SwiftUI

struct CalculatorView: View {
    @ObservedObject var viewModel: RatesViewModel

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("SELECCIÓN DE DIVISA")) {
                    Picker("Divisa", selection: """ + dollar + """viewModel.selectedCurrency) {
                        Text("🇺🇸 USD").tag(CurrencyType.usd)
                        Text("🇪🇺 EUR").tag(CurrencyType.eur)
                        Text("🪙 USDT").tag(CurrencyType.usdt)
                    }
                    .pickerStyle(.segmented)
                }

                Section(header: Text("MONTO A CALCULAR")) {
                    HStack {
                        Text(viewModel.isInverse ? "Bs." : viewModel.selectedCurrency.symbol)
                            .foregroundColor(.yellow)
                            .bold()
                        TextField("0.00", text: """ + dollar + """viewModel.inputAmount)
                            .keyboardType(.decimalPad)
                    }
                }

                Section(header: Text("TOTAL A RECIBIR")) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(String(format: "%.2f", viewModel.calculatedTotal))
                            .font(.system(size: 34, weight: .heavy, design: .rounded))
                            .foregroundColor(.white)
                        Text("Tasa oficial: Bs. \(viewModel.currentRate)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("TasaCambio")
        }
    }
}"""
            )
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }
    val activeSnippet = snippets[selectedIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
            border = BorderStroke(1.dp, IosSeparator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("ios_code_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = IosSystemYellow.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhoneIphone,
                                    contentDescription = null,
                                    tint = IosSystemYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Código Nativo iOS (Swift)",
                                color = IosLabelPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "100% compatible con Apple Xcode & SwiftUI",
                                color = IosSystemYellow,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = IosLabelSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File tabs row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    snippets.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            label = { Text(item.fileName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IosSystemYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = IosTertiarySystemBackground,
                                labelColor = IosLabelSecondary
                            ),
                            border = BorderStroke(1.dp, if (isSelected) IosSystemYellow else IosSeparator),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeSnippet.description,
                    color = IosLabelSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Code Box
                Surface(
                    color = Color(0xFF141416),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IosSeparator),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeSnippet.code,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Copy Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(activeSnippet.fileName, activeSnippet.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "¡${activeSnippet.fileName} copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosSystemYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("copy_swift_code_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar ${activeSnippet.fileName} para Xcode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
