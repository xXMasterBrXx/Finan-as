package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: TransactionType,
    val iconName: String = "category",
    val colorHex: String = "#42A5F5",
    val isCustom: Boolean = false,
    val dbId: Long = 0
)

data class CategoryIconInfo(
    val key: String,
    val vector: ImageVector,
    val label: String,
    val group: String,
    val keywords: String = ""
)

object CategoryIconHelper {
    val iconList: List<CategoryIconInfo> = listOf(
        // Finanças & Renda
        CategoryIconInfo("payments", Icons.Default.Payments, "PIX / Pagamentos", "Finanças", "pix dinheiro recebimento pagamento transferencia"),
        CategoryIconInfo("attach_money", Icons.Default.AttachMoney, "Salário / Dinheiro", "Finanças", "salario dinheiro renda lucro moeda ordenado"),
        CategoryIconInfo("account_balance", Icons.Default.AccountBalance, "Banco / Empréstimo", "Finanças", "banco caixa financeira emprestimo juros agencia"),
        CategoryIconInfo("account_balance_wallet", Icons.Default.AccountBalanceWallet, "Carteira", "Finanças", "carteira saldo dinheiro bolso"),
        CategoryIconInfo("credit_card", Icons.Default.CreditCard, "Cartão de Crédito", "Finanças", "cartao fatura credito debito visa master"),
        CategoryIconInfo("savings", Icons.Default.Savings, "Poupança / Cofrinho", "Finanças", "poupanca cofre reserva investimentos cofrinho"),
        CategoryIconInfo("trending_up", Icons.Default.TrendingUp, "Investimentos", "Finanças", "investimento rendimento acoes bolsa alta"),
        CategoryIconInfo("show_chart", Icons.Default.ShowChart, "Bolsa / Ações", "Finanças", "bolsa valores mercado fundos cripto graficos"),
        CategoryIconInfo("paid", Icons.Default.Paid, "Lucro / Ganhos", "Finanças", "lucro receita vendas bonus comissao premio"),
        CategoryIconInfo("monetization_on", Icons.Default.MonetizationOn, "Rendimento / Moeda", "Finanças", "moeda dividendo juros rendimento dividendos"),
        CategoryIconInfo("currency_exchange", Icons.Default.CurrencyExchange, "Câmbio / Cripto", "Finanças", "dolar euro cambio bitcoin cripto moeda estrangeira"),
        CategoryIconInfo("receipt_long", Icons.Default.ReceiptLong, "Impostos / Tributos", "Finanças", "imposto darf iptu ipva tributo taxa governo"),
        CategoryIconInfo("receipt", Icons.Default.Receipt, "Nota Fiscal / Recibo", "Finanças", "recibo nota fiscal comprovante conta"),
        CategoryIconInfo("price_check", Icons.Default.PriceCheck, "Cotação / Preço", "Finanças", "preco cotacao verificacao"),

        // Alimentação & Bebidas
        CategoryIconInfo("restaurant", Icons.Default.Restaurant, "Restaurante", "Alimentação", "restaurante almoco jantar refeicao comida garfo prato"),
        CategoryIconInfo("fastfood", Icons.Default.Fastfood, "Fast Food / Lanche", "Alimentação", "lanche hamburguer batata delivery mcdonalds burger"),
        CategoryIconInfo("local_cafe", Icons.Default.LocalCafe, "Café / Lanchonete", "Alimentação", "cafe expresso cappuccino cafezinho cafeteria"),
        CategoryIconInfo("local_bar", Icons.Default.LocalBar, "Bar / Drinks", "Alimentação", "bar cerveja drink chopp happy hour coquetel"),
        CategoryIconInfo("local_pizza", Icons.Default.LocalPizza, "Pizza / Pizzaria", "Alimentação", "pizza delivery fatias italiano"),
        CategoryIconInfo("bakery_dining", Icons.Default.BakeryDining, "Padaria / Pães", "Alimentação", "padaria pao doces confeitaria breakfast cafe da manha"),
        CategoryIconInfo("icecream", Icons.Default.Icecream, "Sobremesa / Sorvete", "Alimentação", "sorvete acai sobremesa doce picole gelato"),
        CategoryIconInfo("liquor", Icons.Default.Liquor, "Bebidas / Adega", "Alimentação", "adega vinho cerveja destilado festa whisky"),
        CategoryIconInfo("local_dining", Icons.Default.LocalDining, "Gastronomia", "Alimentação", "gastronomia buffet self service buffet"),

        // Transporte & Viagem
        CategoryIconInfo("directions_car", Icons.Default.DirectionsCar, "Carro / Veículo", "Transporte", "carro automovel veiculo transporte seguro ipva"),
        CategoryIconInfo("local_gas_station", Icons.Default.LocalGasStation, "Combustível / Posto", "Transporte", "gasolina etanol diesel abastecimento posto shell ipiranga"),
        CategoryIconInfo("local_taxi", Icons.Default.LocalTaxi, "Táxi / Uber / 99", "Transporte", "uber 99 taxi corrida motorista app transporte"),
        CategoryIconInfo("directions_bus", Icons.Default.DirectionsBus, "Ônibus", "Transporte", "onibus circular rodoviaria passagem coletivo"),
        CategoryIconInfo("directions_subway", Icons.Default.DirectionsSubway, "Metrô / Trem", "Transporte", "metro trem estacao cptm transporte publico"),
        CategoryIconInfo("two_wheeler", Icons.Default.TwoWheeler, "Moto / Entrega", "Transporte", "moto motocicleta motoboy entregador scooter"),
        CategoryIconInfo("pedal_bike", Icons.Default.PedalBike, "Bicicleta", "Transporte", "bike bicicleta ciclovia pedal ciclista"),
        CategoryIconInfo("flight", Icons.Default.Flight, "Passagem / Voo", "Transporte", "aviao voo passagem aerea viagem latam gol azul"),
        CategoryIconInfo("local_parking", Icons.Default.LocalParking, "Estacionamento / Pedágio", "Transporte", "estacionamento pedagio vaga valet sem parar taggy"),
        CategoryIconInfo("car_repair", Icons.Default.CarRepair, "Mecânico / Oficina", "Transporte", "mecanico conserto oficina manutencao pneu troca oleo revisao"),
        CategoryIconInfo("commute", Icons.Default.Commute, "Transporte Diário", "Transporte", "deslocamento transporte conducao ida volta"),
        CategoryIconInfo("luggage", Icons.Default.Luggage, "Viagem / Férias", "Transporte", "mala bagagem viagem turismo ferias aeroporto"),
        CategoryIconInfo("hotel", Icons.Default.Hotel, "Hotel / Pousada", "Transporte", "hotel pousada estadia booking airbnb hospedagem resort"),
        CategoryIconInfo("beach_access", Icons.Default.BeachAccess, "Praia / Lazer", "Transporte", "praia quiosque sol ferias verao mar"),

        // Compras & Varejo
        CategoryIconInfo("shopping_bag", Icons.Default.ShoppingBag, "Compras / Sacola", "Compras", "compras shopping sacola varejo loja"),
        CategoryIconInfo("shopping_cart", Icons.Default.ShoppingCart, "Supermercado", "Compras", "supermercado mercado compras carrinho alimentos atacadao pão de acucar"),
        CategoryIconInfo("local_grocery_store", Icons.Default.LocalGroceryStore, "Mercearia / Feira", "Compras", "feira hortifruti verduras frutas mercado quitanda sacolao"),
        CategoryIconInfo("local_mall", Icons.Default.LocalMall, "Shopping Center", "Compras", "shopping lojas compras mall comercio"),
        CategoryIconInfo("storefront", Icons.Default.Storefront, "Lojas / Comércio", "Compras", "loja comercio comercio local bairro boutique"),
        CategoryIconInfo("checkroom", Icons.Default.Checkroom, "Roupas / Moda", "Compras", "roupas vestuario sapatos moda calcados zara renner c&a"),
        CategoryIconInfo("card_giftcard", Icons.Default.CardGiftcard, "Presentes / Mimos", "Compras", "presente mimo aniversario lembranca natal"),
        CategoryIconInfo("sell", Icons.Default.Sell, "Ofertas / Vendas", "Compras", "promocao desconto saldo oferta liquidacao"),
        CategoryIconInfo("redeem", Icons.Default.Redeem, "Cupons / Recompensas", "Compras", "cupom recompensa cashback pontos fidelidade"),

        // Casa, Família & Utilidades
        CategoryIconInfo("home", Icons.Default.Home, "Casa / Moradia", "Casa & Família", "casa lar moradia habitacao residencia"),
        CategoryIconInfo("apartment", Icons.Default.Apartment, "Aluguel / Condomínio", "Casa & Família", "aluguel condominio apartamento predio quarto"),
        CategoryIconInfo("lightbulb", Icons.Default.Lightbulb, "Energia / Luz", "Casa & Família", "luz energia eletricidade conta cemig enel celesc light"),
        CategoryIconInfo("water_drop", Icons.Default.WaterDrop, "Água / Saneamento", "Casa & Família", "agua saneamento sabesp conta esgoto compesa copasa"),
        CategoryIconInfo("wifi", Icons.Default.Wifi, "Internet / Wi-Fi", "Casa & Família", "internet banda larga wifi conexao fibra claro vivo oi tim"),
        CategoryIconInfo("phone_iphone", Icons.Default.PhoneIphone, "Celular / Telefonia", "Casa & Família", "celular telefone plano tim claro vivo recarga smartphone"),
        CategoryIconInfo("cleaning_services", Icons.Default.CleaningServices, "Limpeza / Faxina", "Casa & Família", "limpeza diarista faxina produtos limpeza lavanderia sabao"),
        CategoryIconInfo("build", Icons.Default.Build, "Reforma / Reparos", "Casa & Família", "reforma manutencao conserto obra pedreiro ferramentas leroy"),
        CategoryIconInfo("chair", Icons.Default.Chair, "Móveis / Decoração", "Casa & Família", "moveis sofa cama decoracao decor tokstok ikea mobly"),
        CategoryIconInfo("child_care", Icons.Default.ChildCare, "Filhos / Bebê", "Casa & Família", "filho bebe crianca fralda creche escola pediatra"),
        CategoryIconInfo("family_restroom", Icons.Default.FamilyRestroom, "Família / Parentes", "Casa & Família", "familia pensao mesada parentes suporte"),
        CategoryIconInfo("yard", Icons.Default.Yard, "Jardim / Quintal", "Casa & Família", "jardim plantas quintal floricultura grama"),
        CategoryIconInfo("pets", Icons.Default.Pets, "Pet / Animais", "Casa & Família", "pet cachorro gato veterinario racao animal cobasi petz"),

        // Saúde & Bem-Estar
        CategoryIconInfo("medical_services", Icons.Default.MedicalServices, "Saúde / Consultas", "Saúde & Lazer", "saude consulta medico clinica plano unimed"),
        CategoryIconInfo("local_hospital", Icons.Default.LocalHospital, "Hospital / Pronto Socorro", "Saúde & Lazer", "hospital pronto socorro exames laboratorio ps"),
        CategoryIconInfo("medication", Icons.Default.Medication, "Farmácia / Remédios", "Saúde & Lazer", "farmacia remedio drogaria medicamento drogasil pacheco raia"),
        CategoryIconInfo("healing", Icons.Default.Healing, "Tratamentos / Dentista", "Saúde & Lazer", "curativo tratamento fisioterapia dentista dente odonto"),
        CategoryIconInfo("fitness_center", Icons.Default.FitnessCenter, "Academia / Treino", "Saúde & Lazer", "academia musculacao crossfit treino esporte smartfit gym"),
        CategoryIconInfo("spa", Icons.Default.Spa, "Estética / Spa", "Saúde & Lazer", "spa massagem estetica cuidados bem estar skincare"),
        CategoryIconInfo("self_improvement", Icons.Default.SelfImprovement, "Yoga / Terapia", "Saúde & Lazer", "yoga meditacao terapia psicologo psiquiatra mindfulness"),
        CategoryIconInfo("sports_soccer", Icons.Default.SportsSoccer, "Futebol / Esportes", "Saúde & Lazer", "futebol quadra esporte society jogo pelada bola tenis"),
        CategoryIconInfo("pool", Icons.Default.Pool, "Natação / Clube", "Saúde & Lazer", "piscina natacao clube lazer aquafit"),
        CategoryIconInfo("content_cut", Icons.Default.ContentCut, "Cabelo / Barbearia", "Saúde & Lazer", "cabelo barbeiro salao manicure beleza sobrancelha barba"),

        // Lazer & Cultura
        CategoryIconInfo("sports_esports", Icons.Default.SportsEsports, "Jogos / Games", "Saúde & Lazer", "games jogos playstation xbox steam switch nintendo gamer"),
        CategoryIconInfo("movie", Icons.Default.Movie, "Cinema / Filmes", "Saúde & Lazer", "cinema filme pipoca ingresso telona cinemark"),
        CategoryIconInfo("theater_comedy", Icons.Default.TheaterComedy, "Teatro / Stand-up", "Saúde & Lazer", "teatro show comedia peca musical standup cultura"),
        CategoryIconInfo("music_note", Icons.Default.MusicNote, "Música / Shows", "Saúde & Lazer", "musica show concerto festival spotify deezer instrumento"),
        CategoryIconInfo("camera_alt", Icons.Default.CameraAlt, "Fotografia", "Saúde & Lazer", "foto camera ensaio imagem video lente"),
        CategoryIconInfo("book", Icons.Default.Book, "Livros / Leitura", "Saúde & Lazer", "livro leitura livraria ebook saraiva leitura amazon"),
        CategoryIconInfo("celebration", Icons.Default.Celebration, "Festas / Eventos", "Saúde & Lazer", "festa balada aniversario comemoracao casamento formatura"),
        CategoryIconInfo("casino", Icons.Default.Casino, "Apostas / Loterias", "Saúde & Lazer", "aposta loteria jogo bingo sorte mega sena bet"),
        CategoryIconInfo("star", Icons.Default.Star, "Destaque / Especial", "Saúde & Lazer", "estrela especial favorito preferido"),

        // Trabalho, Educação & Tecnologia
        CategoryIconInfo("work", Icons.Default.Work, "Trabalho / Emprego", "Trabalho & Outros", "trabalho emprego servico escritorio clt"),
        CategoryIconInfo("laptop_mac", Icons.Default.LaptopMac, "Freelance / TI", "Trabalho & Outros", "freelance notebook home office programacao desenvolvedor"),
        CategoryIconInfo("school", Icons.Default.School, "Educação / Faculdade", "Trabalho & Outros", "escola faculdade universidade mensalidade pós pos graduacao"),
        CategoryIconInfo("menu_book", Icons.Default.MenuBook, "Cursos / Estudos", "Trabalho & Outros", "curso treinamento aula capacitacao certificacao"),
        CategoryIconInfo("business_center", Icons.Default.BusinessCenter, "Negócios / Empresa", "Trabalho & Outros", "empresa cnpj negocio escritorio mei consultoria"),
        CategoryIconInfo("calculate", Icons.Default.Calculate, "Contabilidade / Finanças", "Trabalho & Outros", "contabilidade contador calculo imposto ir"),
        CategoryIconInfo("computer", Icons.Default.Computer, "Equipamentos / PC", "Trabalho & Outros", "computador perifericos monitor setup teclado hardware"),
        CategoryIconInfo("subscriptions", Icons.Default.Subscriptions, "Assinaturas / Streaming", "Trabalho & Outros", "netflix amazon spotify youtube assinatura plano streaming hbo disney prime"),
        CategoryIconInfo("cloud", Icons.Default.Cloud, "Serviços em Nuvem", "Trabalho & Outros", "nuvem cloud icloud google drive storage backup saas"),
        CategoryIconInfo("security", Icons.Default.Security, "Seguros / Segurança", "Trabalho & Outros", "seguro protecao apolice carro vida seguradora alarme"),
        CategoryIconInfo("volunteer_activism", Icons.Default.VolunteerActivism, "Doações / Caridade", "Trabalho & Outros", "doacao caridade ajuda ong voluntariado caridade dízimo"),
        CategoryIconInfo("emergency", Icons.Default.Emergency, "Imprevistos / Emergência", "Trabalho & Outros", "imprevisto urgencia emergencia conserto socorro"),
        CategoryIconInfo("category", Icons.Default.Category, "Geral / Diversos", "Trabalho & Outros", "geral outros diversos categoria"),
        CategoryIconInfo("more_horiz", Icons.Default.MoreHoriz, "Outros", "Trabalho & Outros", "outros mais diversos")
    )

