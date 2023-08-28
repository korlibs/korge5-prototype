package korlibs.inject

@Deprecated("", replaceWith = ReplaceWith("Injector", "korlibs.inject.Injector"))
typealias AsyncInjector = Injector

@Deprecated("", replaceWith = ReplaceWith("InjectorFactory<T>", "korlibs.inject.InjectorFactory"))
typealias AsyncFactory<T> = InjectorFactory<T>

@Deprecated("", replaceWith = ReplaceWith("InjectorDependency", "korlibs.inject.InjectorDependency"))
typealias InjectorAsyncDependency = InjectorDependency