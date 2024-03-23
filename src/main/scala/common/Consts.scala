package common

import chisel3._

object Consts {
    val WORD_LEN = 32
    val START_ADDR = 0.U(WORD_LEN.W)
    // val START_ADDR_OFFSET = 80000000.U(WORD_LEN.W)
    val CSR_ADDR_LEN = 12

    val EXE_FUN_LEN = 5
    val ALU_X = 0.U(EXE_FUN_LEN.W)
    val ALU_ADD = 1.U(EXE_FUN_LEN.W) // 加算命令
    val ALU_SUB = 2.U(EXE_FUN_LEN.W)
    val ALU_JALR = 3.U(EXE_FUN_LEN.W)
    val ALU_BR_BEQ = 4.U(EXE_FUN_LEN.W)
    val ALU_BR_BNE = 5.U(EXE_FUN_LEN.W)
    val ALU_BR_BLT = 6.U(EXE_FUN_LEN.W)
    val ALU_BR_BGE = 7.U(EXE_FUN_LEN.W)
    val ALU_BR_BLTU = 8.U(EXE_FUN_LEN.W)
    val ALU_BR_BGEU = 9.U(EXE_FUN_LEN.W)
    val ALU_AND = 10.U(EXE_FUN_LEN.W)
    val ALU_OR = 11.U(EXE_FUN_LEN.W)
    val ALU_XOR = 12.U(EXE_FUN_LEN.W)
    val ALU_SLL = 13.U(EXE_FUN_LEN.W)
    val ALU_SRL = 14.U(EXE_FUN_LEN.W)
    val ALU_SRA = 15.U(EXE_FUN_LEN.W)
    val ALU_SLT = 16.U(EXE_FUN_LEN.W)
    val ALU_SLTU = 17.U(EXE_FUN_LEN.W)
    val ALU_COPY1 = 18.U(EXE_FUN_LEN.W)
    val ALU_ECALL = 19.U(EXE_FUN_LEN.W)
    val ALU_MRET = 20.U(EXE_FUN_LEN.W)
    
    val OP1_LEN = 2
    val OP1_X = 0.U(OP1_LEN.W)
    val OP1_RS1 = 1.U(OP1_LEN.W) // rs1レジスタ番号
    val OP1_PC = 2.U(OP1_LEN.W) // pcレジスタ番号
    val OP1_IMZ = 3.U(OP1_LEN.W)

    val OP2_LEN = 3
    val OP2_X = 0.U(OP2_LEN.W)
    val OP2_RS2 = 1.U(OP2_LEN.W) // rs2レジスタ番号
    val OP2_IMI = 2.U(OP2_LEN.W) // I形式の即値
    val OP2_IMS = 3.U(OP2_LEN.W) // S形式の即値
    val OP2_IMU = 4.U(OP2_LEN.W) // U形式の即値
    val OP2_IMJ = 5.U(OP2_LEN.W) // J形式の即値

    val MEN_LEN = 3
    val MEN_X = 0.U(MEN_LEN.W) // メモリへのストアなし
    val MEN_SB = 1.U(MEN_LEN.W) // スカラ命令用 (1byte)
    val MEN_SH = 2.U(MEN_LEN.W) // スカラ命令用 (2byte)
    val MEN_SW = 3.U(MEN_LEN.W) // スカラ命令用 (4byte)
    val MEN_S = MEN_SW

    val REN_LEN = 2
    val REN_X = 0.U(REN_LEN.W)
    val REN_S = 1.U(REN_LEN.W) // スカラ命令用
    val REN_S_OFF = 2.U(REN_LEN.W) // オフセット計算をしてストアする

    val WB_SEL_LEN = 3
    val WB_X = 0.U(WB_SEL_LEN.W)
    val WB_MEM = 1.U(WB_SEL_LEN.W)
    val WB_ALU = 2.U(WB_SEL_LEN.W)
    val WB_PC = 3.U(WB_SEL_LEN.W)
    val WB_CSR = 4.U(WB_SEL_LEN.W)

    val CSR_LEN = 3
    val CSR_X = 0.U(CSR_LEN.W)
    val CSR_W = 1.U(CSR_LEN.W)
    val CSR_S = 2.U(CSR_LEN.W)
    val CSR_C = 3.U(CSR_LEN.W)
    val CSR_E = 4.U(CSR_LEN.W)
    
    val CSR_MTVEC = 0x305.U(CSR_ADDR_LEN.W) // CSRのmtvecにはtrap vectorのアドレスが入っている
    val CSR_MCAUSE = 0x342.U(CSR_ADDR_LEN.W)
    val CSR_MEPC = 0x341.U(CSR_ADDR_LEN.W)

    val PRIV_MODE_M = 0x11.U(WORD_LEN.W) // マシンモード
    val ILLEGAL_INST = 0x2.U(WORD_LEN.W) // 不正命令

    val BUBLE = 0x00000013.U(WORD_LEN.W) // NOP命令

    val STAGE_TOTAL = 2
    val STAGE_LEN = 3
    val STAGE_IF = 0.U(STAGE_LEN.W)
    val STAGE_ID = 1.U(STAGE_LEN.W)
    val STAGE_EXE = 2.U(STAGE_LEN.W)
    val STAGE_MEM = 3.U(STAGE_LEN.W)
    val STAGE_WB = 4.U(STAGE_LEN.W)
}