package korlibs.io.file.std

import korlibs.io.isNodeJs
import korlibs.io.runtime.node.require_node
import korlibs.platform.*

actual object StandardPaths : StandardPathsBase {
    override val cwd: String get() = when {
        isNodeJs -> require_node("fs").realpathSync(process.cwd()).unsafeCast<String>()
        else -> "."
    }

    //override val executableFile: String get() = when {
    //    isNodeJs -> {
    //        console.log("argv", process.argv, process.argv0)
    //        console.log(Error())
    //        (process.argv[1] ?: null)?.unsafeCast<String>() ?: super.executableFile
    //    }
    //    else -> super.executableFile
    //}
}
