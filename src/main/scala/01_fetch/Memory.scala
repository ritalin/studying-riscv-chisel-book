package fetch

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import common.Consts._

class ImemPortIo extends Bundle {
    val addr = Input(UInt(WORD_LEN.W))
    val inst = Output(UInt(WORD_LEN.W))
}

class Memory extends Module {
    val io = IO(new Bundle {
        val imem = new ImemPortIo()
    })

    val mem = Mem(16384, UInt(8.W))
    // loadMemoryFromFileだと警告出た上に正しく読み込まない
    loadMemoryFromFileInline(mem, "src/hex/fetch.hex")

    io.imem.inst := Cat(
        mem(io.imem.addr + 3.U(WORD_LEN.W)),
        mem(io.imem.addr + 2.U(WORD_LEN.W)),
        mem(io.imem.addr + 1.U(WORD_LEN.W)),
        mem(io.imem.addr)
    )
}