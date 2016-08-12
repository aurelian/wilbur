## Wilbur Whateley

### Lein thing

 * ```lein run``` starts http server
 * ```lein figwheel``` connects figwheel
 * ```lein uberjar```

### Database

 * [Postgresql](https://www.postgresql.org/docs/9.5/static/index.html)

```createdb wilbur_development```

 * Migratus [github](https://github.com/yogthos/migratus) [clojars](https://clojars.org/migratus)

Run migrations from clojure:

```clojure
(require '[wilbur.db :as db])
(db/migrate-all-things!)
```

`lein migratus` and `java -jar target/uberjar/wilbur.jar migrate` does the same.

`lein migratus create do-stuff` creates a new migration

 * yesql => to run sql queries [github](https://github.com/krisajenkins/yesql) [clojars](https://clojars.org/yesql)

### Stuff

 * [component-level-state](https://github.com/reagent-project/reagent-cookbook/tree/master/basics/component-level-state)

