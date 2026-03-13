# <p align="center">RivalCore</p>

> UHC-style last man standing plugin for **Paper 1.21.1**

<p align="center">
  <img src="https://i.imgur.com/8wntLVV.png" alt="RivalCore Logo"/>
</p>

---

## Panoramica

RivalCore è un plugin PvP competitivo stile UHC con squadre (RED vs BLU), fasi progressive, timer, world border dinamico e una UI ricca (BossBar, sidebar, tab HP, chat formattata). Lo stato della partita persiste tra i riavvii grazie a un database H2 embedded.

---

## Funzionalità

### Fasi di gioco

La partita dura **120 minuti** ed è divisa in 4 fasi automatiche:

| Fase | Minuto | Cosa succede |
|------|--------|--------------|
| **Initial** | 0–30 | Respawn consentito, PvP libero, warm-up |
| **Phase Two** | 30–60 | Morte permanente attiva, niente respawn |
| **Phase Three** | 60–90 | Squadre rivelate pubblicamente |
| **Final** | 90–120 | Deathmatch finale |
| **Ended** | 120+ | PvP disabilitato, tutti teletrasportati allo spawn |

> Modalità **debug**: tutte le fasi compresse a 1 minuto per testare rapidamente.

### BossBar

![BossBar](https://i.imgur.com/oyuIfVL.png)
![BossBar](https://i.imgur.com/oRFG5dq.png)
![BossBar](https://i.imgur.com/8mUTEWJ.png)
![BossBar](https://i.imgur.com/2usLzLX.png)

Barra sempre visibile con fase corrente, tempo rimasto e colore per fase:
- **Verde** → Initial &nbsp; **Giallo** → Phase Two &nbsp; **Rosso** → Phase Three &nbsp; **Rosa** → Final

### Sidebar — Kill Leaderboard

![Sidebar](https://i.imgur.com/K1THY2F.png)

Mostra la top 3 dei giocatori con più kill, aggiornata in tempo reale.

### Tab list con HP

![Tab HP](https://i.imgur.com/YK2bx9A.png)

Lista giocatori con salute corrente, prefisso squadra colorato e allineamento pixel-perfect.

### Chat formattata con squadre

![Chat](https://i.imgur.com/DGLNwy6.png)

Prima della rivelazione: `NomeGiocatore: messaggio`
Dopo la rivelazione: `[ROSSO] NomeGiocatore: messaggio`

### Squadre RED vs BLU

![Team Reveal](https://i.imgur.com/2bsIf8m.png)

- Assegnazione casuale 50/50 a inizio partita
- Actionbar privata per 15 secondi all'avvio
- Rivelazione pubblica automatica al minuto 90 (o manuale con `/teams show`)
- **Friendly fire bloccato** dopo la rivelazione
- Prefissi colorati nei name tag e in chat

### Eliminazione & Spettatori

![Spectator](https://i.imgur.com/jCZnWUk.png)

- **Initial**: morte = respawn allo spawn
- **Phase Two+**: morte = eliminazione permanente in modalità Spettatore
- Gli eliminati restano in partita come osservatori
- Un admin può fare `/respawn <giocatore>` per ripristinarlo

### World Border dinamico

Border che si restringe progressivamente:

| Fase | Da → A |
|------|--------|
| Phase 1 | 1200 → 800 blocchi |
| Phase 2 | 800 → 600 blocchi |
| Phase 3 | 600 → 200 blocchi |

---

## Comandi

| Comando | Permesso | Descrizione |
|---------|----------|-------------|
| `/start` | `rivalcore.command.start` | Avvia la partita |
| `/game status` | `rivalcore.command.game` | Mostra fase e tempo rimasto |
| `/game stop` | `rivalcore.command.game` | Termina la partita |
| `/teams show` | `rivalcore.command.teams.show` | Rivela le squadre pubblicamente |
| `/teams list` | `rivalcore.command.teams.show` | Lista giocatori per squadra |
| `/teams warn` | `rivalcore.command.teams.show` | Ricorda la squadra via actionbar |
| `/pvp enable` | `rivalcore.command.pvp.enable` | Abilita PvP globale |
| `/pvp disable` | `rivalcore.command.pvp.disable` | Disabilita PvP globale |
| `/setspawn` | `rivalcore.command.setspawn` | Imposta spawn alla posizione attuale |
| `/respawn <giocatore>` | `rivalcore.command.respawn` | Ripristina un giocatore eliminato |
| `/rivalcore reload` | `rivalcore.command.reload` | Ricarica config.yml |

> Tutti i permessi sono riservati agli **OP** di default.

---

## Installazione

1. Scarica `RivalCore.jar` dalla [sezione Releases](https://github.com/Indifferenzah/RivalCore/releases)
2. Metti il file nella cartella `plugins/` del tuo server Paper 1.21.1
3. Riavvia il server — verrà generato `plugins/RivalCore/config.yml`
4. Configura a piacere e usa `/setspawn` per impostare lo spawn
5. Avvia con `/start`

**Requisiti**: Paper 1.21.1+, Java 21+

---

## Configurazione

Il file `config.yml` è ampiamente documentato. I principali parametri:

```yaml
settings:
  debug: false                          # true = fasi da 1 minuto (test)
  total-match-minutes: 120
  auto-reveal-teams-at-90min: true
  default-pvp-enabled: true
  assign-late-joiners-to-smaller-team: false

border:
  enabled: true
  phase-1-start: 1200.0
  phase-1-end: 800.0
  # ...

tab:
  hp-enabled: true
  hp-format: '%team%%name%&cHP: &4%hp%'

scoreboard:
  title: '&6&lRivalUHC'
```

---

## Build da sorgente

```bash
git clone https://github.com/indifferenzah/RivalUHC.git
cd RivalUHC
mvn clean package
# Output: target/RivalCore.jar
```

Richiede Java 21+ e Maven.

---

## Autore

Made by **indifferenzah**
