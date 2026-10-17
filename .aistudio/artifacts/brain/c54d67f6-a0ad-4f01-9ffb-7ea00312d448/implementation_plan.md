# Correção do Cálculo e Diferença de Centavos nas Compras Parceladas

Plano de ajuste para eliminar a diferença de centavos ao cadastrar e salvar compras parceladas, implementando cálculo financeiro exato com inteiros em centavos, ajuste automático na última parcela e prévia transparente no diálogo.

## Decisões Confirmadas pelo Usuário

> [!IMPORTANT]
> As seguintes preferências foram confirmadas pelo usuário na etapa de alinhamento:
> - **Entrada de valor:** O usuário continuará informando sempre o **Valor Total** da compra no campo principal.
> - **Distribuição de centavos:** A diferença de centavos resultante da divisão (quando houver) será ajustada na **última parcela**, acompanhada de uma **prévia exata e transparente** antes de salvar.

---

### 1. Visão Geral do Problema & Solução

**Causa Raiz Identificada:**
1. **Divisão com ponto flutuante (`Double`):** A função `createInstallments` em `TransactionRepository.kt` realizava divisão direta em ponto flutuante e concentrava todo o `remainder` na **primeira parcela** (`i == 1`), enquanto o diálogo exibia uma divisão média simples (`parsed / totalInstallments`).
2. **Discrepância visual e faturas:** Para compras como R$ 100,00 em 3x, a primeira parcela ficava R$ 33,34 e as outras R$ 33,33, quando o esperado em faturas e na prévia era ver parcelas regulares e a última absorvendo o centavo restante (ou exibir claramente a composição das parcelas).
3. **Deriva de centavos na edição:** Ao editar uma parcela existente em `TransactionDialog`, o código fazia `transactionToEdit.amount * transactionToEdit.totalInstallments`. Ao editar uma parcela de R$ 33,33 de um total de R$ 100,00, o total virava R$ 99,99, perdendo centavos repetidamente.

**Solução:**
- Implementar algoritmo financeiro baseado em inteiros (centavos) para dividir o valor total entre as $N$ parcelas.
- Manter as primeiras $(N - 1)$ parcelas com o valor base arredondado e alocar a diferença exata na última parcela ($N$), garantindo que a soma matemática das parcelas seja rigorosamente igual ao valor total informado.
- Atualizar a prévia no `TransactionDialog` para mostrar exatamente a divisão real: se as parcelas forem iguais, exibe `${N}x de R$ X,XX`; se houver diferença de centavos na última parcela, exibe `${N-1}x de R$ X,XX + 1x de R$ Y,YY (última parcela)`.
- Corrigir a inicialização do valor na edição para somar os valores reais do grupo de parcelas, evitando qualquer drift na edição.

---

### 2. Experiência do Usuário & Mudanças na Interface

1. **Diálogo de Adicionar / Editar Lançamento (`TransactionDialog.kt`):**
   - Ao ativar "Parcelar Gasto / Compra", o usuário digita o **Valor Total** normalmente.
   - O card de resumo de parcelamento exibirá a **prévia exata das parcelas**:
     - *Exemplo com divisão exata (R$ 90,00 em 3x):* `3x de R$ 30,00 • Total: R$ 90,00`
     - *Exemplo com centavo na última parcela (R$ 100,00 em 3x):* `2x de R$ 33,33 + 1x de R$ 33,34 (última) • Total: R$ 100,00`
     - *Exemplo com redução de centavo (R$ 50,00 em 3x):* `2x de R$ 16,67 + 1x de R$ 16,66 (última) • Total: R$ 50,00`
   - O usuário tem certeza imediata do valor de cada fatura antes de clicar em salvar.

2. **Edição de Parcela Existente:**
   - Ao clicar para editar qualquer parcela de um grupo, o campo de valor carregará a soma exata de todas as parcelas do grupo original (e não a multiplicação ingênua de uma única parcela).

---

### 3. Decisões Técnicas & Arquitetura

```
┌──────────────────────────────────────────────────────────────┐
│                    TransactionDialog.kt                      │
│                                                              │
│  Entrada: Total R$ 100,00, Parcelas: 3x                      │
│  Cálculo exato em centavos:                                  │
│    totalCents = 10000                                        │
│    baseCents = Math.round(10000 / 3.0) = 3333                │
│    lastCents = 10000 - (3333 * 2) = 3334                     │
│  Prévia: 2x de R$ 33,33 + 1x de R$ 33,34 (última)            │
└──────────────────────────────┬───────────────────────────────┘
                               │ onSave(totalAmount, count)
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                 TransactionRepository.kt                     │
│                                                              │
│  createInstallments(totalAmount, totalInstallments)          │
│    Loop 1..(count-1): amount = baseCents / 100.0             │
│    Loop count:        amount = lastCents / 100.0             │
│    Soma estrita = totalAmount                                │
│    Inclusão segura no Room Database & Sync                   │
└──────────────────────────────────────────────────────────────┘
```

**Algoritmo de Divisão Exata (Lógica Financeira):**
```kotlin
fun calculateInstallmentSchedule(totalAmount: Double, count: Int): List<Double> {
    val totalCents = Math.round(totalAmount * 100.0)
    val safeCount = count.coerceAtLeast(1)
    if (safeCount == 1) return listOf(totalCents / 100.0)

    val baseCents = Math.round(totalCents.toDouble() / safeCount)
    val lastCents = totalCents - (baseCents * (safeCount - 1))

    return List(safeCount) { index ->
        val cents = if (index == safeCount - 1) lastCents else baseCents
        cents / 100.0
    }
}
```

---

### 4. Plano de Implementação Passo a Passo

1. **Utilitário de Cálculo Financeiro (`InstallmentCalculator.kt`):**
   - Criar `InstallmentCalculator` com funções puras para:
     - `calculateSchedule(totalAmount: Double, totalInstallments: Int): List<Double>`
     - `formatSchedulePreview(totalAmount: Double, totalInstallments: Int): String`
   - Testável isoladamente, garantindo soma idêntica e precisão de 2 casas decimais.

2. **Repositório (`TransactionRepository.kt`):**
   - Atualizar `createInstallments` para usar `InstallmentCalculator.calculateSchedule`.
   - Assegurar que as primeiras $(N-1)$ parcelas recebam `baseAmount` e a última parcela receba o valor ajustado.
   - Atualizar `updateDetailedTransaction` para reaproveitar a mesma lógica consistente.

3. **Interface do Diálogo (`TransactionDialog.kt`):**
   - Integrar `InstallmentCalculator` no bloco de preview de parcelamento.
   - Ao editar um item que pertence a um grupo parcelado, buscar a soma exata das parcelas ativas do grupo para inicializar `amountText`.

4. **Verificação & Compilação:**
   - Executar `compile_applet` para validar integridade do build.
   - Validar cenários:
     - 100.00 em 3x (33.33, 33.33, 33.34)
     - 50.00 em 3x (16.67, 16.67, 16.66)
     - 10.00 em 6x (1.67 x 5 + 1.65)
     - Edição de parcelas sem perda cumulativa de centavos.
