# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/3 (100.0%)
- **Function parity:** 16/16 matched (target 44) — 100.0%
- **Class/type parity:** 3/9 matched (target 4) — 33.3%
- **Combined symbol parity:** 19/25 matched (target 48) — 76.0%
- **Average inline-code cosine:** 0.61 (function body across 3 matched files)
- **Average documentation cosine:** 0.74 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. optim

- **Target:** `hmac.Optim`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 41405.4
- **Functions:** 8/8 matched (target 24)
- **Missing functions:** _none_
- **Types:** 2/6 matched (target 2)
- **Missing types:** `BufferKind`, `KeySize`, `BlockSize`, `OutputSize`

### 2. simple

- **Target:** `hmac.Simple`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 21002.9
- **Functions:** 7/7 matched (target 17)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `KeySize`, `OutputSize`

### 3. lib

- **Target:** `hmac.Lib`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 103.2
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

