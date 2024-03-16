package common

import chisel3._

object Consts {
    val WORD_LEN = 32
    val START_ADDR = 0.U(WORD_LEN.W)
    // val START_ADDR_OFFSET = 80000000.U(WORD_LEN.W)

    val EXE_FUN_LEN = 5
    val ALU_X = 0.U(EXE_FUN_LEN.W)
    val ALU_ADD = 1.U(EXE_FUN_LEN.W) // 加算命令
    val ALU_JALR = 2.U(EXE_FUN_LEN.W)
    val ALU_BR_BEQ = 3.U(EXE_FUN_LEN.W)
    val ALU_BR_BNE = 4.U(EXE_FUN_LEN.W)
    val ALU_BR_BLT = 5.U(EXE_FUN_LEN.W)
    val ALU_BR_BGE = 6.U(EXE_FUN_LEN.W)
    val ALU_BR_BLTU = 7.U(EXE_FUN_LEN.W)
    val ALU_BR_BGEU = 8.U(EXE_FUN_LEN.W)
    val ALU_AND = 9.U(EXE_FUN_LEN.W)
    val ALU_OR = 10.U(EXE_FUN_LEN.W)
    val ALU_XOR = 11.U(EXE_FUN_LEN.W)
    val ALU_SLL = 12.U(EXE_FUN_LEN.W)
    val ALU_SRL = 13.U(EXE_FUN_LEN.W)
    val ALU_SRA = 14.U(EXE_FUN_LEN.W)
    
    val OP1_LEN = 2
    val OP1_X = 0.U(OP1_LEN.W)
    val OP1_RS1 = 1.U(OP1_LEN.W) // rs1レジスタ番号
    val OP1_PC = 2.U(OP1_LEN.W) // pcレジスタ番号

    val OP2_LEN = 3
    val OP2_RS2 = 1.U(OP2_LEN.W) // rs2レジスタ番号
    val OP2_IMI = 2.U(OP2_LEN.W) // I形式の即値
    val OP2_IMS = 3.U(OP2_LEN.W) // S形式の即値
    val OP2_IMU = 4.U(OP2_LEN.W) // U形式の即値
    val OP2_IMJ = 5.U(OP2_LEN.W) // J形式の即値

    val MEN_LEN = 2
    val MEN_X = 0.U(MEN_LEN.W) // メモリへのストアなし
    val MEN_S = 1.U(MEN_LEN.W) // スカラ命令用

    val REN_LEN = 2
    val REN_X = 0.U(REN_LEN.W)
    val REN_S = 1.U(REN_LEN.W) // スカラ命令用

    val WB_SEL_LEN = 3
    val WB_X = 0.U(WB_SEL_LEN.W)
    val WB_MEM = 1.U(WB_SEL_LEN.W)
    val WB_ALU = 2.U(WB_SEL_LEN.W)
    val WB_PC = 3.U(WB_SEL_LEN.W)
}