    val availableIcons: Map<String, ImageVector> = iconList.associate { it.key to it.vector }

    val availableColors = listOf(
        "#EF5350", "#EC407A", "#AB47BC", "#7E57C2",
        "#5C6BC0", "#42A5F5", "#29B6F6", "#26C6DA",
        "#26A69A", "#43A047", "#66BB6A", "#9CCC65",
        "#D4E157", "#FFEE58", "#FFCA28", "#FFA726",
        "#FF7043", "#8D6E63", "#78909C", "#0284C7"
    )

    fun getIcon(iconName: String): ImageVector {
        return availableIcons[iconName] ?: Icons.Default.Category
    }

    fun parseColor(hex: String, fallback: Color = Color(0xFF42A5F5)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = if (cleanHex.length == 6) {
                ("FF$cleanHex").toLong(16).toInt()
            } else if (cleanHex.length == 8) {
                cleanHex.toLong(16).toInt()
            } else {
                return fallback
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}

object Categories {
    // Expense Categories
    val Food = CategoryItem(
        id = "food",
        name = "Alimentação",
        icon = Icons.Default.Restaurant,
        color = Color(0xFFFF7043),
        type = TransactionType.EXPENSE
    )
    val Housing = CategoryItem(
        id = "housing",
        name = "Moradia",
        icon = Icons.Default.Home,
        color = Color(0xFF42A5F5),
        type = TransactionType.EXPENSE
    )
    val Transport = CategoryItem(
        id = "transport",
        name = "Transporte",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFFAB47BC),
        type = TransactionType.EXPENSE
    )
    val Leisure = CategoryItem(
        id = "leisure",
        name = "Lazer",
        icon = Icons.Default.SportsEsports,
        color = Color(0xFFEC407A),
        type = TransactionType.EXPENSE
    )
    val Health = CategoryItem(
        id = "health",
        name = "Saúde",
        icon = Icons.Default.MedicalServices,
        color = Color(0xFFEF5350),
        type = TransactionType.EXPENSE
    )
    val Education = CategoryItem(
        id = "education",
        name = "Educação",
        icon = Icons.Default.School,
        color = Color(0xFF26A69A),
        type = TransactionType.EXPENSE
    )
    val Bills = CategoryItem(
        id = "bills",
        name = "Contas & Fixas",
        icon = Icons.Default.ReceiptLong,
        color = Color(0xFFFFB74D),
        type = TransactionType.EXPENSE
    )
    val Shopping = CategoryItem(
        id = "shopping",
        name = "Compras",
        icon = Icons.Default.ShoppingBag,
        color = Color(0xFF7E57C2),
        type = TransactionType.EXPENSE
    )
    val OtherExpense = CategoryItem(
        id = "other_expense",
        name = "Outras Saídas",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF78909C),
        type = TransactionType.EXPENSE
    )

