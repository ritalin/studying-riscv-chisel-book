package lui

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import common.Consts._

class ImemPortIo extends Bundle {
    // 入力されたアドレス(PC)
    val addr = Input(UInt(WORD_LEN.W))
    // 読み出されたデータ
    val inst = Output(UInt(WORD_LEN.W))
}

class DmemPortIo extends Bundle {
    // 入力されたアドレス(メモリ)
    val addr = Input(UInt(WORD_LEN.W))
    // メモリから読み出したデータ
    val rdata = Output(UInt(WORD_LEN.W))
    // 書き込み可否信号
    val wen = Input(Bool())
    // メモリへの書き込みデータ
    val wdata = Input(UInt(WORD_LEN.W))
}

class Memory extends Module {
    val io = IO(new Bundle {
        val imem = new ImemPortIo()
        val dmem = new DmemPortIo()
    })

    // 16kB
    val mem = Mem(16384, UInt(8.W))

    io.imem.inst := Cat(
        mem(io.imem.addr + 3.U(WORD_LEN.W)),
        mem(io.imem.addr + 2.U(WORD_LEN.W)),
        mem(io.imem.addr + 1.U(WORD_LEN.W)),
        mem(io.imem.addr + 0.U(WORD_LEN.W))
    )

    io.dmem.rdata := Cat(
        mem(io.dmem.addr + 3.U(WORD_LEN.W)),
        mem(io.dmem.addr + 2.U(WORD_LEN.W)),
        mem(io.dmem.addr + 1.U(WORD_LEN.W)),
        mem(io.dmem.addr + 0.U(WORD_LEN.W))
    )

    when (io.dmem.wen) {
        mem(io.dmem.addr + 3.U(WORD_LEN.W)) := io.dmem.wdata(31, 24)
        mem(io.dmem.addr + 2.U(WORD_LEN.W)) := io.dmem.wdata(23, 16)
        mem(io.dmem.addr + 1.U(WORD_LEN.W)) := io.dmem.wdata(15, 8)
        mem(io.dmem.addr + 0.U(WORD_LEN.W)) := io.dmem.wdata(7, 0)
    }

    def loadFrom(hex_file_path: String) = {
        // loadMemoryFromFileだと警告出た上に正しく読み込まない
        loadMemoryFromFileInline(mem, hex_file_path)    
    }
}
