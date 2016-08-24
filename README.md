## Wilbur

> Iré a esos polos cuando la Tierra esté despejada, si no puedo comprender
> la fórmula Dho-Hna cuando la memorice.
> Los que viven en el aire ma dijeron en el Sabbat que passarán años antes
> que se despeje la Tierra, y supongo que abuelo muerto entonces, así que tendré
> que aprender todos los ángulos de los planos y todas las fórmulas entre el Yr y el Nhhngr.

_Wilbur Whateley, 26 noviembre 1916_

**wilbur** is a clone of the famous [til](https://til.hashrocket.com/) app made by
[hashrocket](https://github.com/hashrocket/hr-til) using clojure on the backend and
clojurescript with [reagent](http://reagent-project.github.io/) on frontend.

### Lein thing

 * ```lein run``` starts http server on port 3000
 * ```lein figwheel``` connects figwheel
 * ```lein uberjar```

AFAIR this project was generated using [reagent template](https://github.com/reagent-project/reagent-template).

### Toolbox

 * [Postgresql](https://www.postgresql.org/docs/9.5/static/index.html)

```createdb wilbur_development```

 * [Migratus](https://github.com/yogthos/migratus) / [clojars](https://clojars.org/migratus)

Run migrations from clojure:

```clojure
(require '[wilbur.db :as db])
(db/migrate-all-things!)
```

`lein migratus` and `java -jar target/uberjar/wilbur.jar migrate` does the same.

`lein migratus create do-stuff` creates a new migration

 * [yesql](https://github.com/krisajenkins/yesql) / [clojars](https://clojars.org/yesql)

 * [buddy-auth](https://funcool.github.io/buddy-auth/latest) / [clojars](https://clojars.org/buddy/buddy-auth)

 * Logs with [timbre](https://github.com/ptaoussanis/timbre).

Exclude all calls from code bellow warn level:

```bash
export TIMBRE_LEVEL=':warn'
lein cljsbuild once
lein uberjar
```

### Stuff

 * [component-level-state](https://github.com/reagent-project/reagent-cookbook/tree/master/basics/component-level-state)