    // Income Categories
    val Salary = CategoryItem(
        id = "salary",
        name = "Salário",
        icon = Icons.Default.Payments,
        color = Color(0xFF2E7D32),
        type = TransactionType.INCOME
    )
    val Freelance = CategoryItem(
        id = "freelance",
        name = "Freelance / Extra",
        icon = Icons.Default.LaptopMac,
        color = Color(0xFF00897B),
        type = TransactionType.INCOME
    )
    val Investments = CategoryItem(
        id = "investments",
        name = "Investimentos",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF1E88E5),
        type = TransactionType.INCOME
    )
    val Gift = CategoryItem(
        id = "gift",
        name = "Presente / Bônus",
        icon = Icons.Default.CardGiftcard,
        color = Color(0xFF8E24AA),
        type = TransactionType.INCOME
    )
    val OtherIncome = CategoryItem(
        id = "other_income",
        name = "Outras Entradas",
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFF43A047),
        type = TransactionType.INCOME
    )

    val expenseCategories = listOf(
        Food,
        Housing,
        Transport,
        Bills,
        Shopping,
        Leisure,
        Health,
        Education,
        OtherExpense
    )

    val incomeCategories = listOf(
        Salary,
        Freelance,
        Investments,
        Gift,
        OtherIncome
    )

    fun getCategoryByName(
        name: String,
        type: TransactionType,
        customCategories: List<CategoryItem> = emptyList()
    ): CategoryItem {
        val customMatches = customCategories.filter { it.type == type }
        val list = if (type == TransactionType.INCOME) (incomeCategories + customMatches) else (expenseCategories + customMatches)
        return list.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: (if (type == TransactionType.INCOME) OtherIncome else OtherExpense)
    }

    fun findCategoryByName(
        name: String,
        customCategories: List<CategoryItem> = emptyList()
    ): CategoryItem {
        val all = expenseCategories + incomeCategories + customCategories
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: OtherExpense
    }
}
