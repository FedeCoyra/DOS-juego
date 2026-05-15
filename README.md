# 🃏 Juego DOS — Evaluacíon Final
### Programación Orientada a Objetos · 2025/2026
**Universidad Nacional de Luján**

> **Alumno:** Coyra, Federico
> **Legajo Sistemas:** 182.939 · **Legajo Cs. de Datos:** 199.751

---
## Reglas del Juego

### Composición del Mazo — 108 cartas

| Tipo de carta | Descripción | Cantidad |
|---|---|---|
| Numéricas 1, 3, 4, 5 | 4 colores × 3 copias c/u | 48 |
| Numéricas 6, 7, 8, 9, 10 | 4 colores × 2 copias c/u | 40 |
| Comodín # | 2 por cada color (4 colores) | 8 |
| Comodín DOS | Sin color, valen 2 | 12 |
| **Total** | | **108** |

**Colores:** 🔴 Rojo · 🔵 Azul · 🟢 Verde · 🟡 Amarillo


## Cómo se Juega

2 a 4 jugadores.

En su turno, cada jugador intenta hacer juego con las cartas de su mano respecto de las cartas de la Fila Central.


### Desarrollo del Turno

En cada turno, el jugador puede:

#### Opción 1 — Hacer jugada

**Juego Simple:** jugar **1 carta** de la mano cuyo número coincida con el de una carta de la Fila Central.

```
Mano: [7 ROJO]  →  Fila Central: [7 AZUL]   ✅ coinciden en número
```

**Juego Doble:** jugar **2 cartas** de la mano cuya **suma** coincida con el número de una carta de la Fila Central.

```
Mano: [3 ROJO] + [5 ROJO]  →  Fila Central: [8 VERDE]   ✅ suma = 8
```

> ⭐ **Bono Color:** si la(s) carta(s) jugada(s) coinciden además en **color** con la carta de la Fila Central, el jugador puede **bajar una carta adicional** a la Fila Central.

#### Opción 2 — Robar carta

Si el jugador no puede (o no quiere) hacer jugada, roba **1 carta** del mazo. Luego puede:
- Intentar jugarla inmediatamente (jugada simple o doble).
- Si tampoco puede, debe **bajar 1 carta** de su mano a la Fila Central y pasar el turno.

#### Fin del Turno

Al terminar el turno, la Fila Central siempre debe tener **al menos 2 cartas**. Si tiene menos, se reponen desde el mazo.

### Cartas Especiales

#### Comodín DOS 🃏
- Vale **2** de **cualquier color**.
- Al jugarlo, el jugador elige el color que representará.
- Si está en la Fila Central, el jugador elige el color al usarlo.

#### Comodín # 🃏
- Vale **cualquier número del 1 al 10**.
- El jugador elige el número al momento de tirarlo.
- **Respeta el color** de la carta; no puede elegir color libremente.

### Regla DOS ‼️

> Cuando un jugador tiene **exactamente 2 cartas en su mano**, **debe anunciar "DOS!"** en voz alta.

- Si otro jugador lo nota antes de que lo anuncie, el infractor recibe una **penalización de +2 cartas**.
- Las cartas de castigo se aplican **al finalizar el turno**.

### Fin de la Partida

El primer jugador en **quedarse sin cartas** gana la ronda.


## Cómo Ejecutar la Aplicación

### Opción A — Partida en Red (Multijugador Real)

Esta opción permite que cada jugador corra el cliente en su propia máquina y se conecten a un servidor compartido.

---

#### Paso 1 — Levantar el Servidor

Ejecutar **una sola vez**, en la máquina que actuará como servidor:

```
ServidorDosMain
```

Aparecerán dos diálogos:

| Diálogo | Valor recomendado | Descripción |
|---|---|---|
| **IP del servidor** | Elegir la IP de red local (ej: `192.168.1.100`) | La IP que los clientes usarán para conectarse |
| **Puerto del servidor** | `5000` | Puerto en que escuchará el servidor RMI |

> ✅ Si todo va bien, la consola mostrará: `Servidor DOS levantado en 192.168.1.100:5000`

---

#### Paso 2 — Conectar los Clientes

Ejecutar en **cada máquina jugadora** (incluyendo la del servidor si quiere jugar):

```
ClienteDosMain
```

Aparecerán los siguientes diálogos en orden:

| # | Diálogo | Jugador 1 (Host) | Jugador 2 | Jugador 3 | Jugador 4 |
|---|---|---|---|---|---|
| 1 | **IP del cliente** | Su propia IP | Su propia IP | Su propia IP | Su propia IP |
| 2 | **Puerto del cliente** | `5001` | `5002` | `5003` | `5004` |
| 3 | **IP del servidor** | `192.168.1.100` | `192.168.1.100` | `192.168.1.100` | `192.168.1.100` |
| 4 | **Puerto del servidor** | `5000` | `5000` | `5000` | `5000` |
| 5 | **Índice del jugador** | `0` | `1` | `2` | `3` |
| 6 | **¿Es host?** | ✅ SÍ | ❌ NO | ❌ NO | ❌ NO |
| 7 | **Cantidad de jugadores** | `2` / `3` / `4` | *(no aparece)* | *(no aparece)* | *(no aparece)* |
| 8 | **Tipo de vista** | Gráfica / Consola | Gráfica / Consola | Gráfica / Consola | Gráfica / Consola |

> ⚠️ **Importante:** el diálogo "¿Es host?" sólo lo debe marcar **el primer jugador** (índice 0). Este jugador es quien inicia la partida y define la cantidad de jugadores.

> ⚠️ **El Jugador 1 debe conectarse último**, después de que todos los demás clientes ya estén conectados. La partida se inicia automáticamente cuando el host llama a `iniciarNuevaPartida()`.

---

#### Orden correcto de conexión para partida de 4 jugadores

```
1. Levantar ServidorDosMain  →  puerto 5000
2. Conectar Cliente J3       →  índice 3, puerto 5003, NO es host
3. Conectar Cliente J2       →  índice 2, puerto 5002, NO es host
4. Conectar Cliente J1       →  índice 1, puerto 5001, NO es host
5. Conectar Cliente J0       →  índice 0, puerto 5001*, SÍ es host, cantJugadores=4
   → La partida arranca automáticamente
```

*\*En la misma máquina que el servidor, usar una IP diferente o 127.0.0.1 como IP de cliente.*

---
