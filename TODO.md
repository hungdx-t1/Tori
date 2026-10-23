# 📌 Tori Framework Roadmap - v26.10.0

Development plan and progress tracking of features for the next version.

---

## 🎯 v26.10.0 Milestone

### 🛠️ In Progress / Planned

- [ ] Adding Autocomplete for `@CommandOption`
- [ ] Make a global exception handler

### ✅ Completed
- [x] Add auto-download file jar if available via command.
- [x] Add ConcurrentUniqueMap and ImmutableUniqueMap based on UniqueMap interface.
- [x] Add of() static method on UniqueMap interface and move Entry record to that.
- [x] Support defering when has an error on command interaction (only on CommandRegistrar v1)
- [x] Add toArray() to support rollback collections (ex: List, Set) to array
- [x] Add auto-load ".env" file when starting Tori server.
- [x] Add getEnv methods and bonus missing javadocs on some method in JavaDiscordBot.