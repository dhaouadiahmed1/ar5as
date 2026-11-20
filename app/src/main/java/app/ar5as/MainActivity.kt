package app.ar5as

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import app.ar5as.ui.theme.Ar5asCloud
import app.ar5as.ui.theme.Ar5asCobalt
import app.ar5as.ui.theme.Ar5asCobaltBar
import app.ar5as.ui.theme.Ar5asCobaltDark
import app.ar5as.ui.theme.Ar5asCobaltLight
import app.ar5as.ui.theme.Ar5asCobaltTint
import app.ar5as.ui.theme.Ar5asInk
import app.ar5as.ui.theme.Ar5asInkSoft
import app.ar5as.ui.theme.Ar5asSunshine
import app.ar5as.ui.theme.Ar5asTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Ease-out-expo easing, like the web preview's count-up tweens.
private val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

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
    var sheetProductId by remember { mutableStateOf<String?>(null) }
    var splashVisible by remember { mutableStateOf(true) }
    // Basket quantities are hoisted here so the bottom sheet's
    // "Ajouter à la قفة" feeds the same basket as the Budget tab.
    val quantities = remember { mutableStateMapOf<String, Int>() }

    val allProducts = remember(version) { SEED_PRODUCTS + store.customProducts() }
    fun bump() { version++ }
    val basketCount = quantities.values.count { it > 0 }
    val sheetProduct = allProducts.find { it.id == sheetProductId }

    LaunchedEffect(Unit) {
        delay(1150)
        splashVisible = false
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ar5as — أرخص", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Ar5asCobalt,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية", fontSize = 11.sp) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "قارن") },
                        label = { Text("قارن", fontSize = 11.sp) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Filled.LocalOffer, contentDescription = "عروض") },
                        label = { Text("عروض", fontSize = 11.sp) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (basketCount > 0) {
                                        PopBadge(basketCount)
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "القفة")
                            }
                        },
                        label = { Text("القفة", fontSize = 11.sp) },
                        colors = navColors()
                    )
                }
            }
        ) { padding ->
            key(version) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Ar5asCloud)
                        .padding(padding)
                        .padding(horizontal = 12.dp)
                ) {
                    when (tab) {
                        0 -> HomeScreen(
                            store = store,
                            products = allProducts,
                            onOpenSheet = { sheetProductId = it },
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
                        2 -> PromoScreen(onOpenSheet = { sheetProductId = it })
                        3 -> BudgetScreen(store = store, products = allProducts, quantities = quantities)
                    }
                }
            }
        }

        // Product bottom sheet (from Home and Promos).
        if (sheetProduct != null) {
            ProductSheet(
                product = sheetProduct,
                store = store,
                quantities = quantities,
                onDismiss = { sheetProductId = null },
                onGoToCompare = { id ->
                    sheetProductId = null
                    compareSelectedId = id
                    compareQuery = ""
                    tab = 1
                }
            )
        }

        // Splash intro: cobalt screen, spring-in logo, sunshine loading bar,
        // lifts away after ~1s to reveal the home screen.
        AnimatedVisibility(
            visible = splashVisible,
            exit = fadeOut(tween(350)) + slideOutVertically(tween(350)) { -it }
        ) {
            SplashScreen()
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Ar5asCobalt,
    selectedTextColor = Ar5asCobalt,
    unselectedIconColor = Ar5asInkSoft,
    unselectedTextColor = Ar5asInkSoft,
    indicatorColor = Ar5asCobaltTint
)

