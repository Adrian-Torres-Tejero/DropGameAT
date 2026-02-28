# Space Collector

## Introducción

Space Collector es un juego 2D desarrollado con libGDX en Java. El jugador maneja una nave espacial que tiene que recoger cristales que caen desde arriba. Hay dos tipos: cristales normales (1 punto) y cristales dorados (3 puntos, pero caen más rápido). Si se te escapan 3 cristales al suelo, el juego termina.

---

## Desarrollo

### Lógica

**Movimiento:** La nave se mueve con las flechas izquierda y derecha. Se usa delta time para que la velocidad sea la misma en cualquier ordenador:
```java
ship.x -= 400 * delta;
```

**Colisiones:** Cada objeto tiene un `Rectangle` invisible. Cuando el rectángulo de la nave toca el de un cristal, lo recoge. Esto se llama AABB (Axis-Aligned Bounding Box):
```java
if (rect.overlaps(ship)) { score++; }
```

**Dificultad progresiva:** Con cada cristal recogido, la velocidad de caída aumenta un poco y los cristales aparecen más seguido, hasta un límite máximo.

**Sonido:** Al recoger un cristal suena un efecto de audio cargado con `Gdx.audio.newSound()`.

### Estructura

El juego usa la arquitectura de pantallas de libGDX:

| Clase | Función |
|-------|---------|
| `SpaceGame` | Clase principal, gestiona pantallas y recursos compartidos |
| `MenuScreen` | Pantalla de inicio con instrucciones |
| `GameScreen` | Lógica del juego: movimiento, cristales, colisiones y puntuación |
| `DesktopLauncher` | Lanzador para PC, configura la ventana |

---

## Conclusiones

Lo más importante que aprendí fue la diferencia entre la representación lógica y la gráfica. El juego no "ve" las imágenes para detectar colisiones, sino que trabaja con rectángulos matemáticos (`Rectangle`) que no se ven en pantalla. Las texturas son solo para mostrar algo al jugador.

También entendí la utilidad del delta time: sin él, el juego iría más rápido o más lento según el ordenador donde se ejecute. Multiplicar siempre por `delta` lo hace justo para todos.

##Para poder ejecutar el juego hay que introducir dos comando en la terminal de Android Studio.
`$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`
y despues este:
`.\gradlew desktop:run`

