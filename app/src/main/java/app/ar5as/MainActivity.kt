package app.ar5as

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ar5as.R
import app.ar5as.data.CATEGORIES
import app.ar5as.data.DEFAULT_BASKET_IDS
import app.ar5as.data.PROMOS
import app.ar5as.data.PriceStore
import app.ar5as.data.Product
import app.ar5as.data.SEED_PRODUCTS
import app.ar5as.data.STORES
import app.ar5as.data.cheapestPrice
import app.ar5as.data.expensivePrice
import app.ar5as.data.fmt
import app.ar5as.data.photoResFor
import app.ar5as.ui.theme.Ar5asAmber
import app.ar5as.ui.theme.Ar5asGreen
import app.ar5as.ui.theme.Ar5asTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = PriceStore(applicationContext)
        setContent {
            Ar5asTheme {
                Ar5asApp(store)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ar5asApp(store: PriceStore) {
    var tab by remember { mutableStateOf(0) }
    var version by remember { mutableStateOf(0) }
    var compareQuery by remember { mutableStateOf("") }
    var compareSelectedId by remember { mutableStateOf<String?>(null) }

    val allProducts = remember(version) { SEED_PRODUCTS + store.customProducts() }
    fun bump() { version++ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ar5as — أرخص", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Ar5asGreen,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "قارن") },
                    label = { Text("قارن", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.LocalOffer, contentDescription = "عروض") },
                    label = { Text("عروض", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "القفة") },
                    label = { Text("القفة", fontSize = 11.sp) }
                )
            }
        }
    ) { padding ->
        key(version) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {
                when (tab) {
                    0 -> HomeScreen(
                        store = store,
                        products = allProducts,
                        onOpenProduct = { id ->
                            compareSelectedId = id
                            compareQuery = ""
                            tab = 1
                        },
                        onOpenPromo = { tab = 2 },
                        onOpenAddPrice = { tab = 1 }
                    )
                    1 -> CompareScreen(
                        store = store,
                        products = allProducts,
                        initialQuery = compareQuery,
                        selectedId = compareSelectedId,
                        onSelect = { compareSelectedId = it },
                        onDataChanged = ::bump
                    )
                    2 -> PromoScreen()
                    3 -> BudgetScreen(store = store, products = allProducts)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Product photo thumbnail (v0.2, Pixabay photos bundled in the app).
// Custom user products have no photo: show a generic basket icon instead.
// ---------------------------------------------------------------------------
@Composable
fun ProductThumb(product: Product, size: Int) {
    val res = product.imageRes
    if (res != null) {
        Image(
            painter = painterResource(id = res),
            contentDescription = product.nameAr,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = Ar5asGreen
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Accueil
// ---------------------------------------------------------------------------
@Composable
fun HomeScreen(
    store: PriceStore,
    products: List<Product>,
    onOpenProduct: (String) -> Unit,
    onOpenPromo: () -> Unit,
    onOpenAddPrice: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }

    val filtered = products.filter {
        (category == null || it.category == category) &&
            (query.isBlank() || it.nameAr.contains(query, ignoreCase = true) ||
                it.nameFr.contains(query, ignoreCase = true))
    }

    val bestPromo = PROMOS.maxByOrNull { it.savingPct }
    val basket = DEFAULT_BASKET_IDS.mapNotNull { id -> products.find { it.id == id } }
    val basketCheap = basket.sumOf { cheapestPrice(store, it)?.second ?: 0.0 }
    val basketExp = basket.sumOf { expensivePrice(store, it)?.second ?: 0.0 }
    val basketSaving = basketExp - basketCheap

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            // v0.2 hero: real market photo banner, like the web preview.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_market),
                    contentDescription = "سوق",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        "وين أرخص؟",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        "قارن قبل ما تشري",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("لوّج على منتج… زيت، سكّر، دجاج…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = category == null,
                        onClick = { category = null },
                        label = { Text("الكل") }
                    )
                }
                items(CATEGORIES) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = if (category == cat) null else cat },
                        label = { Text(cat) }
                    )
                }
            }
        }
        if (bestPromo != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPromo() },
                    colors = CardDefaults.cardColors(containerColor = Ar5asAmber)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("🔥 أحسن عرض اليوم", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            "${bestPromo.productAr} — ${bestPromo.store}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                fmt(bestPromo.oldPrice),
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                fmt(bestPromo.newPrice),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "-${bestPromo.savingPct}%",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("حتى ${bestPromo.ends} — شوف كل العروض", color = Color.Black)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("🧺 أرخص قفة (10 منتجات أساسية)", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أرخص مجموع: ", fontSize = 15.sp)
                        Text(fmt(basketCheap), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ar5asGreen)
                    }
                    Text("أغلى مجموع: ${fmt(basketExp)}")
                    Text(
                        "توفّر ${fmt(basketSaving)} كي تشري الأرخص",
                        fontWeight = FontWeight.Bold,
                        color = Ar5asGreen
                    )
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onOpenAddPrice,
                modifier = Modifier.fillMaxWidth()
            ) { Text("✏️ زيد سوم / صحّح سوم شفتو") }
        }
        item {
            Text("المنتجات", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        items(filtered) { product ->
            val cheap = cheapestPrice(store, product)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProduct(product.id) }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductThumb(product, 64)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.nameAr, fontWeight = FontWeight.Bold)
                        Text("${product.nameFr} • ${product.category}", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            cheap?.let { fmt(it.second) } ?: "—",
                            fontWeight = FontWeight.Bold,
                            color = Ar5asGreen,
                            fontSize = 17.sp
                        )
                        Text(cheap?.first ?: "", fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            Text(
                "⚠️ الأسعار تقريبية (مثال فقط، نوفمبر 2026) — موش أسعار رسمية. " +
                    "نجّم تبدّل أي سوم من «قارن» و يتحفظ في تليفونك.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Comparer
// ---------------------------------------------------------------------------
@Composable
fun CompareScreen(
    store: PriceStore,
    products: List<Product>,
    initialQuery: String,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDataChanged: () -> Unit
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var editTarget by remember { mutableStateOf<Pair<Product, String>?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    val filtered = products.filter {
        query.isBlank() || it.nameAr.contains(query, ignoreCase = true) ||
            it.nameFr.contains(query, ignoreCase = true)
    }
    val selected = products.find { it.id == selectedId } ?: filtered.firstOrNull()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("لوّج على منتج…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("قارن الأسعار", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { showAdd = true }) { Text("+ زيد منتج") }
            }
        }
        items(filtered) { product ->
            val cheap = cheapestPrice(store, product)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(product.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (product.id == selected?.id)
                        MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductThumb(product, 64)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        product.nameAr,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        cheap?.let { "${fmt(it.second)} • ${it.first}" } ?: "—",
                        color = Ar5asGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (selected != null) {
            item {
                val cheap = cheapestPrice(store, selected)
                val exp = expensivePrice(store, selected)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        selected.imageRes?.let { res ->
                            Image(
                                painter = painterResource(id = res),
                                contentDescription = selected.nameAr,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Text(selected.nameAr, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text("${selected.nameFr} • ${selected.unit} • ${selected.category}", fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))
                        STORES.forEach { storeName ->
                            val price = store.priceOf(selected, storeName)
                            val isCheapest = cheap?.first == storeName
                            val isExpensive = exp?.first == storeName
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(storeName, fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal)
                                    if (isCheapest) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "الأرخص ✓",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Ar5asGreen, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            price?.let { fmt(it) } ?: "—",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCheapest) Ar5asGreen else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp
                                        )
                                        if (price != null && exp != null && !isExpensive) {
                                            Text(
                                                "أرخص بـ ${fmt(exp.second - price)} من الأغلى",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else if (isExpensive) {
                                            Text(
                                                "الأغلى",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { editTarget = selected to storeName }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "عدّل السوم")
                                    }
                                }
                            }
                        }
                        if (cheap != null && exp != null && cheap.second < exp.second) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "الفرق بين الأرخص والأغلى: ${fmt(exp.second - cheap.second)}",
                                fontWeight = FontWeight.Bold,
                                color = Ar5asGreen
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    editTarget?.let { (product, storeName) ->
        EditPriceDialog(
            product = product,
            storeName = storeName,
            current = store.priceOf(product, storeName),
            onDismiss = { editTarget = null },
            onSave = { newPrice ->
                store.setPrice(product.id, storeName, newPrice)
                editTarget = null
                onDataChanged()
            }
        )
    }
    if (showAdd) {
        AddProductDialog(
            onDismiss = { showAdd = false },
            onSave = { name, category, storeName, price ->
                store.addCustomProduct(name, category, storeName, price)
                showAdd = false
                onDataChanged()
            }
        )
    }
}

@Composable
fun EditPriceDialog(
    product: Product,
    storeName: String,
    current: Double?,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var text by remember { mutableStateOf(current?.let { "%.2f".format(it) } ?: "") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("زيد / صحّح سوم") },
        text = {
            Column {
                Text("${product.nameAr} — $storeName")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.replace(',', '.'); error = false },
                    label = { Text("السوم (د.ت)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = error,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) Text("اكتب سوم صحيح، مثال: 7.50", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(6.dp))
                Text("السوم يتحفظ في تليفونك برك.", fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                val v = text.toDoubleOrNull()
                if (v == null || v <= 0) error = true else onSave(v)
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES[0]) }
    var storeName by remember { mutableStateOf(STORES[0]) }
    var priceText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("زيد منتج جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text("اسم المنتج (مثال: عدس أحمر 1كغ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("الفئة:", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Text("المتجر:", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(STORES) { s ->
                        FilterChip(
                            selected = storeName == s,
                            onClick = { storeName = s },
                            label = { Text(s) }
                        )
                    }
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.replace(',', '.'); error = false },
                    label = { Text("السوم (د.ت)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = error,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) Text("اكتب اسم وسوم صحيح.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                val v = priceText.toDoubleOrNull()
                if (name.isBlank() || v == null || v <= 0) error = true
                else onSave(name.trim(), category, storeName, v)
            }) { Text("زيد") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ---------------------------------------------------------------------------
// Promo
// ---------------------------------------------------------------------------
@Composable
fun PromoScreen() {
    val promos = PROMOS.sortedByDescending { it.savingPct }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("عروض الأسبوع", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "أسعار تقريبية — مثال فقط. تحقّق من السوم في المتجر قبل ما تشري.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(promos) { promo ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    photoResFor(promo.productId)?.let { res ->
                        Image(
                            painter = painterResource(id = res),
                            contentDescription = promo.productAr,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(promo.productAr, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(promo.store, fontSize = 13.sp)
                        Text("حتى ${promo.ends}", fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                fmt(promo.oldPrice),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(fmt(promo.newPrice), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Ar5asGreen)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Ar5asAmber)) {
                        Text(
                            "-${promo.savingPct}%",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------------------------
// Budget / القفة
// ---------------------------------------------------------------------------
@Composable
fun BudgetScreen(store: PriceStore, products: List<Product>) {
    var budgetText by remember { mutableStateOf("") }
    val quantities = remember { mutableStateMapOf<String, Int>() }

    val budget = budgetText.toDoubleOrNull() ?: 0.0
    var cheapTotal = 0.0
    var expTotal = 0.0
    quantities.forEach { (id, qty) ->
        if (qty > 0) {
            products.find { it.id == id }?.let { product ->
                cheapTotal += (cheapestPrice(store, product)?.second ?: 0.0) * qty
                expTotal += (expensivePrice(store, product)?.second ?: 0.0) * qty
            }
        }
    }
    val remaining = budget - cheapTotal
    val saving = expTotal - cheapTotal

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("القفة — قدّاش تنجّم تصرف؟", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it.replace(',', '.') },
                label = { Text("الميزانية الشهرية (د.ت)") },
                placeholder = { Text("مثال: 400") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("مجموع القفة (بأرخص الأسعار): ${fmt(cheapTotal)}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("بأغلى الأسعار: ${fmt(expTotal)} — توفّر ${fmt(saving)}")
                    if (budget > 0) {
                        if (remaining >= 0) {
                            Text("يبقى لك: ${fmt(remaining)} ✓", fontWeight = FontWeight.Bold, color = Ar5asGreen)
                        } else {
                            Text(
                                "فوق الميزانية بـ ${fmt(-remaining)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text("اكتب ميزانيتك باش تشوف الباقي.", fontSize = 13.sp)
                    }
                }
            }
        }
        item { Text("اختار المنتجات والكميات:", fontWeight = FontWeight.Bold) }
        items(products) { product ->
            val qty = quantities[product.id] ?: 0
            val cheap = cheapestPrice(store, product)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductThumb(product, 48)
                        Spacer(Modifier.width(8.dp))
                        Checkbox(
                            checked = qty > 0,
                            onCheckedChange = { checked ->
                                quantities[product.id] = if (checked) 1 else 0
                            }
                        )
                        Column {
                            Text(product.nameAr, fontWeight = FontWeight.Bold)
                            Text(
                                cheap?.let { "${fmt(it.second)} • ${it.first}" } ?: "—",
                                fontSize = 12.sp,
                                color = Ar5asGreen
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (qty > 0) quantities[product.id] = qty - 1 },
                            modifier = Modifier.size(36.dp)
                        ) { Icon(Icons.Filled.Remove, contentDescription = "نقّص") }
                        Text("$qty", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        IconButton(
                            onClick = { quantities[product.id] = qty + 1 },
                            modifier = Modifier.size(36.dp)
                        ) { Icon(Icons.Filled.Add, contentDescription = "زيّد") }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
