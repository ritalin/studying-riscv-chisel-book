package common

import chisel3._
import chisel3.util._

object Instructions {
    // LOAD WORD命令
    // funct3: 010
    // opcode: 0000011
    val LW = BitPat("b?????????????????010?????0000011") // [?]{17}010[?]{5}000{3}1{2}
    // STORE WORD命令
    // funct3: 010
    // opcode: 0100011
    val SW = BitPat("b?????????????????010?????0100011") // [?]{17}010[?]{5}010{3}1{2}
}