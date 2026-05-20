# tessera-samples

Sample programs showing how to use `tessera-core` as a library.

Each sample has its own `main()` and can be run independently.

## Running a sample

```bash
./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.QuickStartSampleKt
./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.TrainingSampleKt
./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.SpecialTokensSampleKt
./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.PersistenceSampleKt
```

## Samples

| File | What it shows |
|---|---|
| `QuickStartSample.kt` | Minimal train → encode → decode → verify round-trip |
| `TrainingSample.kt` | Training config options and progress callback |
| `SpecialTokensSample.kt` | Custom special tokens, allowed vs. disallowed at encode time |
| `PersistenceSample.kt` | Train, save to disk, load from disk, verify identical encoding |

## Note

All samples import only `public` symbols from `tessera-core`.
No access to `internal` classes.
