# TripStack — Multi-Event Travel & Budget Planner

**Student:** Zene Beaufort

## Project Description

TripStack is a JavaFX desktop application that helps you plan and save for
several trips or events at the same time — a cruise, a concert, a theme
park visit — instead of tracking each one separately in notes or
spreadsheets. Every trip has its own cost estimate, savings goal, and
deadline. You log deposits toward each trip, itemize its expenses, and the
dashboard flags any trip that's falling behind the savings pace it needs
to hit its goal on time.

Data is stored locally in a single SQLite file (`tripstack.db`) created
automatically the first time the app runs.

## Features

- **Trips tab** — Create, view, edit, and delete trips (name, category,
  location, trip date, total cost, savings goal, deadline, optional
  "compare group").
- **Savings tab** — Log deposits toward a selected trip; see a running
  total saved vs. goal with a progress bar.
- **Expenses tab** — Itemize costs per trip (deposit, tickets, hotel,
  flights, transportation, spending money, other) instead of one lump sum.
- **Dashboard tab** — All trips sorted by upcoming deadline, with days
  remaining, amount saved, and a status flag (On track / Behind pace /
  Funded / Past deadline).
- **Compare tab** — Group two or more candidate trips under the same
  "Compare Group" name (e.g., the same concert in two different cities)
  and view them side by side, then archive the ones you didn't pick.
- **Input validation & exception handling** — Required fields, no past
  deadlines, no negative or non-numeric amounts; database errors are
  shown as user-facing alerts instead of crashing the app.

## Technologies Used

- Java 17
- JavaFX (`javafx-controls`)
- SQLite via the `sqlite-jdbc` JDBC driver
- Maven (`javafx-maven-plugin` for running the app)

## Project Structure

```
tripstack/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/java/com/tripstack/
    ├── MainApp.java              Application entry point
    ├── model/                    Trip, Deposit, Expense
    ├── dao/                      DatabaseManager, TripDAO, DepositDAO, ExpenseDAO
    ├── exception/                ValidationException, DataAccessException
    ├── ui/                       DashboardTab, TripsTab, SavingsTab, ExpensesTab, CompareTab
    └── util/                     AlertUtil
```

## How to Compile and Run

### Requirements

- JDK 17 or later
- Maven 3.8+
- Internet access on first build (Maven downloads JavaFX and the SQLite
  JDBC driver from Maven Central)

### Steps

```bash
# 1. Clone the repository
git clone <your-repository-url>
cd tripstack

# 2. Run the application
mvn clean javafx:run
```

The `tripstack.db` SQLite file is created automatically in the project's
working directory the first time the app runs — no manual setup needed.

### Building a runnable jar (optional)

```bash
mvn clean package
```

## AI Prompts (Optional)

Portions of this project's boilerplate (DAO structure, JavaFX form
layout, and this README) were scaffolded with the help of Claude, then
reviewed and adjusted by the student.
