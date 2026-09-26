package com.example.data.webserver

object WebDesktopAppHtml {

    fun getHtml(): String {
        return """<!DOCTYPE html>
<html lang="pt-BR" data-theme="dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>BUMoney Desktop • Painel Financeiro PC</title>
  <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%2310B981'><path d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14.5h-2v-2h2v2zm0-4h-2V7h2v5.5z'/></svg>">
  <style>
    :root {
      --bg-base: #0B0F19;
      --bg-surface: #111827;
      --bg-card: #182234;
      --bg-hover: #222F46;
      --border-color: rgba(255, 255, 255, 0.08);
      --border-focus: #10B981;
      --text-main: #F3F4F6;
      --text-muted: #9CA3AF;
      --text-dim: #6B7280;
      --primary: #10B981;
      --primary-hover: #059669;
      --primary-light: rgba(16, 185, 129, 0.15);
      --income: #10B981;
      --expense: #EF4444;
      --expense-light: rgba(239, 68, 68, 0.15);
      --warning: #F59E0B;
      --info: #3B82F6;
      --sidebar-width: 250px;
      --header-height: 64px;
      --radius-sm: 8px;
      --radius-md: 12px;
      --radius-lg: 16px;
      --shadow: 0 4px 20px rgba(0, 0, 0, 0.35);
      --font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }

    [data-theme="light"] {
      --bg-base: #F8FAFC;
      --bg-surface: #FFFFFF;
      --bg-card: #F1F5F9;
      --bg-hover: #E2E8F0;
      --border-color: rgba(0, 0, 0, 0.08);
      --text-main: #0F172A;
      --text-muted: #475569;
      --text-dim: #94A3B8;
      --primary: #059669;
      --primary-hover: #047857;
      --primary-light: rgba(5, 150, 105, 0.12);
      --shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: var(--font-family);
      background-color: var(--bg-base);
      color: var(--text-main);
      display: flex;
      height: 100vh;
      overflow: hidden;
      font-size: 14px;
    }

    /* SIDEBAR */
    aside {
      width: var(--sidebar-width);
      background: var(--bg-surface);
      border-right: 1px solid var(--border-color);
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      user-select: none;
    }
    .brand {
      height: var(--header-height);
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0 20px;
      border-bottom: 1px solid var(--border-color);
    }
    .brand-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #10B981, #059669);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 800;
      font-size: 16px;
    }
    .brand-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--text-main);
      letter-spacing: -0.3px;
    }
    .brand-badge {
      font-size: 10px;
      background: var(--primary-light);
      color: var(--primary);
      padding: 2px 6px;
      border-radius: 4px;
      font-weight: 600;
      margin-left: auto;
    }
    .p2p-status-box {
      margin: 12px 16px;
      padding: 10px 12px;
      background: var(--bg-card);
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
    }
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #EF4444;
      flex-shrink: 0;
      transition: background 0.3s;
    }
    .status-dot.active {
      background: #10B981;
      box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
    }

    nav {
      flex: 1;
      padding: 8px 12px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      overflow-y: auto;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      border-radius: var(--radius-sm);
      color: var(--text-muted);
      cursor: pointer;
      font-weight: 500;
      transition: all 0.15s ease;
      text-decoration: none;
    }
    .nav-item:hover {
      background: var(--bg-hover);
      color: var(--text-main);
    }
    .nav-item.active {
      background: var(--primary-light);
      color: var(--primary);
      font-weight: 600;
    }
    .nav-item svg {
      width: 18px;
      height: 18px;
      stroke-width: 2;
    }

    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid var(--border-color);
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .shortcuts-hint {
      font-size: 11px;
      color: var(--text-dim);
      line-height: 1.5;
    }
    .kbd {
      background: var(--bg-hover);
      padding: 2px 5px;
      border-radius: 4px;
      font-family: monospace;
      color: var(--text-muted);
    }

    /* MAIN CONTAINER */
    main {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    /* TOP HEADER */
    header {
      height: var(--header-height);
      background: var(--bg-surface);
      border-bottom: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      gap: 16px;
      flex-shrink: 0;
    }
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .page-title {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.4px;
    }
    .month-nav {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--bg-card);
      padding: 4px 8px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-color);
      font-size: 13px;
      font-weight: 600;
    }
    .month-nav button {
      background: none;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 4px;
      border-radius: 4px;
      transition: background 0.15s;
    }
    .month-nav button:hover {
      background: var(--bg-hover);
      color: var(--text-main);
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 8px 14px;
      border-radius: var(--radius-sm);
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s ease;
      border: 1px solid transparent;
      outline: none;
      text-decoration: none;
    }
    .btn-primary {
      background: var(--primary);
      color: #FFFFFF;
    }
    .btn-primary:hover {
      background: var(--primary-hover);
    }
    .btn-secondary {
      background: var(--bg-card);
      color: var(--text-main);
      border: 1px solid var(--border-color);
    }
    .btn-secondary:hover {
      background: var(--bg-hover);
    }
    .btn-icon {
      padding: 8px;
      border-radius: var(--radius-sm);
      background: var(--bg-card);
      color: var(--text-muted);
      border: 1px solid var(--border-color);
    }
    .btn-icon:hover {
      color: var(--text-main);
      background: var(--bg-hover);
    }

    /* CONTENT SCROLLER */
    .content-area {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      overflow-x: hidden;
    }

    /* METRIC CARDS GRID */
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }
    .metric-card {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 18px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      position: relative;
      overflow: hidden;
      box-shadow: var(--shadow);
    }
    .metric-card::before {
      content: "";
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 3px;
      background: var(--primary);
      opacity: 0.8;
    }
    .metric-card.expense::before { background: var(--expense); }
    .metric-card.balance::before { background: var(--info); }
    .metric-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .metric-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-main);
      letter-spacing: -0.5px;
    }
    .metric-value.income { color: var(--income); }
    .metric-value.expense { color: var(--expense); }
    .metric-sub {
      font-size: 12px;
      color: var(--text-dim);
    }

    /* BUDGET PROGRESS BANNER */
    .budget-banner {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 16px 20px;
      margin-bottom: 24px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .budget-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      font-weight: 600;
    }
    .progress-bar-bg {
      height: 8px;
      background: var(--bg-card);
      border-radius: 99px;
      overflow: hidden;
    }
    .progress-bar-fill {
      height: 100%;
      background: var(--primary);
      border-radius: 99px;
      transition: width 0.4s ease;
    }

    /* CHARTS & VISUAL ROW */
    .charts-row {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 16px;
      margin-bottom: 24px;
    }
    .chart-card {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .chart-title {
      font-size: 15px;
      font-weight: 700;
      color: var(--text-main);
    }

    /* CATEGORY DISTRIBUTION BARS */
    .cat-bars {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .cat-bar-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .cat-bar-header {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      font-weight: 500;
    }
    .cat-progress {
      height: 6px;
      background: var(--bg-card);
      border-radius: 4px;
      overflow: hidden;
    }
    .cat-progress-fill {
      height: 100%;
      border-radius: 4px;
    }

    /* DATA TABLE */
    .table-container {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      overflow: hidden;
      box-shadow: var(--shadow);
    }
    .table-toolbar {
      padding: 14px 18px;
      border-bottom: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      flex-wrap: wrap;
    }
    .search-box {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--bg-card);
      padding: 6px 12px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-color);
      width: 280px;
    }
    .search-box input {
      background: none;
      border: none;
      outline: none;
      color: var(--text-main);
      width: 100%;
      font-size: 13px;
    }
    .filter-group {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    select.filter-select {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      color: var(--text-main);
      padding: 7px 10px;
      border-radius: var(--radius-sm);
      font-size: 13px;
      outline: none;
      cursor: pointer;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    th {
      background: var(--bg-card);
      color: var(--text-muted);
      font-size: 12px;
      font-weight: 600;
      text-transform: uppercase;
      padding: 12px 16px;
      border-bottom: 1px solid var(--border-color);
      user-select: none;
    }
    td {
      padding: 12px 16px;
      border-bottom: 1px solid var(--border-color);
      font-size: 13px;
      vertical-align: middle;
    }
    tr:hover td {
      background: var(--bg-hover);
    }
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
    }
    .badge-income { background: var(--primary-light); color: var(--income); }
    .badge-expense { background: var(--expense-light); color: var(--expense); }
    .badge-tag { background: var(--bg-card); color: var(--text-muted); border: 1px solid var(--border-color); }

    .actions-cell {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .action-btn {
      background: none;
      border: none;
      color: var(--text-dim);
      padding: 4px;
      border-radius: 4px;
      cursor: pointer;
      transition: color 0.15s, background 0.15s;
    }
    .action-btn:hover {
      color: var(--text-main);
      background: var(--bg-hover);
    }
    .action-btn.delete:hover {
      color: var(--expense);
    }

    /* CARDS GRID */
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
    }
    .credit-card-item {
      background: linear-gradient(135deg, #2D1B69, #1E1035);
      border-radius: var(--radius-lg);
      padding: 24px;
      color: white;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 200px;
      position: relative;
      overflow: hidden;
      box-shadow: 0 10px 25px rgba(0,0,0,0.4);
      border: 1px solid rgba(255,255,255,0.1);
    }
    .credit-card-chip {
      width: 36px;
      height: 26px;
      background: linear-gradient(135deg, #e0c878, #b8860b);
      border-radius: 4px;
    }

    /* MODAL */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.2s ease;
    }
    .modal-overlay.active {
      opacity: 1;
      pointer-events: auto;
    }
    .modal {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      width: 100%;
      max-width: 520px;
      padding: 24px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.5);
      display: flex;
      flex-direction: column;
      gap: 16px;
      transform: translateY(10px);
      transition: transform 0.2s ease;
    }
    .modal-overlay.active .modal {
      transform: translateY(0);
    }
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 12px;
    }
    .modal-title {
      font-size: 16px;
      font-weight: 700;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .form-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-muted);
    }
    .form-input, .form-select, .form-textarea {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      color: var(--text-main);
      padding: 8px 12px;
      border-radius: var(--radius-sm);
      font-size: 13px;
      outline: none;
      transition: border-color 0.15s;
    }
    .form-input:focus, .form-select:focus, .form-textarea:focus {
      border-color: var(--border-focus);
    }
    .type-toggle {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
    }
    .type-btn {
      padding: 8px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-color);
      background: var(--bg-card);
      color: var(--text-muted);
      cursor: pointer;
      font-weight: 600;
      text-align: center;
      transition: all 0.15s;
    }
    .type-btn.active.expense {
      background: var(--expense-light);
      color: var(--expense);
      border-color: var(--expense);
    }
    .type-btn.active.income {
      background: var(--primary-light);
      color: var(--income);
      border-color: var(--primary);
    }
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 12px;
      border-top: 1px solid var(--border-color);
    }

    /* TOAST */
    .toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      z-index: 2000;
    }
    .toast {
      background: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-left: 4px solid var(--primary);
      padding: 12px 18px;
      border-radius: var(--radius-sm);
      box-shadow: 0 10px 30px rgba(0,0,0,0.4);
      display: flex;
      align-items: center;
      gap: 10px;
      animation: slideIn 0.25s ease;
      font-size: 13px;
      font-weight: 500;
    }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  </style>
</head>
<body>

  <!-- SIDEBAR -->
  <aside>
    <div class="brand">
      <div class="brand-icon">B</div>
      <div class="brand-title">BUMoney PC</div>
      <div class="brand-badge">P2P</div>
    </div>

    <div class="p2p-status-box">
      <div id="p2pDot" class="status-dot"></div>
      <span id="p2pStatusLabel">Conectando ao celular...</span>
    </div>

    <nav>
      <a class="nav-item active" data-view="dashboard" onclick="switchView('dashboard')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>
        <span>Visão Geral</span>
      </a>
      <a class="nav-item" data-view="transactions" onclick="switchView('transactions')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        <span>Lançamentos</span>
      </a>
      <a class="nav-item" data-view="cards" onclick="switchView('cards')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/></svg>
        <span>Cartões de Crédito</span>
      </a>
      <a class="nav-item" data-view="categories" onclick="switchView('categories')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M4 9h16M4 15h16M10 3L8 21M16 3l-2 18"/></svg>
        <span>Categorias & Metas</span>
      </a>
      <a class="nav-item" data-view="sync" onclick="switchView('sync')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/></svg>
        <span>Sincronização P2P</span>
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="shortcuts-hint">
        Atalhos: <span class="kbd">N</span> Novo &bull; <span class="kbd">/</span> Busca &bull; <span class="kbd">Esc</span> Fechar
      </div>
      <button class="btn btn-secondary" onclick="toggleTheme()" style="width: 100%;">
        <span id="themeBtnText">🌓 Mudar Tema</span>
      </button>
    </div>
  </aside>

  <!-- MAIN -->
  <main>
    <header>
      <div class="header-left">
        <div id="pageTitle" class="page-title">Visão Geral</div>
        <div class="month-nav">
          <button onclick="changeMonth(-1)">◀</button>
          <span id="currentMonthLabel">Setembro 2026</span>
          <button onclick="changeMonth(1)">▶</button>
          <button onclick="goToCurrentMonth()" style="font-size: 11px; padding: 2px 6px;">Hoje</button>
        </div>
      </div>

      <div class="header-right">
        <button class="btn btn-secondary btn-icon" title="Atualizar dados agora" onclick="loadData(true)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/></svg>
        </button>
        <button class="btn btn-primary" onclick="openTransactionModal()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          <span>Novo Lançamento</span>
        </button>
      </div>
    </header>

    <div class="content-area">
      <!-- VIEW: DASHBOARD -->
      <section id="view-dashboard">
        <div class="metrics-grid">
          <div class="metric-card balance">
            <span class="metric-label">Saldo Acumulado</span>
            <span id="metricBalance" class="metric-value">R$ 0,00</span>
            <span id="metricBalanceSub" class="metric-sub">Balanço geral</span>
          </div>
          <div class="metric-card">
            <span class="metric-label">Receitas do Mês</span>
            <span id="metricIncome" class="metric-value income">R$ 0,00</span>
            <span id="metricIncomeCount" class="metric-sub">0 lançamentos</span>
          </div>
          <div class="metric-card expense">
            <span class="metric-label">Despesas do Mês</span>
            <span id="metricExpense" class="metric-value expense">R$ 0,00</span>
            <span id="metricExpenseCount" class="metric-sub">0 lançamentos</span>
          </div>
          <div class="metric-card">
            <span class="metric-label">Economia no Mês</span>
            <span id="metricSavings" class="metric-value">R$ 0,00</span>
            <span id="metricSavingsRate" class="metric-sub">Taxa: 0%</span>
          </div>
        </div>

        <div id="budgetBanner" class="budget-banner" style="display: none;">
          <div class="budget-header">
            <span>Meta de Orçamento Mensal</span>
            <span id="budgetProgressText">R$ 0 / R$ 0</span>
          </div>
          <div class="progress-bar-bg">
            <div id="budgetProgressBar" class="progress-bar-fill" style="width: 0%;"></div>
          </div>
        </div>

        <div class="charts-row">
          <div class="chart-card">
            <div class="chart-title">Evolução Mensal (Receitas vs Despesas)</div>
            <div id="chartEvolution" style="height: 220px; display: flex; align-items: flex-end; justify-content: space-around; padding-top: 20px;">
              <!-- Dynamic Bars rendered by JS -->
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-title">Gastos por Categoria</div>
            <div id="catDistributionList" class="cat-bars">
              <!-- Category bars rendered by JS -->
            </div>
          </div>
        </div>

        <div class="table-container">
          <div class="table-toolbar">
            <div style="font-weight: 700; font-size: 15px;">Últimos Lançamentos</div>
            <button class="btn btn-secondary" onclick="switchView('transactions')">Ver Todos</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Tipo</th>
                <th>Descrição</th>
                <th>Categoria</th>
                <th>Valor</th>
                <th style="text-align: right;">Ações</th>
              </tr>
            </thead>
            <tbody id="recentTransactionsTableBody">
              <!-- Rows rendered by JS -->
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW: TRANSACTIONS -->
      <section id="view-transactions" style="display: none;">
        <div class="table-container">
          <div class="table-toolbar">
            <div class="search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
              <input id="searchInput" type="text" placeholder="Buscar lançamento... (/)" oninput="filterTransactions()">
            </div>

            <div class="filter-group">
              <select id="filterType" class="filter-select" onchange="filterTransactions()">
                <option value="ALL">Todos os Tipos</option>
                <option value="EXPENSE">Apenas Despesas</option>
                <option value="INCOME">Apenas Receitas</option>
              </select>

              <select id="filterCategory" class="filter-select" onchange="filterTransactions()">
                <option value="ALL">Todas as Categorias</option>
              </select>

              <select id="filterCard" class="filter-select" onchange="filterTransactions()">
                <option value="ALL">Todos os Cartões/Contas</option>
              </select>

              <button class="btn btn-secondary" onclick="exportCSV()" title="Exportar para Excel / Sheets">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                <span>Exportar CSV</span>
              </button>
            </div>
          </div>

          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Tipo</th>
                <th>Título / Descrição</th>
                <th>Categoria</th>
                <th>Cartão / Conta</th>
                <th>Parcelas</th>
                <th>Valor</th>
                <th style="text-align: right;">Ações</th>
              </tr>
            </thead>
            <tbody id="allTransactionsTableBody">
              <!-- Full table rows -->
            </tbody>
          </table>

          <div style="padding: 12px 18px; background: var(--bg-card); border-top: 1px solid var(--border-color); display: flex; justify-content: space-between; font-size: 13px; font-weight: 600;">
            <span id="tableSummaryCount">0 lançamentos listados</span>
            <span id="tableSummaryAmount">Total: R$ 0,00</span>
          </div>
        </div>
      </section>

      <!-- VIEW: CARDS -->
      <section id="view-cards" style="display: none;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 18px; font-weight: 700;">Cartões de Crédito Cadastrados</h2>
            <p style="color: var(--text-dim); font-size: 13px;">Acompanhe faturas, limites e vencimentos dos seus cartões</p>
          </div>
          <button class="btn btn-primary" onclick="openCardModal()">+ Adicionar Cartão</button>
        </div>

        <div id="cardsGridContainer" class="cards-grid">
          <!-- Cards rendered by JS -->
        </div>
      </section>

      <!-- VIEW: CATEGORIES -->
      <section id="view-categories" style="display: none;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 18px; font-weight: 700;">Categorias Personalizadas</h2>
            <p style="color: var(--text-dim); font-size: 13px;">Organize suas finanças com categorias próprias</p>
          </div>
          <button class="btn btn-primary" onclick="openCategoryModal()">+ Nova Categoria</button>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Ícone / Cor</th>
                <th>Nome</th>
                <th>Tipo</th>
                <th style="text-align: right;">Origem</th>
              </tr>
            </thead>
            <tbody id="categoriesTableBody">
              <!-- Categories list -->
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW: P2P SYNC -->
      <section id="view-sync" style="display: none;">
        <div class="chart-card" style="max-width: 680px; margin: 0 auto;">
          <h2 style="font-size: 18px; font-weight: 700;">Status da Conexão P2P Celular ↔ PC</h2>
          <p style="color: var(--text-muted); font-size: 13px;">
            Este painel web roda localmente dentro do seu aplicativo Android no celular. Não existem servidores intermediários na nuvem; toda comunicação é ponto a ponto na sua rede Wi-Fi.
          </p>

          <div style="background: var(--bg-card); padding: 16px; border-radius: var(--radius-sm); border: 1px solid var(--border-color); display: flex; flex-direction: column; gap: 8px;">
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-muted);">Status do Servidor:</span>
              <strong style="color: var(--primary);">🟢 ONLINE (Ativo)</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-muted);">Aparelho Celular:</span>
              <strong id="syncDeviceName">Android Device</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-muted);">Endereço Local:</span>
              <span id="syncLocalUrl" style="font-family: monospace;">-</span>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-muted);">Sincronização em Tempo Real (SSE):</span>
              <strong id="syncSseStatus" style="color: var(--primary);">Conectado</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-muted);">Última Transmissão:</span>
              <span id="syncLastTime">-</span>
            </div>
          </div>

          <div style="display: flex; gap: 10px;">
            <button class="btn btn-primary" onclick="loadData(true)">Forçar Sincronização Agora</button>
            <button class="btn btn-secondary" onclick="exportJSON()">Baixar Backup Completo (JSON)</button>
            <button class="btn btn-secondary" onclick="logout()">Desconectar Navegador</button>
          </div>
        </div>
      </section>
    </div>
  </main>

  <!-- MODAL: TRANSACTION -->
  <div id="transactionModal" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <div id="txModalTitle" class="modal-title">Novo Lançamento</div>
        <button class="action-btn" onclick="closeTransactionModal()">✕</button>
      </div>

      <div class="type-toggle">
        <button id="typeExpenseBtn" class="type-btn active expense" onclick="setTxType('EXPENSE')">Despesa</button>
        <button id="typeIncomeBtn" class="type-btn income" onclick="setTxType('INCOME')">Receita</button>
      </div>

      <div class="form-group">
        <label class="form-label">Descrição / Título</label>
        <input id="txTitle" class="form-input" type="text" placeholder="Ex: Supermercado, Salário, Uber..." required>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
        <div class="form-group">
          <label class="form-label">Valor (R$)</label>
          <input id="txAmount" class="form-input" type="number" step="0.01" placeholder="0,00" required>
        </div>

        <div class="form-group">
          <label class="form-label">Data</label>
          <input id="txDate" class="form-input" type="date">
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
        <div class="form-group">
          <label class="form-label">Categoria</label>
          <select id="txCategory" class="form-select"></select>
        </div>

        <div id="txCardGroup" class="form-group">
          <label class="form-label">Cartão de Crédito</label>
          <select id="txCard" class="form-select">
            <option value="">Nenhum (Conta corrente/dinheiro)</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Anotações (opcional)</label>
        <input id="txNote" class="form-input" type="text" placeholder="Detalhes adicionais...">
      </div>

      <!-- Installment / Recurring options -->
      <div id="txAdvancedSection" style="background: var(--bg-card); padding: 10px; border-radius: var(--radius-sm); border: 1px solid var(--border-color); display: flex; flex-direction: column; gap: 8px;">
        <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer;">
          <input id="txIsInstallment" type="checkbox" onchange="toggleInstallmentInputs()">
          <span>Parcelar este gasto</span>
        </label>
        <div id="txInstallmentInputs" style="display: none; align-items: center; gap: 8px; margin-top: 4px;">
          <span style="font-size: 12px; color: var(--text-muted);">Total de Parcelas:</span>
          <select id="txTotalInstallments" class="form-select" style="width: 100px;">
            <option value="2">2x</option>
            <option value="3">3x</option>
            <option value="4">4x</option>
            <option value="5">5x</option>
            <option value="6">6x</option>
            <option value="10">10x</option>
            <option value="12">12x</option>
            <option value="18">18x</option>
            <option value="24">24x</option>
          </select>
        </div>

        <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer;">
          <input id="txIsRecurring" type="checkbox" onchange="toggleRecurringInputs()">
          <span>Tornar lançamento recorrente (mensal)</span>
        </label>
        <div id="txRecurringInputs" style="display: none; align-items: center; gap: 8px; margin-top: 4px;">
          <span style="font-size: 12px; color: var(--text-muted);">Repetir por:</span>
          <select id="txRecurringMonths" class="form-select" style="width: 120px;">
            <option value="3">3 meses</option>
            <option value="6">6 meses</option>
            <option value="12">12 meses</option>
            <option value="24">24 meses</option>
          </select>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeTransactionModal()">Cancelar</button>
        <button class="btn btn-primary" onclick="saveTransaction()">Salvar Lançamento</button>
      </div>
    </div>
  </div>

  <!-- MODAL: CARD -->
  <div id="cardModal" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <div id="cardModalTitle" class="modal-title">Novo Cartão de Crédito</div>
        <button class="action-btn" onclick="closeCardModal()">✕</button>
      </div>

      <div class="form-group">
        <label class="form-label">Nome do Cartão</label>
        <input id="cardName" class="form-input" type="text" placeholder="Ex: Nubank Roxo, Inter Black..." required>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
        <div class="form-group">
          <label class="form-label">Últimos 4 Dígitos</label>
          <input id="cardLastDigits" class="form-input" type="text" maxlength="4" placeholder="1234">
        </div>
        <div class="form-group">
          <label class="form-label">Cor do Cartão</label>
          <input id="cardColor" class="form-input" type="color" value="#8A05BE" style="height: 38px; padding: 2px;">
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Limite Total (R$)</label>
        <input id="cardLimit" class="form-input" type="number" step="0.01" placeholder="5000,00">
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
        <div class="form-group">
          <label class="form-label">Dia do Fechamento</label>
          <input id="cardClosingDay" class="form-input" type="number" min="1" max="31" value="5">
        </div>
        <div class="form-group">
          <label class="form-label">Dia do Vencimento</label>
          <input id="cardDueDay" class="form-input" type="number" min="1" max="31" value="12">
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeCardModal()">Cancelar</button>
        <button class="btn btn-primary" onclick="saveCard()">Salvar Cartão</button>
      </div>
    </div>
  </div>

  <!-- MODAL: PIN AUTH -->
  <div id="authModal" class="modal-overlay">
    <div class="modal" style="max-width: 400px; text-align: center;">
      <div style="font-size: 32px; margin-bottom: 8px;">🔐</div>
      <h3 style="font-size: 18px; font-weight: 700;">Acesso Protegido</h3>
      <p style="color: var(--text-muted); font-size: 13px;">
        Digite o <strong>código PIN de 6 dígitos</strong> exibido na tela do app no celular:
      </p>

      <div style="margin: 16px 0;">
        <input id="pinInput" type="password" maxlength="6" inputmode="numeric" placeholder="••••••" style="font-size: 28px; letter-spacing: 8px; text-align: center; width: 180px; padding: 8px; border-radius: var(--radius-sm); border: 2px solid var(--border-color); background: var(--bg-card); color: var(--text-main); outline: none;">
      </div>

      <div id="authErrorMsg" style="color: var(--expense); font-size: 12px; display: none;">PIN incorreto. Verifique no app.</div>

      <div style="display: flex; gap: 8px; justify-content: center; margin-top: 12px;">
        <button class="btn btn-primary" onclick="submitPin()" style="width: 100%;">Conectar ao Celular</button>
      </div>
    </div>
  </div>

  <!-- TOAST CONTAINER -->
  <div id="toastContainer" class="toast-container"></div>

  <script>
    // GLOBAL STATE
    let allData = { transactions: [], cards: [], categories: [], preferences: {} };
    let filteredTransactions = [];
    let currentYear = new Date().getFullYear();
    let currentMonth = new Date().getMonth(); // 0-11
    let activeTxId = null;
    let activeCardId = null;
    let sseSource = null;
    let sessionToken = localStorage.getItem("bumoney_pc_token") || "";

    const MONTH_NAMES = [
      "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
      "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    ];

    // INIT
    document.addEventListener("DOMContentLoaded", () => {
      // Check query param for PIN
      const urlParams = new URLSearchParams(window.location.search);
      const queryPin = urlParams.get("pin");
      if (queryPin) {
        document.getElementById("pinInput").value = queryPin;
        submitPin(queryPin);
      } else if (sessionToken) {
        loadData();
      } else {
        openAuthModal();
      }

      updateMonthLabel();
      initKeyboardShortcuts();
      document.getElementById("txDate").value = new Date().toISOString().split("T")[0];
    });

    // NAVIGATION
    function switchView(viewName) {
      document.querySelectorAll("aside nav .nav-item").forEach(el => {
        el.classList.toggle("active", el.getAttribute("data-view") === viewName);
      });

      document.querySelectorAll("main .content-area section").forEach(el => {
        el.style.display = el.id === 'view-' + viewName ? 'block' : 'none';
      });

      const titles = {
        dashboard: "Visão Geral",
        transactions: "Lançamentos e Movimentações",
        cards: "Cartões de Crédito",
        categories: "Categorias & Metas",
        sync: "Sincronização P2P Local"
      };
      document.getElementById("pageTitle").textContent = titles[viewName] || "BUMoney";
    }

    function updateMonthLabel() {
      document.getElementById("currentMonthLabel").textContent = `${'$'}{MONTH_NAMES[currentMonth]} ${'$'}{currentYear}`;
    }

    function changeMonth(delta) {
      currentMonth += delta;
      if (currentMonth > 11) { currentMonth = 0; currentYear++; }
      else if (currentMonth < 0) { currentMonth = 11; currentYear--; }
      updateMonthLabel();
      renderDashboard();
      filterTransactions();
    }

    function goToCurrentMonth() {
      currentYear = new Date().getFullYear();
      currentMonth = new Date().getMonth();
      updateMonthLabel();
      renderDashboard();
      filterTransactions();
    }

    // AUTHENTICATION
    function openAuthModal() {
      document.getElementById("authModal").classList.add("active");
      setTimeout(() => document.getElementById("pinInput").focus(), 100);
    }

    async function submitPin(directPin) {
      const pin = directPin || document.getElementById("pinInput").value.trim();
      const errEl = document.getElementById("authErrorMsg");
      errEl.style.display = "none";

      try {
        const resp = await fetch("/api/auth", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pin })
        });
        const res = await resp.json();
        if (res.success && res.token) {
          sessionToken = res.token;
          localStorage.setItem("bumoney_pc_token", sessionToken);
          document.getElementById("authModal").classList.remove("active");
          showToast("Autenticado com sucesso via P2P!");
          loadData();
        } else {
          errEl.style.display = "block";
          errEl.textContent = res.error || "PIN incorreto.";
        }
      } catch (e) {
        errEl.style.display = "block";
        errEl.textContent = "Falha de conexão com o celular.";
      }
    }

    function logout() {
      sessionToken = "";
      localStorage.removeItem("bumoney_pc_token");
      if (sseSource) sseSource.close();
      openAuthModal();
    }

    // DATA LOADING
    async function loadData(showSyncToast = false) {
      if (!sessionToken) return;

      try {
        const resp = await fetch(`/api/data?token=${'$'}{sessionToken}`);
        if (resp.status === 401) {
          logout();
          return;
        }
        allData = await resp.json();

        // Update P2P Status Badge
        document.getElementById("p2pDot").classList.add("active");
        document.getElementById("p2pStatusLabel").textContent = "Conectado via Wi-Fi";
        document.getElementById("syncDeviceName").textContent = allData.deviceName || "Android Phone";
        document.getElementById("syncLocalUrl").textContent = window.location.origin;
        document.getElementById("syncLastTime").textContent = new Date().toLocaleTimeString();

        renderDashboard();
        populateFilters();
        filterTransactions();
        renderCards();
        renderCategories();

        startSSE();

        if (showSyncToast) showToast("Dados sincronizados em tempo real!");
      } catch (e) {
        document.getElementById("p2pDot").classList.remove("active");
        document.getElementById("p2pStatusLabel").textContent = "Desconectado do celular";
      }
    }

    // SERVER-SENT EVENTS (SSE) FOR REAL-TIME P2P SYNC
    function startSSE() {
      if (sseSource) sseSource.close();

      sseSource = new EventSource(`/api/events?token=${'$'}{sessionToken}`);
      sseSource.onopen = () => {
        document.getElementById("syncSseStatus").textContent = "Ativo e Monitorando";
        document.getElementById("syncSseStatus").style.color = "var(--primary)";
      };
      sseSource.addEventListener("update", (e) => {
        showToast("Atualização recebida do celular!");
        loadData(false);
      });
      sseSource.onerror = () => {
        document.getElementById("syncSseStatus").textContent = "Reconectando...";
        document.getElementById("syncSseStatus").style.color = "var(--warning)";
      };
    }

    // DASHBOARD RENDERING
    function renderDashboard() {
      const txs = allData.transactions || [];
      const monthTxs = txs.filter(t => {
        const d = new Date(t.timestamp);
        return d.getFullYear() === currentYear && d.getMonth() === currentMonth;
      });

      let income = 0;
      let expense = 0;
      let incomeCount = 0;
      let expenseCount = 0;
      const catTotals = {};

      monthTxs.forEach(t => {
        if (t.type === "INCOME") {
          income += t.amount;
          incomeCount++;
        } else {
          expense += t.amount;
          expenseCount++;
          catTotals[t.category] = (catTotals[t.category] || 0) + t.amount;
        }
      });

      // Overall balance
      let totalBalance = 0;
      txs.forEach(t => {
        if (t.type === "INCOME") totalBalance += t.amount;
        else totalBalance -= t.amount;
      });

      const savings = income - expense;
      const savingsRate = income > 0 ? Math.round((savings / income) * 100) : 0;

      document.getElementById("metricBalance").textContent = formatBRL(totalBalance);
      document.getElementById("metricIncome").textContent = formatBRL(income);
      document.getElementById("metricIncomeCount").textContent = `${'$'}{incomeCount} receitas`;
      document.getElementById("metricExpense").textContent = formatBRL(expense);
      document.getElementById("metricExpenseCount").textContent = `${'$'}{expenseCount} despesas`;
      document.getElementById("metricSavings").textContent = formatBRL(savings);
      document.getElementById("metricSavings").style.color = savings >= 0 ? "var(--income)" : "var(--expense)";
      document.getElementById("metricSavingsRate").textContent = `Taxa: ${'$'}{savingsRate}% economizado`;

      // Monthly Budget Progress
      const monthlyBudget = allData.preferences?.monthlyBudget || 0;
      const budgetBanner = document.getElementById("budgetBanner");
      if (monthlyBudget > 0) {
        budgetBanner.style.display = "flex";
        const pct = Math.min(Math.round((expense / monthlyBudget) * 100), 100);
        document.getElementById("budgetProgressText").textContent = `${'$'}{formatBRL(expense)} de ${'$'}{formatBRL(monthlyBudget)} (${'$'}{pct}%)`;
        const bar = document.getElementById("budgetProgressBar");
        bar.style.width = pct + "%";
        bar.style.background = pct >= 90 ? "var(--expense)" : (pct >= 70 ? "var(--warning)" : "var(--primary)");
      } else {
        budgetBanner.style.display = "none";
      }

      // Category Distribution Bars
      const catList = document.getElementById("catDistributionList");
      catList.innerHTML = "";
      const sortedCats = Object.entries(catTotals).sort((a,b) => b[1] - a[1]).slice(0, 5);
      if (sortedCats.length === 0) {
        catList.innerHTML = "<div style='color: var(--text-dim); font-size: 13px;'>Nenhuma despesa neste mês.</div>";
      } else {
        sortedCats.forEach(([cat, val]) => {
          const pct = Math.round((val / expense) * 100);
          catList.innerHTML += `
            <div class="cat-bar-item">
              <div class="cat-bar-header">
                <span>${'$'}{cat}</span>
                <span>${'$'}{formatBRL(val)} (${'$'}{pct}%)</span>
              </div>
              <div class="cat-progress">
                <div class="cat-progress-fill" style="width: ${'$'}{pct}%; background: var(--primary);"></div>
              </div>
            </div>
          `;
        });
      }

      // Monthly Evolution Chart (Last 6 Months)
      renderEvolutionChart();

      // Recent Transactions (Last 8)
      const recentTbody = document.getElementById("recentTransactionsTableBody");
      recentTbody.innerHTML = "";
      const recents = [...txs].sort((a,b) => b.timestamp - a.timestamp).slice(0, 8);
      if (recents.length === 0) {
        recentTbody.innerHTML = "<tr><td colspan='6' style='text-align: center; color: var(--text-dim);'>Nenhum lançamento cadastrado.</td></tr>";
      } else {
        recents.forEach(t => {
          recentTbody.innerHTML += createTableRowHtml(t);
        });
      }
    }

    function renderEvolutionChart() {
      const container = document.getElementById("chartEvolution");
      container.innerHTML = "";
      const txs = allData.transactions || [];

      // Calculate past 5 months + current
      const months = [];
      for (let i = 4; i >= 0; i--) {
        const d = new Date(currentYear, currentMonth - i, 1);
        months.push({ year: d.getFullYear(), month: d.getMonth(), name: MONTH_NAMES[d.getMonth()].substring(0, 3) });
      }

      let maxVal = 1000;
      const data = months.map(m => {
        let inc = 0, exp = 0;
        txs.forEach(t => {
          const td = new Date(t.timestamp);
          if (td.getFullYear() === m.year && td.getMonth() === m.month) {
            if (t.type === "INCOME") inc += t.amount;
            else exp += t.amount;
          }
        });
        if (inc > maxVal) maxVal = inc;
        if (exp > maxVal) maxVal = exp;
        return { name: m.name, inc, exp };
      });

      data.forEach(d => {
        const incHeight = Math.round((d.inc / maxVal) * 160);
        const expHeight = Math.round((d.exp / maxVal) * 160);
        container.innerHTML += `
          <div style="display: flex; flex-direction: column; align-items: center; gap: 8px;">
            <div style="display: flex; align-items: flex-end; gap: 6px; height: 160px;">
              <div title="Receitas: ${'$'}{formatBRL(d.inc)}" style="width: 20px; height: ${'$'}{Math.max(incHeight, 4)}px; background: var(--income); border-radius: 4px 4px 0 0;"></div>
              <div title="Despesas: ${'$'}{formatBRL(d.exp)}" style="width: 20px; height: ${'$'}{Math.max(expHeight, 4)}px; background: var(--expense); border-radius: 4px 4px 0 0;"></div>
            </div>
            <span style="font-size: 11px; color: var(--text-dim);">${'$'}{d.name}</span>
          </div>
        `;
      });
    }

    // TRANSACTIONS TABLE & FILTERS
    function populateFilters() {
      const catSelect = document.getElementById("filterCategory");
      catSelect.innerHTML = "<option value='ALL'>Todas as Categorias</option>";
      const modalCatSelect = document.getElementById("txCategory");
      modalCatSelect.innerHTML = "";

      const cats = (allData.categories || []).map(c => c.name || c);
      cats.forEach(c => {
        catSelect.innerHTML += `<option value="${'$'}{c}">${'$'}{c}</option>`;
        modalCatSelect.innerHTML += `<option value="${'$'}{c}">${'$'}{c}</option>`;
      });

      const cardSelect = document.getElementById("filterCard");
      cardSelect.innerHTML = "<option value='ALL'>Todos os Cartões/Contas</option>";
      const modalCardSelect = document.getElementById("txCard");
      modalCardSelect.innerHTML = "<option value=''>Nenhum (Conta Corrente / Dinheiro)</option>";

      (allData.cards || []).forEach(card => {
        cardSelect.innerHTML += `<option value="${'$'}{card.id}">${'$'}{card.name}</option>`;
        modalCardSelect.innerHTML += `<option value="${'$'}{card.id}">${'$'}{card.name}</option>`;
      });
    }

    function filterTransactions() {
      const q = document.getElementById("searchInput").value.toLowerCase();
      const type = document.getElementById("filterType").value;
      const cat = document.getElementById("filterCategory").value;
      const card = document.getElementById("filterCard").value;

      const txs = allData.transactions || [];
      filteredTransactions = txs.filter(t => {
        const td = new Date(t.timestamp);
        const matchMonth = td.getFullYear() === currentYear && td.getMonth() === currentMonth;
        if (!matchMonth) return false;

        if (type !== "ALL" && t.type !== type) return false;
        if (cat !== "ALL" && t.category !== cat) return false;
        if (card !== "ALL" && String(t.cardId || "") !== card) return false;
        if (q && !t.title.toLowerCase().includes(q) && !(t.note || "").toLowerCase().includes(q)) return false;

        return true;
      });

      // Render table
      const tbody = document.getElementById("allTransactionsTableBody");
      tbody.innerHTML = "";
      if (filteredTransactions.length === 0) {
        tbody.innerHTML = "<tr><td colspan='8' style='text-align: center; color: var(--text-dim); padding: 30px;'>Nenhum lançamento encontrado para os filtros selecionados.</td></tr>";
      } else {
        filteredTransactions.forEach(t => {
          tbody.innerHTML += createTableRowHtml(t);
        });
      }

      // Summary
      let sum = 0;
      filteredTransactions.forEach(t => {
        if (t.type === "INCOME") sum += t.amount;
        else sum -= t.amount;
      });
      document.getElementById("tableSummaryCount").textContent = `${'$'}{filteredTransactions.length} lançamentos listados`;
      document.getElementById("tableSummaryAmount").textContent = `Balanço Filtrado: ${'$'}{formatBRL(sum)}`;
    }

    function createTableRowHtml(t) {
      const d = new Date(t.timestamp);
      const dateStr = d.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric" });
      const isIncome = t.type === "INCOME";
      const card = (allData.cards || []).find(c => c.id === t.cardId);
      const cardName = card ? card.name : (t.type === "EXPENSE" ? "Conta Corrente" : "Entrada");

      let parcelInfo = "-";
      if (t.isInstallment) {
        parcelInfo = `<span class="badge badge-tag">${'$'}{t.installmentNumber}/${'$'}{t.totalInstallments}x</span>`;
      } else if (t.isRecurring) {
        parcelInfo = `<span class="badge badge-tag">Recorrente</span>`;
      }

      return `
        <tr>
          <td style="color: var(--text-muted); font-size: 12px;">${'$'}{dateStr}</td>
          <td><span class="badge ${'$'}{isIncome ? 'badge-income' : 'badge-expense'}">${'$'}{isIncome ? 'Receita' : 'Despesa'}</span></td>
          <td>
            <div style="font-weight: 600;">${'$'}{escapeHtml(t.title)}</div>
            ${'$'}{t.note ? `<div style="font-size: 11px; color: var(--text-dim);">${'$'}{escapeHtml(t.note)}</div>` : ''}
          </td>
          <td><span class="badge badge-tag">${'$'}{escapeHtml(t.category)}</span></td>
          <td style="color: var(--text-muted);">${'$'}{escapeHtml(cardName)}</td>
          <td>${'$'}{parcelInfo}</td>
          <td style="font-weight: 700; color: ${'$'}{isIncome ? 'var(--income)' : 'var(--expense)'};">
            ${'$'}{isIncome ? '+' : '-'} ${'$'}{formatBRL(t.amount)}
          </td>
          <td style="text-align: right;">
            <div class="actions-cell" style="justify-content: flex-end;">
              <button class="action-btn" title="Editar Lançamento" onclick="openTransactionModal(${'$'}{t.id})">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
              </button>
              <button class="action-btn delete" title="Excluir" onclick="deleteTransaction(${'$'}{t.id})">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
              </button>
            </div>
          </td>
        </tr>
      `;
    }

    // CARDS RENDERING
    function renderCards() {
      const container = document.getElementById("cardsGridContainer");
      container.innerHTML = "";
      const cards = allData.cards || [];

      if (cards.length === 0) {
        container.innerHTML = "<div style='grid-column: 1/-1; text-align: center; color: var(--text-dim); padding: 40px;'>Nenhum cartão cadastrado. Clique em '+ Adicionar Cartão' para começar.</div>";
        return;
      }

      cards.forEach(c => {
        container.innerHTML += `
          <div class="credit-card-item" style="background: linear-gradient(135deg, ${'$'}{c.colorHex || '#4F46E5'}, #0F172A);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: 700; font-size: 16px;">${'$'}{escapeHtml(c.name)}</span>
              <div class="credit-card-chip"></div>
            </div>

            <div style="font-size: 18px; letter-spacing: 2px; font-family: monospace;">
              •••• •••• •••• ${'$'}{c.lastFourDigits || '••••'}
            </div>

            <div style="display: flex; justify-content: space-between; font-size: 12px; opacity: 0.9;">
              <div>
                <span>Limite: </span><strong>${'$'}{formatBRL(c.limitAmount || 0)}</strong>
              </div>
              <div>
                <span>Fecha: dia <strong>${'$'}{c.closingDay}</strong> &bull; Vence: dia <strong>${'$'}{c.dueDay}</strong></span>
              </div>
            </div>
          </div>
        `;
      });
    }

    // CATEGORIES RENDERING
    function renderCategories() {
      const tbody = document.getElementById("categoriesTableBody");
      tbody.innerHTML = "";
      const cats = allData.categories || [];

      cats.forEach(c => {
        tbody.innerHTML += `
          <tr>
            <td>
              <div style="display: flex; align-items: center; gap: 8px;">
                <div style="width: 14px; height: 14px; border-radius: 50%; background: ${'$'}{c.colorHex || '#10B981'};"></div>
                <span>${'$'}{c.iconName || '🏷️'}</span>
              </div>
            </td>
            <td style="font-weight: 600;">${'$'}{escapeHtml(c.name)}</td>
            <td><span class="badge ${'$'}{c.type === 'INCOME' ? 'badge-income' : 'badge-expense'}">${'$'}{c.type === 'INCOME' ? 'Receita' : 'Despesa'}</span></td>
            <td style="text-align: right; color: var(--text-dim); font-size: 12px;">${'$'}{c.isDefault ? 'Padrão do Sistema' : 'Personalizada'}</td>
          </tr>
        `;
      });
    }

    // TRANSACTION MODAL & ACTIONS
    let selectedTxType = "EXPENSE";
    function setTxType(type) {
      selectedTxType = type;
      document.getElementById("typeExpenseBtn").classList.toggle("active", type === "EXPENSE");
      document.getElementById("typeIncomeBtn").classList.toggle("active", type === "INCOME");
      document.getElementById("txCardGroup").style.display = type === "EXPENSE" ? "flex" : "none";
      document.getElementById("txAdvancedSection").style.display = type === "EXPENSE" ? "flex" : "none";
    }

    function toggleInstallmentInputs() {
      const checked = document.getElementById("txIsInstallment").checked;
      document.getElementById("txInstallmentInputs").style.display = checked ? "flex" : "none";
      if (checked) {
        document.getElementById("txIsRecurring").checked = false;
        document.getElementById("txRecurringInputs").style.display = "none";
      }
    }

    function toggleRecurringInputs() {
      const checked = document.getElementById("txIsRecurring").checked;
      document.getElementById("txRecurringInputs").style.display = checked ? "flex" : "none";
      if (checked) {
        document.getElementById("txIsInstallment").checked = false;
        document.getElementById("txInstallmentInputs").style.display = "none";
      }
    }

    function openTransactionModal(editId = null) {
      activeTxId = editId;
      const modal = document.getElementById("transactionModal");
      modal.classList.add("active");

      if (editId) {
        const tx = allData.transactions.find(t => t.id === editId);
        if (tx) {
          document.getElementById("txModalTitle").textContent = "Editar Lançamento";
          setTxType(tx.type);
          document.getElementById("txTitle").value = tx.title;
          document.getElementById("txAmount").value = tx.amount;
          document.getElementById("txDate").value = new Date(tx.timestamp).toISOString().split("T")[0];
          document.getElementById("txCategory").value = tx.category;
          document.getElementById("txCard").value = tx.cardId || "";
          document.getElementById("txNote").value = tx.note || "";
          document.getElementById("txIsInstallment").checked = !!tx.isInstallment;
          document.getElementById("txIsRecurring").checked = !!tx.isRecurring;
          toggleInstallmentInputs();
          toggleRecurringInputs();
        }
      } else {
        document.getElementById("txModalTitle").textContent = "Novo Lançamento";
        setTxType("EXPENSE");
        document.getElementById("txTitle").value = "";
        document.getElementById("txAmount").value = "";
        document.getElementById("txDate").value = new Date().toISOString().split("T")[0];
        document.getElementById("txNote").value = "";
        document.getElementById("txIsInstallment").checked = false;
        document.getElementById("txIsRecurring").checked = false;
        toggleInstallmentInputs();
        toggleRecurringInputs();
      }
      setTimeout(() => document.getElementById("txTitle").focus(), 100);
    }

    function closeTransactionModal() {
      document.getElementById("transactionModal").classList.remove("active");
    }

    async function saveTransaction() {
      const title = document.getElementById("txTitle").value.trim();
      const amount = parseFloat(document.getElementById("txAmount").value);
      const dateVal = document.getElementById("txDate").value;
      const category = document.getElementById("txCategory").value;
      const cardId = document.getElementById("txCard").value ? parseInt(document.getElementById("txCard").value) : null;
      const note = document.getElementById("txNote").value.trim();
      const isInstallment = document.getElementById("txIsInstallment").checked;
      const totalInstallments = isInstallment ? parseInt(document.getElementById("txTotalInstallments").value) : 1;
      const isRecurring = document.getElementById("txIsRecurring").checked;
      const recurringMonths = isRecurring ? parseInt(document.getElementById("txRecurringMonths").value) : 1;

      if (!title || isNaN(amount) || amount <= 0) {
        alert("Preencha o título e um valor válido.");
        return;
      }

      const timestamp = dateVal ? new Date(dateVal + "T12:00:00").getTime() : Date.now();

      const payload = {
        title,
        amount,
        type: selectedTxType,
        category,
        cardId,
        note,
        timestamp,
        isInstallment,
        totalInstallments,
        isRecurring,
        recurringMonths
      };

      try {
        const url = activeTxId ? `/api/transactions/${'$'}{activeTxId}?token=${'$'}{sessionToken}` : `/api/transactions?token=${'$'}{sessionToken}`;
        const method = activeTxId ? "PUT" : "POST";
        const resp = await fetch(url, {
          method,
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const res = await resp.json();
        if (res.success) {
          closeTransactionModal();
          showToast(activeTxId ? "Lançamento atualizado no celular!" : "Lançamento salvo no celular!");
          loadData(false);
        } else {
          alert("Erro: " + (res.error || "Falha ao salvar"));
        }
      } catch (e) {
        alert("Erro de comunicação P2P com o celular.");
      }
    }

    async function deleteTransaction(id) {
      if (!confirm("Tem certeza que deseja excluir este lançamento?")) return;
      try {
        const resp = await fetch(`/api/transactions/${'$'}{id}?token=${'$'}{sessionToken}`, { method: "DELETE" });
        const res = await resp.json();
        if (res.success) {
          showToast("Lançamento excluído!");
          loadData(false);
        }
      } catch (e) {
        alert("Erro ao excluir.");
      }
    }

    // CARD MODAL & ACTIONS
    function openCardModal() {
      document.getElementById("cardModal").classList.add("active");
    }
    function closeCardModal() {
      document.getElementById("cardModal").classList.remove("active");
    }
    async function saveCard() {
      const name = document.getElementById("cardName").value.trim();
      const lastDigits = document.getElementById("cardLastDigits").value.trim();
      const colorHex = document.getElementById("cardColor").value;
      const limitAmount = parseFloat(document.getElementById("cardLimit").value) || 0;
      const closingDay = parseInt(document.getElementById("cardClosingDay").value) || 5;
      const dueDay = parseInt(document.getElementById("cardDueDay").value) || 12;

      if (!name) { alert("Digite o nome do cartão."); return; }

      try {
        const resp = await fetch(`/api/cards?token=${'$'}{sessionToken}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, lastFourDigits: lastDigits, colorHex, limitAmount, closingDay, dueDay })
        });
        const res = await resp.json();
        if (res.success) {
          closeCardModal();
          showToast("Cartão adicionado com sucesso!");
          loadData(false);
        }
      } catch (e) {
        alert("Erro ao salvar cartão.");
      }
    }

    // EXPORTS
    function exportCSV() {
      window.location.href = `/api/export/csv?token=${'$'}{sessionToken}`;
    }
    function exportJSON() {
      window.location.href = `/api/export/json?token=${'$'}{sessionToken}`;
    }

    // THEME & SHORTCUTS
    function toggleTheme() {
      const html = document.documentElement;
      const isDark = html.getAttribute("data-theme") === "dark";
      html.setAttribute("data-theme", isDark ? "light" : "dark");
    }

    function initKeyboardShortcuts() {
      document.addEventListener("keydown", (e) => {
        if (e.target.tagName === "INPUT" || e.target.tagName === "SELECT" || e.target.tagName === "TEXTAREA") {
          if (e.key === "Escape") {
            closeTransactionModal();
            closeCardModal();
          }
          return;
        }

        if (e.key === "n" || e.key === "N") {
          e.preventDefault();
          openTransactionModal();
        } else if (e.key === "/") {
          e.preventDefault();
          switchView("transactions");
          document.getElementById("searchInput").focus();
        } else if (e.key === "Escape") {
          closeTransactionModal();
          closeCardModal();
        }
      });
    }

    // HELPERS
    function formatBRL(val) {
      return (val || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
    }

    function escapeHtml(str) {
      if (!str) return "";
      return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
    }

    function showToast(msg) {
      const c = document.getElementById("toastContainer");
      const t = document.createElement("div");
      t.className = "toast";
      t.innerHTML = `<span>⚡</span> <span>${'$'}{escapeHtml(msg)}</span>`;
      c.appendChild(t);
      setTimeout(() => {
        t.style.opacity = "0";
        setTimeout(() => t.remove(), 300);
      }, 3500);
    }
  </script>
</body>
</html>
"""
    }
}
