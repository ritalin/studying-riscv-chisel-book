package load_word

import chisel3._
import chisel3.util._

class Top extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
    })

    val core = Module(new Core())
    val memory = Module(new Memory())

    // 接続
    core.io.imem <> memory.io.imem
    core.io.dmem <> memory.io.dmem // 本文中には記載されていない
    io.exit := core.io.exit
}