// ---------------------------------------------------------------------------
// Splash intro (v0.3, like the web preview).
// ---------------------------------------------------------------------------
@Composable
fun SplashScreen() {
    val logoScale = remember { Animatable(0.3f) }
    val bar = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        bar.animateTo(1f, tween(950, easing = EaseOutExpo))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ar5asCobalt),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .background(Ar5asSunshine, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = Ar5asCobalt,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Ar5as",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp
            )
            Text(
                "أرخص — قارن قبل ما تشري",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(bar.value)
                        .height(6.dp)
                        .background(Ar5asSunshine, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Motion helpers (v0.3, 60fps.design-inspired).
// ---------------------------------------------------------------------------

/** Springy press feedback: card shrinks a little while pressed, springs back. */
@Composable
fun SpringCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "springPress"
    )
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

/** Staggered entrance: fades + rises in, delayed by index. */
@Composable
fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(28f) }
    LaunchedEffect(Unit) {
        delay((index * 70).coerceAtMost(420).toLong())
        launch { alpha.animateTo(1f, tween(380)) }
        launch { offsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
    }
    Box(
        Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offsetY.value
        }
    ) { content() }
}

/** Number that counts up with an ease-out-expo tween, like the preview. */
@Composable
fun AnimatedNumber(value: Double, decimals: Int = 2, suffix: String = "") {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(value) {
        anim.animateTo(value.toFloat(), tween(1000, easing = EaseOutExpo))
    }
    Text(
        "%.${decimals}f".format(anim.value) + suffix,
        fontWeight = FontWeight.Bold,
        color = Ar5asCobalt,
        fontSize = 20.sp
    )
}

/** Integer that counts up (27 products, 5 stores…). */
@Composable
fun AnimatedInt(value: Int) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(value) {
        anim.animateTo(value.toFloat(), tween(900, easing = EaseOutExpo))
    }
    Text(
        "${anim.value.toInt()}",
        fontWeight = FontWeight.Bold,
        color = Ar5asCobalt,
        fontSize = 20.sp
    )
}

