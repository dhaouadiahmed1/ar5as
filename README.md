# Ar5as — أرخص v0.2

**Ar5as** (أرخص = "le moins cher") aide les Tunisiens à trouver les prix les plus bas
et les promos des produits de tous les jours — et à tenir leur budget.

- Package : `app.ar5as` — versionCode 2 / versionName **0.2**
- Kotlin + Jetpack Compose (Material3), minSdk 26
- **100% hors-ligne, sans compte, sans pub, sans backend.**

## Nouveautés v0.2 (2026-11-19)
- **Thème clair forcé** — fond blanc, cartes blanches, vert #1B7A43 + ambre #FFB300,
  comme l'exemple web approuvé (le mode sombre du téléphone est ignoré).
- **Photos réelles des produits** — les 27 produits ont une vraie photo
  (Pixabay, licence gratuite, images téléchargées et réduites à max 400px,
  ~5–55 Ko chacune, embarquées dans l'app).
  Miniatures 64dp dans Accueil + Comparer, grande photo dans la fiche produit,
  miniature dans les cartes Promo, 48dp dans le Budget. Les produits ajoutés
  par l'utilisateur affichent une icône générique (pas de photo).
  > Stockage : les photos sont dans le repo en **base64 texte**
  > (`app/images-b64/*.b64`) — le push snapshot de l'app GitHub de Muse
  > n'accepte que des fichiers texte UTF-8, pas de JPG binaires.
  > Elles sont décodées en JPG dans `res/drawable/` au moment du build
  > (`scripts/decode_images.py`, tâche Gradle `decodeImages` sur `preBuild`,
  > ou étape [1b/8] de `build-manual.sh`). Ne pas committer de JPG binaires
  > dans `res/drawable/` — ils sont régénérés à chaque build.
- **Bannière photo sur l'Accueil** — photo d'un étal de marché
  (`res/drawable/hero_market.jpg`, Pixabay) avec le titre par-dessus.

## Ce que fait v0.1

1. **الرئيسية (Accueil)** — recherche, catégories (ماكلة، خضرة وغلّة، لحم ودجاج،
   حليب وجبن، زيت وسكّر، نظافة), meilleure promo du jour, résumé de la « قفة »
   la moins chère (10 produits de base).
2. **قارن (Comparer)** — 27 produits, prix dans 5 enseignes
   (Carrefour, Monoprix, Aziza, Magasin Général, Souk). Le moins cher est
   surligné en vert, avec l'écart par rapport au plus cher.
3. **عروض (Promo)** — 8 promos de la semaine : ancien prix → nouveau prix,
   % d'économie, enseigne, date de fin.
4. **القفة (Budget)** — tu écris ton budget mensuel en DT, tu coches les
   produits + quantités, l'app calcule le total le moins cher, le reste
   (ou le dépassement) et l'économie vs le panier le plus cher.
5. **زيد سوم** — corriger un prix ou ajouter un produit : sauvegardé
   sur le téléphone (SharedPreferences). Aucune donnée ne quitte l'appareil.

## ⚠️ Les prix sont indicatifs

Les prix de départ sont des **exemples plausibles (nov. 2026)** — ex. huile 1L
~7–9 DT, sucre 1kg ~1,50 DT, lait 1L ~1,60 DT, poulet 1kg ~9–11 DT.
Ce ne sont **pas** des prix officiels en direct. L'app l'affiche clairement
(« أسعار تقريبية ») et chaque prix est modifiable.

## Compiler

```bash
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
```
Note : le `gradle-wrapper.jar` (binaire) n'est pas poussé via l'app GitHub de Muse — en CI le workflow utilise Gradle 8.7 système (`gradle assembleDebug`), pas le wrapper. En local sur cette VM, utiliser `./build-manual.sh` (voir ci-dessous).

Prérequis : JDK 17+ et Android SDK (API 34, build-tools 34.0.0).
En CI, le workflow `.github/workflows/android.yml` build l'APK debug.

> Note : l'app GitHub de Muse ne peut pas créer de fichiers workflow via
> l'API (403). Si le repo est créé, AHMED ajoute `android.yml` lui-même —
> le fichier est déjà dans ce dossier, prêt à copier.

### Build local sans Gradle (sandbox)

Sur cette VM, Gradle ne peut pas tourner : le sandbox bloque le TCP brut
(`other_tcp: Deny`) et le protocole du daemon Gradle utilise du TCP loopback
— `./gradlew` échoue avec `Could not receive a message from the daemon`
(`Unexpected type tag 109`). Ce n'est pas un problème du code.
Pour vérifier la compilation localement, `build-manual.sh` fait exactement
les mêmes étapes qu'AGP, avec les mêmes compilateurs (kotlinc 2.0.20 +
plugin Compose 2.0.20, aapt2, d8) :

```bash
./build-manual.sh
# APK : build/manual/ar5as-debug.apk (signé, debug)
```

Kotlin est en **2.0.20** (AGP 8.5.2 + Compose BOM 2024.06.00) — la même
version pour Gradle et pour le build manuel, pour que les deux restent
cohérents.

## Prochaines étapes (pas dans v0.1)

- Prix réels participatifs (crowdsourcing) + historique des prix.
- Alertes quand un produit baisse dans un magasin proche.
- Scanner code-barres, liste de courses partageable.
- Aucun scraping de site dans v0.1 — les prix viennent des utilisateurs.
