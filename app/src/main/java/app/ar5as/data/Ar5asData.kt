package app.ar5as.data

import android.content.Context
import android.content.SharedPreferences

// ---------------------------------------------------------------------------
// Ar5as v0.1 — data
// IMPORTANT: all prices below are INDICATIVE example prices (Nov 2026 ranges),
// clearly labelled in the app as "أسعار تقريبية". They are NOT live official
// prices. Every price can be corrected by the user ("زيد سوم") and the
// correction is saved on-device (SharedPreferences). No backend, no scraping.
// ---------------------------------------------------------------------------

val STORES = listOf("Carrefour", "Monoprix", "Aziza", "Magasin Général", "Souk")

val CATEGORIES = listOf(
    "ماكلة",        // Épicerie
    "خضرة وغلّة",   // Fruits & légumes
    "لحم ودجاج",    // Viande & volaille
    "حليب وجبن",    // Lait & fromage
    "زيت وسكّر",    // Huile & sucre
    "نظافة"         // Hygiène & nettoyage
)

data class Product(
    val id: String,
    val nameAr: String,
    val nameFr: String,
    val unit: String,
    val category: String,
    /** Price per store, in the STORES order. Null = no price for that store. */
    val prices: Map<String, Double?>
)

data class Promo(
    val productAr: String,
    val store: String,
    val oldPrice: Double,
    val newPrice: Double,
    val ends: String // e.g. "2026-11-30"
) {
    val savingPct: Int get() = ((1.0 - newPrice / oldPrice) * 100).toInt()
}

fun fmt(price: Double): String = "%.2f".format(price) + " د.ت"

private fun p(
    id: String, nameAr: String, nameFr: String, unit: String, category: String,
    carrefour: Double, monoprix: Double, aziza: Double, mg: Double, souk: Double
): Product = Product(
    id, nameAr, nameFr, unit, category,
    mapOf(
        "Carrefour" to carrefour,
        "Monoprix" to monoprix,
        "Aziza" to aziza,
        "Magasin Général" to mg,
        "Souk" to souk
    )
)

/** Seed catalogue — 27 products, indicative prices in DT. */
val SEED_PRODUCTS: List<Product> = listOf(
    // زيت وسكّر
    p("zit-nabati", "زيت نباتي 1ل", "Huile végétale 1L", "1 ل", "زيت وسكّر", 7.80, 8.20, 7.50, 8.00, 7.20),
    p("sokkor", "سكّر 1كغ", "Sucre 1kg", "1 كغ", "زيت وسكّر", 1.50, 1.55, 1.45, 1.50, 1.40),
    p("zit-zitouna", "زيت زيتونة 1ل", "Huile d'olive 1L", "1 ل", "زيت وسكّر", 19.50, 21.00, 18.90, 20.50, 17.50),
    // ماكلة
    p("ma9rouna", "مقرونة 500غ", "Pâtes 500g", "500 غ", "ماكلة", 1.20, 1.35, 1.10, 1.25, 1.00),
    p("riz", "روز 1كغ", "Riz 1kg", "1 كغ", "ماكلة", 3.80, 4.10, 3.50, 3.90, 3.20),
    p("farina", "فرينة 1كغ", "Farine 1kg", "1 كغ", "ماكلة", 1.80, 1.95, 1.70, 1.85, 1.60),
    p("couscous", "كسكسي 1كغ", "Couscous 1kg", "1 كغ", "ماكلة", 2.60, 2.85, 2.40, 2.70, 2.20),
    p("3das", "عدس 1كغ", "Lentilles 1kg", "1 كغ", "ماكلة", 4.20, 4.60, 3.90, 4.30, 3.60),
    p("9ahwa", "قهوة 250غ", "Café 250g", "250 غ", "ماكلة", 8.90, 9.50, 8.40, 9.10, 8.00),
    p("chay", "شاي 200غ", "Thé 200g", "200 غ", "ماكلة", 3.40, 3.70, 3.10, 3.50, 2.90),
    p("tmatem-m3ajna", "طماطم معجونة 400غ", "Tomate concentrée 400g", "400 غ", "ماكلة", 2.30, 2.50, 2.10, 2.40, 1.90),
    // خضرة وغلّة
    p("tmatem", "طماطم 1كغ", "Tomates 1kg", "1 كغ", "خضرة وغلّة", 2.40, 2.80, 2.20, 2.60, 1.80),
    p("batata", "بطاطا 1كغ", "Pommes de terre 1kg", "1 كغ", "خضرة وغلّة", 1.90, 2.20, 1.70, 2.00, 1.40),
    p("bsal", "بصل 1كغ", "Oignons 1kg", "1 كغ", "خضرة وغلّة", 1.60, 1.90, 1.40, 1.70, 1.10),
    p("tfe7", "تفاح 1كغ", "Pommes 1kg", "1 كغ", "خضرة وغلّة", 5.50, 6.20, 5.00, 5.80, 4.50),
    p("banan", "موز 1كغ", "Bananes 1kg", "1 كغ", "خضرة وغلّة", 6.80, 7.50, 6.40, 7.00, 5.90),
    // لحم ودجاج
    p("djej", "دجاج 1كغ", "Poulet 1kg", "1 كغ", "لحم ودجاج", 9.80, 10.50, 9.20, 10.10, 8.90),
    p("l7am-3alouch", "لحم علوش 1كغ", "Agneau 1kg", "1 كغ", "لحم ودجاج", 38.00, 42.00, 37.00, 40.00, 34.00),
    p("l7am-ba9ri", "لحم بقري 1كغ", "Bœuf 1kg", "1 كغ", "لحم ودجاج", 32.00, 35.00, 31.00, 33.50, 29.00),
    p("bidh", "بيض 30 حبة", "Œufs x30", "30 حبة", "لحم ودجاج", 14.50, 15.50, 13.90, 15.00, 13.00),
    // حليب وجبن
    p("7lib", "حليب 1ل", "Lait 1L", "1 ل", "حليب وجبن", 1.60, 1.65, 1.55, 1.60, 1.50),
    p("jben", "جبن 200غ", "Fromage 200g", "200 غ", "حليب وجبن", 5.20, 5.80, 4.90, 5.40, 4.60),
    p("yaourt", "ياغورت", "Yaourt", "1", "حليب وجبن", 0.90, 1.00, 0.85, 0.95, 0.80),
    // نظافة
    p("saboun", "مسحوق غسيل 1كغ", "Lessive 1kg", "1 كغ", "نظافة", 9.50, 10.50, 8.90, 10.00, 8.50),
    p("javel", "ماء جافيل 1ل", "Javel 1L", "1 ل", "نظافة", 1.80, 2.00, 1.60, 1.90, 1.40),
    p("papier", "ورق تواليت 4", "Papier toilette x4", "4", "نظافة", 3.60, 4.00, 3.30, 3.80, 3.00),
    p("dentifrice", "معجون أسنان", "Dentifrice", "1", "نظافة", 4.80, 5.50, 4.40, 5.00, 4.10)
)

