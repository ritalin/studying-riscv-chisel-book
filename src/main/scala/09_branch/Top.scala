package branch

import chisel3._
import chisel3.util._
import common.Consts._

class Top(exit_addr: UInt, start_addr: UInt = 0.U(WORD_LEN.W)) extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
    })

    val core = Module(new Core(exit_addr, start_addr))
    val memory = Module(new Memory())

    // 接続
    core.io.imem <> memory.io.imem
    core.io.dmem <> memory.io.dmem // 本文中には記載されていない
    io.exit := core.io.exit
}