/** Basket badge that pops every time the count changes. */
@Composable
fun PopBadge(count: Int) {
    val pop = remember { Animatable(1f) }
    LaunchedEffect(count) {
        pop.snapTo(0.5f)
        pop.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    Badge(
        containerColor = Ar5asSunshine,
        contentColor = Ar5asInk,
        modifier = Modifier.graphicsLayer {
            scaleX = pop.value
            scaleY = pop.value
        }
    ) { Text("$count", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
}

/** Horizontal price bar that grows with a staggered delay. */
@Composable
fun PriceBar(fraction: Float, delayMs: Int, color: Color) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        anim.animateTo(fraction.coerceIn(0f, 1f), tween(600, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFEAF0F8), RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(anim.value)
                .height(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
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
                .background(Ar5asCobaltTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = Ar5asCobalt
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
    onOpenSheet: (String) -> Unit,
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
            StaggerIn(0) {
                Column {
                    // Hero: real market photo banner, cobalt scrim.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
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
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Ar5asCobalt.copy(alpha = 0.55f),
                                            Ar5asCobalt.copy(alpha = 0.85f)
                                        )
                                    )
                                )
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
                                fontSize = 26.sp
                            )
                            Text(
                                "قارن قبل ما تشري",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("لوّج على منتج… زيت، سكّر، دجاج…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ar5asCobalt,
                            unfocusedBorderColor = Color(0xFFD0D9E6),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }
        }
        item {
            StaggerIn(1) {
                // Count-up stats, like the preview hero.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedInt(products.size)
                            Text("منتج", fontSize = 12.sp, color = Ar5asInkSoft)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedInt(STORES.size)
                            Text("متاجر", fontSize = 12.sp, color = Ar5asInkSoft)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "-${bestPromo?.savingPct ?: 0}%",
                                fontWeight = FontWeight.Bold,
                                color = Ar5asCobalt,
                                fontSize = 20.sp
                            )
                            Text("أحسن عرض", fontSize = 12.sp, color = Ar5asInkSoft)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedNumber(basketCheap, suffix = " د.ت")
                            Text("أرخص قفة", fontSize = 12.sp, color = Ar5asInkSoft)
                        }
                    }
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = category == null,
                        onClick = { category = null },
                        label = { Text("الكل") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Ar5asCobalt,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(CATEGORIES) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = if (category == cat) null else cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Ar5asCobalt,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        if (bestPromo != null) {
            item {
                StaggerIn(2) {
                    SpringCard(onClick = onOpenPromo, containerColor = Ar5asSunshine) {
                        Column(Modifier.padding(14.dp)) {
                            Text("أحسن عرض اليوم", fontWeight = FontWeight.Bold, color = Ar5asInk)
                            Text(
                                "${bestPromo.productAr} — ${bestPromo.store}",
                                color = Ar5asInk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    fmt(bestPromo.oldPrice),
                                    color = Ar5asInk,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    fmt(bestPromo.newPrice),
                                    color = Ar5asInk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "-${bestPromo.savingPct}%",
                                    color = Ar5asInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("حتى ${bestPromo.ends} — شوف كل العروض", color = Ar5asInk)
                        }
                    }
                }
            }
        }
        item {
            StaggerIn(3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("أرخص قفة (10 منتجات أساسية)", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("أرخص مجموع: ", fontSize = 15.sp)
                            AnimatedNumber(basketCheap, suffix = " د.ت")
                        }
                        Text("أغلى مجموع: ${fmt(basketExp)}")
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("توفّر ", fontWeight = FontWeight.Bold)
                            AnimatedNumber(basketSaving, suffix = " د.ت")
                            Text(" كي تشري الأرخص", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onOpenAddPrice,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ar5asCobalt)
            ) { Text("زيد سوم / صحّح سوم شفتو") }
        }
        item {
            Text("المنتجات", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        // Big photo cards, 2 per row — like the preview grid.
        val pairs = filtered.chunked(2)
        items(pairs.size) { rowIndex ->
            val pair = pairs[rowIndex]
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEachIndexed { colIndex, product ->
                    val cheap = cheapestPrice(store, product)
                    val promo = PROMOS.find { it.productId == product.id }
                    StaggerIn((rowIndex * 2 + colIndex).coerceAtMost(8)) {
                        SpringCard(
                            onClick = { onOpenSheet(product.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box {
                                product.imageRes?.let { res ->
                                    Image(
                                        painter = painterResource(id = res),
                                        contentDescription = product.nameAr,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(Ar5asCobaltTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ShoppingCart,
                                        contentDescription = null,
                                        tint = Ar5asCobalt,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                if (promo != null) {
                                    Text(
                                        "-${promo.savingPct}%",
                                        fontWeight = FontWeight.Bold,
                                        color = Ar5asInk,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Ar5asSunshine, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(product.nameAr, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "${product.nameFr} • ${product.category}",
                                    fontSize = 11.sp,
                                    color = Ar5asInkSoft
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    cheap?.let { fmt(it.second) } ?: "—",
                                    fontWeight = FontWeight.Bold,
                                    color = Ar5asCobalt,
                                    fontSize = 17.sp
                                )
                                Text(cheap?.first ?: "", fontSize = 12.sp, color = Ar5asInkSoft)
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        item {
            Text(
                "الأسعار تقريبية (مثال فقط، نوفمبر 2026) — موش أسعار رسمية. " +
                    "نجّم تبدّل أي سوم من «قارن» و يتحفظ في تليفونك.",
                fontSize = 12.sp,
                color = Ar5asInkSoft
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Product bottom sheet (v0.3, like the preview).
// Tapping a product / promo opens it: photo, sunshine promo strip, all 5
// store prices as staggered animated bars (cheapest in cobalt), add to
// basket with confirmation, and a full-compare option.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(
    product: Product,
    store: PriceStore,
    quantities: SnapshotStateMap<String, Int>,
    onDismiss: () -> Unit,
    onGoToCompare: (String) -> Unit
) {
    val promo = PROMOS.find { it.productId == product.id }
    val cheap = cheapestPrice(store, product)
    val exp = expensivePrice(store, product)
    var added by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(product.nameAr, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "سكّر")
                    }
                }
                Text(
                    "${product.nameFr} • ${product.unit} • ${product.category}",
                    fontSize = 13.sp,
                    color = Ar5asInkSoft
                )
            }
            item {
                product.imageRes?.let { res ->
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = product.nameAr,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            if (promo != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Ar5asSunshine, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "عرض في ${promo.store}: ${fmt(promo.oldPrice)} ← ${fmt(promo.newPrice)} " +
                                "(-${promo.savingPct}%) — حتى ${promo.ends}",
                            fontWeight = FontWeight.Bold,
                            color = Ar5asInk
                        )
                    }
                }
            }
            item {
                Text("الأسعار في المتاجر", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            STORES.forEachIndexed { index, storeName ->
                val price = store.priceOf(product, storeName)
                val isCheapest = cheap?.first == storeName
                val fraction = if (price != null && exp != null && exp.second > 0) {
                    (price / exp.second).toFloat()
                } else {
                    0f
                }
                item {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    storeName,
                                    fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCheapest) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "الأرخص ✓",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(Ar5asCobalt, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                price?.let { fmt(it) } ?: "—",
                                fontWeight = FontWeight.Bold,
                                color = if (isCheapest) Ar5asCobalt else Ar5asInk,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        PriceBar(
                            fraction = fraction,
                            delayMs = index * 90,
                            color = if (isCheapest) Ar5asCobalt else Ar5asCobaltBar
                        )
                    }
                }
            }
            if (cheap != null && exp != null && cheap.second < exp.second) {
                item {
                    Text(
                        "الفرق بين الأرخص والأغلى: ${fmt(exp.second - cheap.second)}",
                        fontWeight = FontWeight.Bold,
                        color = Ar5asCobalt
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        quantities[product.id] = (quantities[product.id] ?: 0) + 1
                        added = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Ar5asCobalt)
                ) {
                    if (added) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("تزاد للقفة ✓")
                    } else {
                        Text("Ajouter à la قفة")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onGoToCompare(product.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ar5asCobalt)
                ) { Text("قارن الكل") }
                Spacer(Modifier.height(24.dp))
            }
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
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ar5asCobalt,
                    unfocusedBorderColor = Color(0xFFD0D9E6),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("قارن الأسعار", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { showAdd = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ar5asCobalt)
                ) { Text("+ زيد منتج") }
            }
        }
        items(filtered) { product ->
            val cheap = cheapestPrice(store, product)
            SpringCard(
                onClick = { onSelect(product.id) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (product.id == selected?.id) Ar5asCobaltTint else Color.White
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
                        color = Ar5asCobalt,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (selected != null) {
            item {
                val cheap = cheapestPrice(store, selected)
                val exp = expensivePrice(store, selected)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
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
                        Text(
                            "${selected.nameFr} • ${selected.unit} • ${selected.category}",
                            fontSize = 13.sp,
                            color = Ar5asInkSoft
                        )
                        Spacer(Modifier.height(10.dp))
                        STORES.forEachIndexed { index, storeName ->
                            val price = store.priceOf(selected, storeName)
                            val isCheapest = cheap?.first == storeName
                            val isExpensive = exp?.first == storeName
                            val fraction = if (price != null && exp != null && exp.second > 0) {
                                (price / exp.second).toFloat()
                            } else {
                                0f
                            }
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            storeName,
                                            fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isCheapest) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "الأرخص ✓",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(Ar5asCobalt, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                price?.let { fmt(it) } ?: "—",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCheapest) Ar5asCobalt else Ar5asInk,
                                                fontSize = 16.sp
                                            )
                                            if (price != null && exp != null && !isExpensive) {
                                                Text(
                                                    "أرخص بـ ${fmt(exp.second - price)} من الأغلى",
                                                    fontSize = 11.sp,
                                                    color = Ar5asInkSoft
                                                )
                                            } else if (isExpensive) {
                                                Text(
                                                    "الأغلى",
                                                    fontSize = 11.sp,
                                                    color = Ar5asInkSoft
                                                )
                                            }
                                        }
                                        IconButton(onClick = { editTarget = selected to storeName }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "عدّل السوم")
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                PriceBar(
                                    fraction = fraction,
                                    delayMs = index * 90,
                                    color = if (isCheapest) Ar5asCobalt else Ar5asCobaltBar
                                )
                            }
                        }
                        if (cheap != null && exp != null && cheap.second < exp.second) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "الفرق بين الأرخص والأغلى: ${fmt(exp.second - cheap.second)}",
                                fontWeight = FontWeight.Bold,
                                color = Ar5asCobalt
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
            Button(
                onClick = {
                    val v = text.toDoubleOrNull()
                    if (v == null || v <= 0) error = true else onSave(v)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ar5asCobalt)
            ) { Text("حفظ") }
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
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Ar5asCobalt,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text("المتجر:", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(STORES) { s ->
                        FilterChip(
                            selected = storeName == s,
                            onClick = { storeName = s },
                            label = { Text(s) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Ar5asCobalt,
                                selectedLabelColor = Color.White
                            )
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
            Button(
                onClick = {
                    val v = priceText.toDoubleOrNull()
                    if (name.isBlank() || v == null || v <= 0) error = true
                    else onSave(name.trim(), category, storeName, v)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ar5asCobalt)
            ) { Text("زيد") }
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
fun PromoScreen(onOpenSheet: (String) -> Unit) {
    val promos = PROMOS.sortedByDescending { it.savingPct }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("عروض الأسبوع", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "أسعار تقريبية — مثال فقط. تحقّق من السوم في المتجر قبل ما تشري.",
                fontSize = 12.sp,
                color = Ar5asInkSoft
            )
        }
        items(promos) { promo ->
            SpringCard(onClick = { onOpenSheet(promo.productId) }, modifier = Modifier.fillMaxWidth()) {
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
                        Text(promo.store, fontSize = 13.sp, color = Ar5asInkSoft)
                        Text("حتى ${promo.ends}", fontSize = 12.sp, color = Ar5asInkSoft)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                fmt(promo.oldPrice),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                fmt(promo.newPrice),
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Ar5asCobalt
                            )
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Ar5asSunshine)) {
                        Text(
                            "-${promo.savingPct}%",
                            fontWeight = FontWeight.Bold,
                            color = Ar5asInk,
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
// Budget / القفة — totals tween smoothly, steppers pop on change.
// ---------------------------------------------------------------------------
@Composable
fun BudgetScreen(
    store: PriceStore,
    products: List<Product>,
    quantities: SnapshotStateMap<String, Int>
) {
    var budgetText by remember { mutableStateOf("") }

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
    val cheapAnimated by animateFloatAsState(
        targetValue = cheapTotal.toFloat(),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "cheapTotal"
    )
    val savingAnimated by animateFloatAsState(
        targetValue = saving.toFloat(),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "saving"
    )

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
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ar5asCobalt,
                    unfocusedBorderColor = Color(0xFFD0D9E6),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "مجموع القفة (بأرخص الأسعار): ${"%.2f".format(cheapAnimated)} د.ت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text("بأغلى الأسعار: ${fmt(expTotal)} — توفّر ${"%.2f".format(savingAnimated)} د.ت")
                    if (budget > 0) {
                        if (remaining >= 0) {
                            Text(
                                "يبقى لك: ${fmt(remaining)} ✓",
                                fontWeight = FontWeight.Bold,
                                color = Ar5asCobalt
                            )
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
                            },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                checkedColor = Ar5asCobalt
                            )
                        )
                        Column {
                            Text(product.nameAr, fontWeight = FontWeight.Bold)
                            Text(
                                cheap?.let { "${fmt(it.second)} • ${it.first}" } ?: "—",
                                fontSize = 12.sp,
                                color = Ar5asCobalt
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (qty > 0) quantities[product.id] = qty - 1 },
                            modifier = Modifier.size(36.dp)
                        ) { Icon(Icons.Filled.Remove, contentDescription = "نقّص") }
                        key(qty) {
                            val pop = remember { Animatable(0.6f) }
                            LaunchedEffect(Unit) {
                                pop.animateTo(
                                    1f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            Text(
                                "$qty",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(24.dp)
                                    .graphicsLayer {
                                        scaleX = pop.value
                                        scaleY = pop.value
                                    },
                                textAlign = TextAlign.Center
                            )
                        }
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
