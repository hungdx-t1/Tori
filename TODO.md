# 📌 Tori Framework Roadmap - v26.9.3

Development plan and progress tracking of features for the next version.

---

## 🎯 v26.9.3 Milestone

### 🛠️ In Progress / Planned

- [ ] Adding Autocomplete for `@CommandOption`
- [ ] Make a global exception handler

### ✅ Completed
- [x] Make `FutureAction` lazy-loading completely.
- [x] Add `@GuildOwnerOnly` annotation and related field to indicates the command can only executed by owner of the guild. 
- [x] Add `retryWhen`/`retryIfError` method on `FutureAction`.
- [x] Add `Retry` interface.
- [x] Add `console.console-mode` configuration section to customize logging.
- [x] Remove auto generate startup scripts (because it is redundant, and I will add wiki pages instead).