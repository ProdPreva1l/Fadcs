CREATE TABLE IF NOT EXISTS chest_shops (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    location TEXT NOT NULL,
    owner TEXT NULLABLE,
    adminShop INTEGER NOT NULL,
    listings TEXT NOT NULL
);