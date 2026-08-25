# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/3 (100.0%)
- **Function parity:** 16/16 matched (target 16) — 100.0%
- **Class/type parity:** 3/9 matched (target 3) — 33.3%
- **Combined symbol parity:** 19/25 matched (target 19) — 76.0%
- **Average inline-code cosine:** 0.78 (function body across 3 matched files)
- **Average documentation cosine:** 0.90 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. optim

- **Target:** `hmac.Optim`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 41403.0
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 2/6 matched (target 2)
- **Missing types:** `BufferKind`, `KeySize`, `BlockSize`, `OutputSize`

### 2. simple

- **Target:** `hmac.Simple`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 21001.6
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `KeySize`, `OutputSize`

### 3. lib

- **Target:** `hmac.Lib`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 102.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