/** This week's promos — indicative examples, editable like any price. */
val PROMOS: List<Promo> = listOf(
    Promo("دجاج 1كغ", "Carrefour", 10.50, 8.90, "2026-11-30"),
    Promo("مسحوق غسيل 1كغ", "Carrefour", 10.50, 8.50, "2026-12-03"),
    Promo("قهوة 250غ", "Monoprix", 9.50, 7.90, "2026-12-01"),
    Promo("روز 1كغ", "Magasin Général", 3.90, 3.30, "2026-11-30"),
    Promo("زيت نباتي 1ل", "Aziza", 7.50, 6.90, "2026-11-29"),
    Promo("حليب 1ل", "Aziza", 1.55, 1.40, "2026-11-28"),
    Promo("بيض 30 حبة", "Souk", 13.00, 11.50, "2026-11-27"),
    Promo("تفاح 1كغ", "Souk", 4.50, 3.80, "2026-11-26")
)

/** Default weekly basket shown on Accueil (10 staples). */
val DEFAULT_BASKET_IDS = listOf(
    "zit-nabati", "sokkor", "ma9rouna", "riz", "7lib",
    "djej", "tmatem", "batata", "bidh", "9ahwa"
)

// ---------------------------------------------------------------------------
// On-device price store (SharedPreferences). Overrides seed prices and
// stores user-added products. No account, no network, no backend.
// ---------------------------------------------------------------------------
class PriceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ar5as_prefs", Context.MODE_PRIVATE)

    /** Effective price: user override wins, else seed price (may be null). */
    fun priceOf(product: Product, store: String): Double? {
        val key = "ov_${product.id}_$store"
        return if (prefs.contains(key)) prefs.getFloat(key, 0f).toDouble()
        else product.prices[store]
    }

    fun setPrice(productId: String, store: String, price: Double) {
        prefs.edit().putFloat("ov_${productId}_$store", price.toFloat()).apply()
    }

    /** User-added products, encoded one per line: name¦category¦store¦price */
    fun customProducts(): List<Product> {
        val raw = prefs.getString("custom_products", "") ?: ""
        if (raw.isBlank()) return emptyList()
        val out = mutableListOf<Product>()
        raw.lines().forEachIndexed { index, line ->
            try {
                val parts = line.split("¦")
                if (parts.size != 4) return@forEachIndexed
                val price = parts[3].toDouble()
                val prices = STORES.associateWith { s -> if (s == parts[2]) price else null }
                out.add(
                    Product(
                        id = "custom-$index",
                        nameAr = parts[0],
                        nameFr = parts[0],
                        unit = "1",
                        category = parts[1],
                        prices = prices
                    )
                )
            } catch (_: Exception) {
                // Skip a malformed saved line, never crash on user data.
            }
        }
        return out
    }

    fun addCustomProduct(nameAr: String, category: String, store: String, price: Double) {
        val existing = prefs.getString("custom_products", "") ?: ""
        val line = listOf(nameAr, category, store, price.toString()).joinToString("¦")
        val updated = if (existing.isBlank()) line else "$existing\n$line"
        prefs.edit().putString("custom_products", updated).apply()
    }
}

/** Cheapest / most expensive store price for a product (ignores missing prices). */
fun cheapestPrice(store: PriceStore, product: Product): Pair<String, Double>? =
    STORES.mapNotNull { s -> store.priceOf(product, s)?.let { s to it } }.minByOrNull { it.second }

fun expensivePrice(store: PriceStore, product: Product): Pair<String, Double>? =
    STORES.mapNotNull { s -> store.priceOf(product, s)?.let { s to it } }.maxByOrNull { it.second }
