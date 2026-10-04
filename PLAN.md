# Vocnjak — Orchard Planner MVP

## Context

The repo is currently an empty IntelliJ Java project (just `.gitignore`, `.idea/`, `Vocnjak.iml`, empty `src/`) with JDK 26 configured. The user wants to track the fruit trees they plant: draft an orchard layout, place trees within it, and record per-tree details (species/variety, year planted, flowering time, ripening time, notes). They want this as a real runnable web app they can test themselves, not just code. We've agreed on the stack: Spring Boot (Maven) + H2 (file-based) + server-rendered Thymeleaf pages with a plain HTML/CSS grid — no JS framework, no auth, no multi-user concerns, single v1 MVP.

Latest stable Spring Boot (as of Oct 2026) is **Spring Boot 4.1.x**, which officially supports Java up to version 26. That means we can target **Java 26 directly**, matching the JDK already configured in this IntelliJ project (`.idea/misc.xml`) — no need to install a separate JDK, simplifying first-run setup.

## Key decisions

- **Spring Boot 4.1.x** (latest stable), **Java 26**, Maven build, packaging jar, embedded Tomcat (so `mvn spring-boot:run` gives a real testable web app).
- **Fixed rows × columns grid** for orchard layout (not freeform x/y coordinates). Matches how people actually describe orchard layout ("row 3, column 5"), makes occupancy/bounds checks trivial, and renders as a plain HTML table — no canvas/JS needed. Can evolve later if ever needed.
- Base package: `com.vocnjak`, groupId `com.vocnjak`, artifactId `vocnjak`.
- H2 file-based DB (`./data/vocnjak`) so data survives app restarts; H2 console enabled for dev inspection.

## Project layout

```
Vocnjak/
  pom.xml
  src/main/java/com/vocnjak/
    VocnjakApplication.java
    domain/Orchard.java, Tree.java
    repository/OrchardRepository.java, TreeRepository.java
    service/OrchardService.java, TreeService.java
    service/exception/NotFoundException.java, OutOfBoundsException.java, CellOccupiedException.java
    web/OrchardController.java, TreeController.java, GlobalExceptionHandler.java
    web/dto/OrchardCreateForm.java, TreeForm.java
  src/main/resources/
    application.properties
    templates/orchards/{list,new,grid}.html
    templates/trees/{form,view}.html
    static/css/style.css
  src/test/java/com/vocnjak/service/OrchardServiceTest.java, TreeServiceTest.java
  src/test/java/com/vocnjak/web/OrchardControllerTest.java, TreeControllerTest.java
  src/test/resources/application-test.properties   (points H2 at in-memory DB for tests)
  .gitignore   (add target/ and /data/)
```

Opening `pom.xml` in IntelliJ will let it re-import the module as Maven-aware (replacing the current bare `Vocnjak.iml`) automatically — no manual `.iml` editing.

## pom.xml / application.properties

- Parent: `org.springframework.boot:spring-boot-starter-parent:4.1.x` (use latest 4.1 patch at implementation time).
- `<java.version>26</java.version>`.
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `spring-boot-starter-test` (test), `spring-boot-devtools` (runtime, optional — live reload during manual testing).
- `spring-boot-maven-plugin` in `<build>` for `mvn spring-boot:run` + executable jar.
- `application.properties`: `spring.datasource.url=jdbc:h2:file:./data/vocnjak;AUTO_SERVER=TRUE`, `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.open-in-view=false`, `spring.h2.console.enabled=true` (path `/h2-console`), `server.port=8080`.
- `src/test/resources/application-test.properties`: `spring.datasource.url=jdbc:h2:mem:vocnjak-test` so tests don't touch the dev data file.

## Domain model

**Orchard** (`domain.Orchard`): `id` (Long, generated), `name` (`@NotBlank @Size(max=100)`), `rows` (int, `@Min(1) @Max(100)`), `columns` (int, `@Min(1) @Max(100)`), `trees` (`@OneToMany(mappedBy="orchard", cascade=ALL, orphanRemoval=true)`).

**Tree** (`domain.Tree`), table `tree` with DB `@UniqueConstraint` on `(orchard_id, row_index, col_index)`: `id`, `orchard` (`@ManyToOne(optional=false)`), `rowIndex`/`colIndex` (int, `@Min(0)`), `species` (`@NotBlank @Size(max=100)`), `variety` (`@Size(max=100)`), `plantedYear` (`Integer`, `@Min(1900) @Max(2100)`), `floweringPeriod`/`ripeningPeriod` (free-form `String`, `@Size(max=100)` — e.g. "Late April"), `notes` (`String`, `@Size(max=2000)`).

Row/col bounds against the parent orchard's `rows`/`columns` can't be expressed via static Bean Validation (depends on a sibling entity) — that check lives in the service layer. The DB unique constraint is a second line of defense behind the service-layer occupancy check.

