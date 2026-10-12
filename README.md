# Smart Pantry Manager

A Java Android app that helps reduce food waste. You keep track of the ingredients you actually have at home, and the app suggests only the recipes you can cook **right now**, using strictly those ingredients. No shopping trip required.

Built for **Mobile App Development 700** (Practical Assignment) at Richfield Graduate Institute of Technology.

- **Author:** Emmanuel Ibekwe
- **Student number:** 402413401
- **Language:** Java 
- **IDE:** Android Studio

---

## What the app does

- **Pantry management:** add, edit and delete ingredients (name, quantity, unit, optional expiry date).
- **Pantry list:** every ingredient shown in a RecyclerView backed by the database.
- **20 pre-loaded recipes:** seeded into the database on first run, each with ingredients and simple preparation steps.
- **Suggested Recipes:** runs the strict-matching rule against your pantry and lists only the recipes you can make right now.
- **Recipe detail:** full ingredient list and method for a chosen recipe.
- **Settings:** a toggle for expiring-soon alerts. When on, pantry items expiring within 3 days (or already expired) are highlighted in red.
- **Helpful empty states:** a clear message when the pantry is empty, and when no recipes match ("No recipes match your pantry yet - add more ingredients").
- **Input validation** on the add/edit form (empty name, name too long, missing quantity, non-numeric quantity, quantity of zero or less).

## The strict-matching rule

A recipe is suggested **only if every single ingredient it needs is in the pantry, in at least the required quantity.** If a recipe needs 5 ingredients and the pantry has 4, it is never shown. There is no "almost there" list.

To cope with simple real-world messiness, matching is not a naive string comparison:

- **Names are normalised:** case and extra spaces are ignored, and simple plurals are handled, so `Tomatoes`, `tomatoes` and `tomato` are the same ingredient.
- **Units are converted:** `kg` to `g`, `l` to `ml`, and `tsp`, `tbsp` and `cup` to `ml`, so 1 kg of rice satisfies a recipe needing 200 g.
- **Duplicate pantry rows are added together:** `Egg` (1) and `eggs` (1) count as 2 eggs.
- **Unit families never mix:** weight, volume and count are separate. 2 whole eggs will not satisfy "100 g egg". This is deliberate, because the app cannot safely convert between them.
- **Recipes with no ingredients are never suggested.**

The logic lives in one plain-Java class, `RecipeMatcher`, with no Android dependencies, so it is covered by fast JVM unit tests (see below).

**Tip for entering your pantry:** use `pcs` for countable items (egg, tomato, onion, potato, garlic, bread slices), `g` or `kg` for solids, and `ml`, `l`, `tsp`, `tbsp` or `cup` for liquids and spoon measures. This matches how the seeded recipes are written.

## Database choice: SQLite (via Room)

I chose **SQLite using the Room persistence library**, implemented locally on the device.

Why Room/SQLite over Firebase or PostgreSQL:

- **Offline and self-contained:** the app is about the user's own pantry, so it works with no internet, no account and no backend to host or keep running.
- **The data is relational:** a recipe has many ingredient lines (one-to-many). Room models this with a foreign key and `CASCADE` delete.
- **Compile-time safety:** Room checks the SQL in the DAO annotations when the project is built, so typos fail early instead of crashing at runtime.
- **Reactive UI:** the pantry DAO returns `LiveData`, so the list updates automatically whenever the data changes.
- **Simple to set up and run:** anyone can clone the repository and run it without credentials or configuration.

### Data model

| Table | Columns |
|-------|---------|
| `pantry_items` | `id` (PK, auto), `name`, `quantity`, `unit`, `expiryDate` (nullable, epoch millis) |
| `recipes` | `id` (PK, auto), `name`, `steps` |
| `recipe_ingredients` | `id` (PK, auto), `recipeId` (FK to `recipes.id`, cascade delete), `ingredientName`, `quantity`, `unit` |

The recipes are inserted by a Room callback the first time the database file is created (`SeedData`). Full CRUD is available on pantry items, and the data persists after the app is closed and reopened. The expiring-soon setting is stored separately in `SharedPreferences`.

## Screens

