# Contributing to Tori Framework

First off, thank you for considering contributing to Tori! It's people like you that make Tori an incredible tool for building Discord bots.

This document outlines the process for contributing to the Tori ecosystem, including the core API (`tori-api`), the server (`tori-server`), and extensions (`tori-extension-dave`).

---

## 1. Where to Start?
- **Found a bug?** Open an Issue on GitHub with a clear title, steps to reproduce, and the error log (stack trace).
- **Have a feature request?** Open an Issue to discuss it before you start coding. This ensures your idea aligns with the project's roadmap and saves you from wasted effort.
- **Want to code?** Check the Issues tab for items labeled `good first issue` or `help wanted`.

---

## 2. Branching Strategy (CRITICAL)
Tori maintains multiple versions and distributions (Core vs. Full/JDave). Please follow these rules strictly when creating your Pull Request (PR):

### 🚫 Never PR directly to `main`
The `main` branch represents the currently released, stable version. We do not accept direct PRs to `main` unless it is a critical hotfix for a severe bug in the current release.

### ✅ PR to the `version/x.x.x` branch
When contributing a new feature or fixing a non-critical bug, you **must** target your PR to the active development branch (e.g., `version/26.5.120`).
- If you are unsure which branch is the current development branch, please ask in the Issues section or check the most recently updated `version/` branch.

---

## 3. Development Setup
Tori uses Maven for dependency management and builds. Please note our dual-Java requirement:

1. **For `tori-api` and `tori-server` (Core):**
    - Ensure your IDE is set to use **JDK 21**.
    - These modules must remain backward-compatible with Java 21. Do not use Java 22+ features in these modules.

2. **For `tori-extension-dave` (Audio Encryption):**
    - This module requires **JDK 25** due to the underlying `jdave` dependency.
    - If you are modifying this extension, ensure your compiler is set to JDK 25.

To build the project locally and verify your changes:
```bash
# Build the standard Core version (Java 21)
mvn clean package

# Build the Full version with DAVE support (Java 25)
mvn clean package -P dave-build
```

---

## 4. Pull Request Process

1. **Fork the repository** and clone it to your local machine.

2. **Create a new branch** from the target development branch (e.g., `git checkout -b feature/my-awesome-feature origin/version/26.5.120`).

3. **Make your changes** and commit them. Please write clear, concise commit messages.

4. **Test your code**. Ensure your code compiles and does not break existing functionality.

5. **Push your branch** to your fork.

6. **Open a Pull Request.**
* Fill out the PR template completely.
* Describe what you changed and why.
* Reference any related Issues (e.g., "Fixes #12").

# Code Review
Once your PR is submitted, the maintainers will review your code. We might ask for changes or clarifications. Please be patient and responsive. Once everything looks good, and the GitHub Actions CI passes (✅), your PR will be merged into the development branch!

--- 

# Thank you for helping make Tori better! 🚀