## Layered architecture

**Repositories**: `OrchardRepository extends JpaRepository<Orchard, Long>`. `TreeRepository` adds `findByOrchardId`, `findByOrchardIdAndRowIndexAndColIndex`, `existsByOrchardIdAndRowIndexAndColIndex`.

**Services**:
- `OrchardService`: `createOrchard(name, rows, columns)`, `getOrchard(id)` (throws `NotFoundException`), `listOrchards()`, `deleteOrchard(id)`.
- `TreeService.addTree(orchardId, row, col, TreeForm)`: loads orchard (404 if missing) → validates `0<=row<rows` and `0<=col<columns` (else `OutOfBoundsException`) → checks `!existsByOrchardIdAndRowIndexAndColIndex` (else `CellOccupiedException`) → maps form → saves. Also `updateTree(treeId, TreeForm)` (position immutable in MVP), `getTree(treeId)`, `deleteTree(treeId)`.

**Controllers** (`web`):
- `OrchardController` `@RequestMapping("/orchards")`: `GET ""` list, `GET "/new"` new form, `POST ""` create (`@Valid` + `BindingResult`, redirect to grid or re-render form with errors), `GET "/{id}"` grid view, `POST "/{id}/delete"`.
- `TreeController` `@RequestMapping("/orchards/{orchardId}/trees")`: `GET "/new?row=&col="`, `POST ""` create, `GET "/{treeId}"` view, `GET "/{treeId}/edit"`, `POST "/{treeId}"` update, `POST "/{treeId}/delete"`.
- Root `/` redirects to `/orchards`.
- `GlobalExceptionHandler` (`@ControllerAdvice`): `NotFoundException` → 404; `CellOccupiedException` → redirect to grid with `RedirectAttributes` flash error ("That cell is already occupied by a tree."), rendered as a banner in `grid.html`.

**Grid rendering**: `OrchardController.viewGrid` builds a `rows × columns` 2D structure (nulls for empty cells) via a `TreeService` helper, so `grid.html` just nests `th:each` loops over rows/columns: empty cell → link to `trees/new?row=R&col=C`; occupied cell → species/variety summary + link to `trees/{treeId}`. Plain HTML `<table>` styled via `static/css/style.css` — no JS.

## Validation summary

Bean Validation (`@NotBlank`/`@Size`/`@Min`/`@Max`) on `OrchardCreateForm` and `TreeForm` for field-level rules; `BindingResult.hasErrors()` re-renders the form template with `th:errors`. Cross-entity rules (grid bounds, cell occupancy, not-found) live in the service layer and throw the three custom exceptions, handled by `GlobalExceptionHandler`.

## Test plan

**Service unit tests** (`TreeServiceTest`, `OrchardServiceTest`): cell-free save succeeds; duplicate cell throws `CellOccupiedException` and doesn't save; out-of-range row/col throws `OutOfBoundsException`; missing orchard throws `NotFoundException`; delete removes tree; at least one real `@DataJpaTest` confirming orchard-delete cascades to trees.

**Controller integration tests** (`@SpringBootTest` + MockMvc, backed by the test H2 profile): create orchard → grid shows correct empty cells; blank name re-renders form with errors; add tree → appears in grid; second tree to same cell is rejected end-to-end; edit updates fields; delete removes from grid.

## Manual verification (after implementation)

1. `cd Vocnjak && mvn spring-boot:run`, confirm it starts on `localhost:8080` with JDK 26 (no extra JDK install needed given Spring Boot 4.1 support).
2. Golden path: open `/` → redirects to `/orchards` (empty) → create orchard "Backyard" 4×5 → grid of empty cells → click a cell → fill species/variety/plantedYear/flowering/ripening/notes → submit → cell shows summary → open it → edit variety → delete it → cell empties again.
3. Edge case: add a tree to (0,0), then hit `/orchards/{id}/trees/new?row=0&col=0` again and submit a second tree → expect redirect back to grid with a visible "already occupied" error and only the original tree present.
4. Sanity-check H2 console at `/h2-console` (JDBC URL `jdbc:h2:file:./data/vocnjak`) to see the `ORCHARD`/`TREE` tables and the unique constraint, and confirm data persists across an app restart.
5. Run `mvn test` to confirm the automated suite passes.

## Critical files

- `pom.xml` — Spring Boot 4.1.x parent, Java 26, dependencies.
- `src/main/resources/application.properties` — H2 file datasource + console toggle.
- `src/main/java/com/vocnjak/domain/Tree.java` — grid-cell/uniqueness model.
- `src/main/java/com/vocnjak/service/TreeService.java` — occupancy/bounds business rules.
- `src/main/java/com/vocnjak/web/OrchardController.java` — grid rendering entry point.
- `src/main/resources/templates/orchards/grid.html` — the plain HTML/CSS grid UI.