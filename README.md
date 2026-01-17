# Expense Manager

A comprehensive daily expense manager Android application built with modern Android development practices using Kotlin, Jetpack Compose, and Material Design 3.

## Features

### 1. Track Daily Expenses via SMS
- Automatically detects and parses bank/credit card SMS messages
- Supports major Indian banks (HDFC, SBI, ICICI, Axis, Kotak, etc.)
- Extracts transaction amount, merchant, and payment method from SMS
- Option to manually import transactions from SMS history

### 2. Manual Expense Entry
- Add expenses with detailed information
- Support for past dates (backdating transactions)
- Easy-to-use form with tag suggestions

### 3. Income/Deposit Tracking
- Track all income sources
- Separate income and expense views
- Balance calculation

### 4. Comprehensive Tagging System

#### A. Payee Tags
- Shopkeeper
- Mart
- Amazon
- Uber
- Other (customizable)

#### B. Category Tags
- Shopping
- Food
- Healthcare
- Insurance
- Loan
- Transportation
- Entertainment
- Utilities
- Salary
- Investment
- Other (customizable)

#### C. Payment Method Tags
- Cash
- Visa Credit Card
- Master Credit Card
- UPI
- Debit Card
- Net Banking
- Other (customizable)

#### D. Status Tags
- Done
- Pending
- InFuture

### 5. Tag Management
- Add new custom tags
- Edit existing tags
- Delete unused tags
- Color coding for better visualization

### 6. Transaction Management
- View all transactions with filtering
- Edit previously added expenses
- Delete transactions
- Filter by type (Income/Expense)
- Date range filtering

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Database
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose
- **Async Operations**: Kotlin Coroutines & Flow
- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/src/main/java/com/expensemanager/
├── data/
│   ├── local/
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # Database entities
│   │   ├── AppDatabase.kt # Room database
│   │   └── Converters.kt  # Type converters
│   └── repository/        # Data repositories
├── di/                    # Dependency injection modules
├── sms/                   # SMS parsing utilities
├── ui/
│   ├── components/        # Reusable UI components
│   ├── navigation/        # Navigation setup
│   ├── screens/           # App screens
│   ├── theme/             # Material theme
│   └── viewmodels/        # ViewModels
└── ExpenseManagerApp.kt   # Application class
```

## Permissions

The app requires the following permissions:

- `READ_SMS` - To read bank transaction SMS
- `RECEIVE_SMS` - To automatically capture incoming transaction SMS

## Building the Project

1. Open the project in Android Studio Hedgehog or later
2. Sync Gradle files
3. Build and run on a device/emulator running Android 10+

```bash
./gradlew assembleDebug
```

## Database Schema

### Transactions Table
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary key |
| amount | Double | Transaction amount |
| description | String | Transaction description |
| date | Long | Unix timestamp |
| type | Enum | EXPENSE or INCOME |
| payeeId | Long? | Foreign key to tags |
| categoryId | Long? | Foreign key to tags |
| paymentMethodId | Long? | Foreign key to tags |
| statusId | Long? | Foreign key to tags |
| isFromSms | Boolean | Auto-detected from SMS |
| smsBody | String? | Original SMS text |

### Tags Table
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary key |
| name | String | Tag name |
| type | Enum | PAYEE, CATEGORY, PAYMENT_METHOD, STATUS |
| color | String? | Hex color code |
| icon | String? | Icon identifier |
| isDefault | Boolean | Pre-defined tag |

## Screenshots

The app includes the following screens:
1. **Dashboard** - Financial summary with recent transactions
2. **Transactions** - List of all transactions with filters
3. **Add/Edit Transaction** - Form for creating/updating transactions
4. **Tags Management** - CRUD operations for all tag types
5. **SMS Import** - Scan and import transactions from SMS

## Supported Banks for SMS Parsing

- HDFC Bank
- State Bank of India (SBI)
- ICICI Bank
- Axis Bank
- Kotak Mahindra Bank
- Punjab National Bank
- Bank of India
- Union Bank
- Canara Bank
- Indian Overseas Bank
- Standard Chartered
- Yes Bank
- IndusInd Bank
- RBL Bank
- Federal Bank
- American Express
- Paytm Payments Bank
- And any other bank using standard SMS format

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
