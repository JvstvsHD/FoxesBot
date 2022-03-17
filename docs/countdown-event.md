# Countdown Event

Countdown Event sind Events in Channeln, bei dem die User einen Countdown nachstellen sollen, d.h. bis auf 0
runterzählen.

## Starten eines Events

Um ein CD-Event zu starten, muss der Command **/countdown \<Startwert> \<Channel>** ausgeführt werden. Im angegebenen
Channel wird dann eine Nachricht erscheinen, die über den Start eines Events sowie dessen Startwert instruiert sowie
Fails andeutet.

## Fails

Als Fail gilt:

- invalide Zahl (also Nachricht, die sich nicht eindeutig als Zahl identifizieren lässt, z.B. 100#, w100, 300., etc.)
- eine falsche Zahl, die nicht (letzte Zahl) - 1 ist (wenn letzte Zahl: 100, dann alles außer 99, wie z.B. 100 und 98)

Sollte ein Fail auftreten, wird der Countdown zurückgesetzt. Dies kann zu folgenden Punkten sein:

- zum letzten Hunderter (98 -> 100, 327 -> 400)
- zum letzten Zehner (98 -> 100, 327 -> 330)

Wohin zurückgesetzt werden soll, kann mit **/countdownreset \<Option>** eingestellt werden. Der Command ist global und
unterscheidet *nicht* zwischen Events.  
Des Weiteren kann man **nicht** direkt nach seiner Nachricht die nächste senden. Es muss zuerst jemand anderes eine
Nachricht schreiben. Dabei zählt nicht, ob die Nachricht valide ist.

## Ende

Zum Ende eines jeden Events werden einige Statistiken aufgelistet.