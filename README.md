## KorGE 5.0

KorGE Reboot! New features and changes:

* WASM and FFI modules in common code with a common interface.
* Complex formats supported via WASM: WEBP loading and MP3 decoding as WASM modules embedded in code.
* Operating system integrations now happen with FFI (that both works with JVM and JS). De-duplicated code.
* Supports custom libraries to support complex formats via WASM and OS integrations via FFI.
* JVM, JS and WASM targets only. Native K/N and Android dropped. Integrations still happen via WebViews or JavaScript/WASM loading.
* Faster compile times. Lower download times due to having less targets.
* WASM target eventually will support wasm2c to generate native executables for any platform.
* JS target is isomorphic: generates a single executable that runs in the browser and via cli with Deno executable.
* Korlibs split rework: `korge-foundation` (kbignum, krypto, kds, korinject, klogger, korma, kmem, klock), `korge-core` (korio, korim, korau, korte & ffi) and `korge` (korev, kgl, korgw, korge)
* `AsyncInjector` is now a sync `Injector`. Not asynchronous anymore, and thus simpler and faster.
* Old Korlib artifacts are going to be preserved in the `korlibs4` repository (except for korgw and korge that will be dropped). They will be maintained by the community. Will be kept up to date with Kotlin versions and targets and open to bugfixes, but won't include new features or backports from KorGE 5.
