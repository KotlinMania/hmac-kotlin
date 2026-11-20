# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/3 (66.7%)
- **Function parity:** 15/15 matched (target 45) — 100.0%
- **Class/type parity:** 7/9 matched (target 7) — 77.8%
- **Combined symbol parity:** 22/24 matched (target 52) — 91.7%
- **Average inline-code cosine:** 0.29 (function body across 2 matched files)
- **Average documentation cosine:** 0.56 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. simple

- **Target:** `Simple`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 21006.4
- **Functions:** 7/7 matched (target 17)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `KeySize`, `OutputSize`

### 2. optim

- **Target:** `Optim`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 1407.8
- **Functions:** 8/8 matched (target 28)
- **Missing functions:** _none_
- **Types:** 6/6 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