| Screen | Class | Purpose |
|--------|-------|---------|
| Pantry List | `MainActivity` | Shows all ingredients; **+** button adds one; tap a row to edit |
| Add/Edit Ingredient | `AddEditActivity` | Form with validation, date picker and delete |
| Suggested Recipes | `SuggestedRecipesActivity` | Lists recipes passing the strict-matching rule |
| Recipe Detail | `RecipeDetailActivity` | Ingredients and method for one recipe |
| Settings | `SettingsActivity` | Expiring-soon highlight toggle |

Navigation uses a **bottom navigation bar** (Pantry, Recipes, Settings) plus **Intents** that pass data between screens (an ingredient's fields to the edit screen, a recipe id to the detail screen).

## Project structure

```
app/src/main/java/com/example/smartpantry/
├── MainActivity.java
├── AddEditActivity.java
├── SuggestedRecipesActivity.java
├── RecipeDetailActivity.java
├── SettingsActivity.java
├── PantryAdapter.java
├── RecipeAdapter.java
├── PantryViewModel.java
├── BottomNavHelper.java
└── data/
    ├── AppDatabase.java        (Room database, singleton, seeding callback)
    ├── PantryItem.java         (entity)
    ├── Recipe.java             (entity)
    ├── RecipeIngredient.java   (entity)
    ├── PantryItemDao.java
    ├── RecipeDao.java
    ├── RecipeMatcher.java      (the strict-matching logic)
    └── SeedData.java           (20 pre-loaded recipes)

app/src/main/res/
├── layout/   (activity_*.xml, item_pantry.xml, item_recipe.xml)
└── menu/     (bottom_nav_menu.xml)

app/src/test/java/com/example/smartpantry/data/
└── RecipeMatcherTest.java      (15 unit tests)
```

## Setup and run instructions

### Requirements

- A recent version of **Android Studio** with the Android SDK installed
- **JDK 17** (the JetBrains Runtime bundled with Android Studio works)
- A device or emulator running **Android 8.0 (API 26) or newer**

### Steps

1. **Clone the repository**
   ```
   https://github.com/nue77/SmartPantry-Java-Project.git
   ```
2. **Open the project** in Android Studio (**File > Open**, then select the cloned folder).
3. **Let Gradle sync finish.** If Android Studio reports "Project JDK is not defined", go to **Settings > Build, Execution, Deployment > Build Tools > Gradle** and set **Gradle JDK** to the bundled JDK 17, then sync again.
4. **Choose a device:**
    - **Physical phone (recommended):** enable *Developer options* and *USB debugging*, connect it by USB and accept the debugging prompt.
    - **Emulator:** create a virtual device in *Device Manager* (a lightweight phone profile with an x86_64 Google APIs image works well).
5. **Press Run** (the green play button). The first launch creates the database and loads the 20 recipes.

To build an APK from the command line instead:
```
./gradlew assembleDebug
```
The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Running the unit tests

```
./gradlew testDebugUnitTest
```
Or in Android Studio, right-click `RecipeMatcherTest` and choose **Run 'RecipeMatcherTest'**. No device is needed.

## Testing

- **Unit tests:** 15 JUnit tests cover the strict-matching rule, including the key case that a recipe needing 5 ingredients is excluded when the pantry has only 4, plus singular/plural names, case and whitespace, unit conversion, insufficient quantities, duplicate pantry rows, incompatible unit families and empty inputs.
- **Manual testing:** the full app was tested on a physical Samsung Galaxy A26 (CRUD, validation errors, persistence after closing and reopening, strict matching with ingredients added and removed, recipe detail, empty states, the settings toggle and navigation).

## Known limitations

- Plural handling covers common English patterns, not every irregular plural.
- Descriptive words such as "fresh" or "chopped" are not stripped, so `chopped tomato` and `tomato` are different ingredients.
- Weight, volume and count units are never converted into each other.
- The suggestions refresh when the screen is shown, not live while another screen is open.

## Out of scope (by assignment brief)

No Google Maps, mapping SDKs or GPS/location features, no payment processing, and no Google Play Store publishing.

## Academic integrity

This project is submitted as my own work for Mobile App Development 700. The commit history reflects the incremental development of the app.