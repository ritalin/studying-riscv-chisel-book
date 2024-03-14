package common

import chisel3._

object Consts {
    val WORD_LEN = 32
    val START_ADDR = 0.U(WORD_LEN.W)
    // val START_ADDR_OFFSET = 80000000.U(WORD_LEN.W)

    val EXE_FUN_LEN = 5
    val ALU_X = 0.U(EXE_FUN_LEN.W)
    val ALU_ADD = 1.U(EXE_FUN_LEN.W) // 加算命令

    val OP1_LEN = 2
    val OP1_RS1 = 1.U(OP1_LEN.W) // rs1レジスタ番号

    val OP2_LEN = 3
    val OP2_RS2 = 1.U(OP2_LEN.W) // rs2レジスタ番号
    val OP2_IMI = 2.U(OP2_LEN.W) // I形式の即値
    val OP2_IMS = 3.U(OP2_LEN.W) // S形式の即値

    val MEM_LEN = 2
    val MEM_X = 0.U(MEM_LEN.W) // メモリへのストアなし
    val MEM_S = 1.U(MEM_LEN.W) // スカラ命令用

    val REN_LEN = 2
    val REN_X = 0.U(REN_LEN.W)
    val REN_S = 1.U(REN_LEN.W) // スカラ命令用

    val WB_SEL_LEN = 3
    val WB_X = 0.U(WB_SEL_LEN.W)
    val WB_MEM = 1.U(WB_SEL_LEN.W)
}