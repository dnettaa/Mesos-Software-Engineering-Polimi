# Mesos - Software Engineering 2026

Implementation of the board game **Mesos** for the Software Engineering course, Politecnico di Milano, A.Y. 2025/2026.

## 1. Group Members

- Luca Grecchi
- Vadym Kitsul
- Diana Francesconi
- Andrea Markvukaj

## 2. Implemented Features

### Complete Game Rules
The full rules of Mesos are implemented for **2–5 players**. The game lasts **10 rounds**, until the Tribù deck is exhausted. Each round consists of the following phases in sequence:

1. **Totem Placement**: players place their totem on an offer slot, in turn order
2. **Offer Resolution**: players take available cards according to the totem order on the track (left to right)
3. **Extra Card**: players with an `ExtraPick` building may take one additional card from the upper row after all totems return to the turn order track
4. **Event Resolution**: effects of active Event cards in the lower row are resolved (Hunt, Sustenance, Shamanic Ritual, Cave Paintings)
5. **End Round**: Character and Event cards are discarded from the lower row; remaining upper-row cards shift down; the upper row is replenished from the deck; a new Era begins if a card from the next era is revealed

At the end of the 10th round, final scoring is calculated:
- PP from **Builders** in the tribe
- PP from the count of **Inventors** × the number of distinct invention icons on their cards
- 10 PP for every 2 **Artists** in the tribe
- PP printed on **Building** cards, plus any end-game building effects

The player with the most Prestige Points wins. Ties are broken by Food; if still tied, victory is shared.

Three card types are implemented:
- **Character Cards**: 6 subtypes (Inventor, Gatherer, Shaman, Builder, Artist, Hunter), each with distinct in-game effects and end-game scoring contributions
- **Building Cards**: 14 building types across 3 eras (21 cards total), each with a Food cost and distinct effects
- **Event Cards**: 4 global effects resolved at the end of each round (Hunt, Sustenance, Shamanic Ritual, Cave Paintings), plus 2 special **Final Event** cards placed at the bottom of the Tribù deck that trigger in the last rounds

### User Interfaces
- **TUI** (Text User Interface): fully functional text interface, playable from any terminal
- **GUI** (Graphical User Interface): graphical interface built with JavaFX 23, with screens: Welcome, Lobby, Game, End Game

### Network Protocols
The client can connect to the server via two alternative protocols, chosen at startup:
- **Socket**: TCP communication with Java object serialization; port `12345`
- **RMI** (Remote Method Invocation): direct remote method calls; port `1099`

Both protocols support all game features transparently through the `VirtualServer` and `VirtualView` interfaces.

### Game Persistence
The server automatically saves the game state to a JSON file (`mesos_save.json`) after **every game action**. The write is atomic: the save is first written to a temporary file (`.tmp`) which is then renamed, avoiding corruption in case of a crash during the write.

On restart, if a valid save exists, the server automatically enters **Recovery mode** and waits for players to reconnect. Clients detect the server disconnection and attempt automatic reconnection for 20 seconds. Once reconnected, they can choose whether to resume the saved game or abandon it.

### Global Leaderboard (optional)
The server supports a persistent global leaderboard on a **PostgreSQL** database. At the end of each game, final scores are saved and the leaderboard is shown to all players ordered by best score, separated by number of players. The feature is **optional**: if the database environment variables are not configured, the server starts normally without the leaderboard.

## 3. Running the Project

### Prerequisites
- **Java 23** or higher (JARs are compiled with Java 23 target)

The precompiled JARs are located in `Mesos/deliverables/final/jar/`:
- `server.jar` - game server
- `client.jar` - client (TUI + GUI)

### Starting the Server

From the root of the repository:

```powershell
java -jar .\Mesos\deliverables\final\jar\server.jar
```

The server starts both the socket (port `12345`) and the RMI registry (port `1099`). The save file `mesos_save.json` is created in the directory from which the server is launched.

### Starting the Client (TUI)

From the root of the repository:

```powershell
java -jar .\Mesos\deliverables\final\jar\client.jar
```

At startup the client asks to choose the interface (TUI/GUI) and the protocol (Socket/RMI). When asked for the server IP, press Enter to use `localhost`.

### Starting the Client (GUI)

The same command above allows choosing the GUI as well: JavaFX is already bundled in the JAR, no additional installation is required.

### Database Configuration (leaderboard - optional)

The leaderboard requires a PostgreSQL database, either local or remote (e.g. Neon, Supabase, Railway). The server reads the configuration from the following environment variables:

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC URL of the database | `jdbc:postgresql://localhost:5432/mesos` |
| `DB_USER` | PostgreSQL user | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `password` |

If one or more variables are not set, the server prints a warning and starts anyway **without the leaderboard**.

#### 0. Prerequisites

If you want to use a **local database**, PostgreSQL must be installed on your machine (`createdb` and `pg_restore` are bundled with it). For a **remote database** (Neon, Supabase, Railway), no local installation is needed.

#### 1. Create the schema on your database

The dump is located at `Mesos/deliverables/final/dump/database.dump`. It contains both the `match_results` table schema and the data from previously recorded games.

**Local database:**
```bash
createdb -U <user> mesos
pg_restore -U <user> -d mesos Mesos/deliverables/final/dump/database.dump
```

**Remote database (e.g. Neon, Supabase, Railway):**
```bash
pg_restore -U <user> -d <database_name> -h <host> -p <port> Mesos/deliverables/final/dump/database.dump
```

The schema creates the `match_results` table:

```sql
CREATE TABLE match_results (
    id           SERIAL PRIMARY KEY,
    nickname     VARCHAR(50),
    final_score  INTEGER,
    player_count INTEGER,
    played_at    TIMESTAMP
);
```

#### 2. Set environment variables and start the server

The `DB_URL` format depends on where the database is located:
- **Local:** `jdbc:postgresql://localhost:5432/mesos`
- **Remote:** `jdbc:postgresql://<host>:<port>/<database_name>`
- **Cloud (e.g. Neon):** `jdbc:postgresql://<host>/<database_name>?sslmode=require`

From the root of the repository, in PowerShell:

```powershell
$env:DB_URL      = "jdbc:postgresql://localhost:5432/mesos"
$env:DB_USER     = "postgres"
$env:DB_PASSWORD = "password"
java -jar .\Mesos\deliverables\final\jar\server.jar
```

### Port Summary

| Protocol | Port |
|---|---|
| Socket | `12345` |
| RMI | `1099` |

To connect the client to a remote server, enter the server IP when prompted at client startup (instead of pressing Enter for `localhost`). Make sure ports `12345` and `1099` are reachable.
