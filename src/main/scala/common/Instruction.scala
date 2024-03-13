package common

import chisel3._
import chisel3.util._

object Instructions {
    // 命令Bitパターン
    // funct3: 010
    // opcode: 0000011
    val LW = BitPat("b?????????????????010?????0000011") // [?]{17}010[?]{5}0{5}1{2}
}