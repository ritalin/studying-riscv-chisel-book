package run_test

import chisel3._
import chisel3.util._
import common.Consts._

class Top(start_addr: UInt = 0.U(WORD_LEN.W), quiet: Boolean = false) extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
        val gp = Output(UInt(WORD_LEN.W))
    })

    val core = Module(new Core(start_addr, quiet))
    val memory = Module(new Memory())

    // 接続
    core.io.imem <> memory.io.imem
    core.io.dmem <> memory.io.dmem // 本文中には記載されていない
    io.exit := core.io.exit
    io.gp := core.io.gp
}