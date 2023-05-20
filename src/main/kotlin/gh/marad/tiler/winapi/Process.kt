package gh.marad.tiler.winapi

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import java.io.Closeable

class Process(val processId: Int) : Closeable {
    private val kernel = Kernel32.INSTANCE
    private val handle = kernel.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, processId)

    init {
        if (Pointer.NULL == handle) {
            throw RuntimeException("Failed to open process $processId")
        }
    }

    fun exePath(): String {
        val nameArray = CharArray(261)
        val readLen = IntByReference(261)
        kernel.QueryFullProcessImageName(handle, 0, nameArray, readLen)
        return nameArray.concatToString().trim(Char(0))
    }

    fun exeName(): String = exePath().split("\\").last()

    override fun close() {
        kernel.CloseHandle(handle)
    }
}