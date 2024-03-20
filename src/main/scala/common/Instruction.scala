package common

import chisel3._
import chisel3.util._

object Instructions {
    // LOAD命令(I)
    // 1ワード(4byte)ロードする
    // funct3: 010
    // opcode: 0000011
    val LW = BitPat("b?????????????????010?????0000011") 
    // 2byteロードする (符号拡張)
    // funct3: 001
    // opcode: 0000011
    val LH = BitPat("b?????????????????001?????0000011")  
    // 1byteロードする (符号拡張)
    // funct3: 100
    // opcode: 0000011
    val LB = BitPat("b?????????????????000?????0000011")
    // 2byteロードする 
    // funct3: 101
    // opcode: 0000011
    val LHU = BitPat("b?????????????????101?????0000011")  
    // 1byteロードする
    // funct3: 100
    // opcode: 0000011
    val LBU = BitPat("b?????????????????100?????0000011")

    // STORE WORD命令(I)
    // funct3: 010
    // opcode: 0100011
    val SW = BitPat("b?????????????????010?????0100011") 
    // funct3: 001
    // opcode: 0100011
    val SH = BitPat("b?????????????????001?????0100011") 
    // funct3: 000
    // opcode: 0100011
    val SB = BitPat("b?????????????????000?????0100011") 

    // 加算命令(R)
    // funct7: 0000000
    // funct3: 000
    // opcode: 0110011
    val ADD = BitPat("b0000000??????????000?????0110011")
    // 減算命令(R)
    // funct7: 0100000
    // funct3: 000
    // opcode: 0110011
    val SUB = BitPat("b0100000??????????000?????0110011")
    // 即値加算命令(I)
    // funct3: 000
    // opcode: 0010011
    val ADDI = BitPat("b?????????????????000?????0010011") 

    // ジャンプ命令(J)
    // opcode: 1101111
    val JAL = BitPat("b?????????????????????????1101111") 
    // 即値ジャンプ命令(I)
    // funct3: 000
    // opcode: 1100111
    val JALR = BitPat("b?????????????????000?????1100111") 

    // 即値20bit + 左12bitシフト
    // opcode: 0110111
    val LUI = BitPat("b?????????????????????????0110111") 
    // PC + 即値20bit + 左12bitシフト
    // opcode: 0010111
    val AUIPC = BitPat("b?????????????????????????0010111")

    // 等価分岐命令(B)
    // funct3: 000
    // opcode: 1100011
    val BEQ = BitPat("b?????????????????000?????1100011")
    // 不等価分岐命令(B)
    // funct3: 001
    // opcode: 1100011
    val BNE = BitPat("b?????????????????001?????1100011")
    // a >= b比較分岐命令(B)
    // 符号付きの場合
    // funct: 100
    // opcode: 1100011
    val BLT = BitPat("b?????????????????100?????1100011")
    // a >= b比較分岐命令(B)
    // 符号付きの場合
    // funct: 101
    // opcode: 1100011
    val BGE = BitPat("b?????????????????101?????1100011")
    // a >= b比較分岐命令(B)
    // 符号なしの場合
    // funct: 110
    // opcode: 1100011
    val BLTU = BitPat("b?????????????????110?????1100011")
    // a >= b比較分岐命令(B)
    // 符号なしの場合
    // funct: 111
    // opcode: 1100011
    val BGEU = BitPat("b?????????????????111?????1100011")

    // 論理演算命令(R)
    val AND = BitPat("b0000000??????????111?????0110011")
    val OR = BitPat("b0000000??????????110?????0110011")
    val XOR = BitPat("b0000000??????????100?????0110011")
    // 論理演算命令(I)
    val ANDI = BitPat("b?????????????????111?????0010011") 
    val ORI = BitPat("b?????????????????110?????0010011") 
    val XORI = BitPat("b?????????????????100?????0010011") 

    // シフト演算命令(R)
    val SLL = BitPat("b0000000??????????001?????0110011")
    val SRL = BitPat("b0000000??????????101?????0110011")
    val SRA = BitPat("b0100000??????????101?????0110011") // 算術右シフト（MSB-1を右シフトして、開いたところにMSBの値が入る）
    // シフト演算命令(I)
    val SLLI = BitPat("b0000000??????????001?????0010011")
    val SRLI = BitPat("b0000000??????????101?????0010011")
    val SRAI = BitPat("b0100000??????????101?????0010011")

    // 比較命令(R)
    val SLT = BitPat("b0000000??????????010?????0110011")
    val SLTU = BitPat("b0000000??????????011?????0110011")
    // 比較命令(I)
    val SLTI = BitPat("b?????????????????010?????0010011") 
    val SLTIU = BitPat("b?????????????????011?????0010011")

    // CSR命令(I)
    val CSRRW = BitPat("b?????????????????001?????1110011") 
    val CSRRWI = BitPat("b?????????????????101?????1110011") 
    val CSRRS = BitPat("b?????????????????010?????1110011") 
    val CSRRSI = BitPat("b?????????????????110?????1110011") 
    val CSRRC = BitPat("b?????????????????011?????1110011") 
    val CSRRCI = BitPat("b?????????????????111?????1110011") 

    // ECALL
    val ECALL = BitPat("b00000000000000000000000001110011") 
}