package korlibs.render

actual fun createDialogInterfaceForComponent(nativeComponent: Any?): DialogInterface =
    DialogInterfaceWasm()
