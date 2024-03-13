package store_word

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class Core extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPortIo())
        val dmem = Flipped(new DmemPortIo())
        val exit = Output(Bool())
    })

    val regfile = Mem(32, UInt(WORD_LEN.W))

    val pc_reg = RegInit(START_ADDR)

    io.imem.addr := pc_reg
    val inst = io.imem.inst
    pc_reg := pc_reg + 4.U(WORD_LEN.W)

    val rs1_addr = inst(19, 15) // rs1レジスタ
    val rs2_addr = inst(24, 20) // rs2レジスタ
    val wb_addr = inst(11, 7) // rdレジスタ

    // p.44 

    // [R format]
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +---------------------|--------------+--------------+--------+--------------+---------------------+
    // | funct7              | rs2          | rs1          | funct3 | rd           | opcode              |
    // +---------------------|--------------+--------------+--------+--------------+---------------------+

    // [I format]
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +------------------------------------+--------------+--------+--------------+---------------------+
    // | imm_i                              | rs1          | funct3 | rd           | opcode              |
    // +------------------------------------+--------------+--------+--------------+---------------------+

    // [S format]
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +---------------------+--------------+--------------+--------+--------------+---------------------+
    // | imm_s(11:5)         | rs2          | rs1          | funct3 | imm_s(4:0)   | opcode              |
    // +---------------------+--------------+--------------+--------+--------------+---------------------+

    val rs1_data = Mux((rs1_addr =/= 0.U(WORD_LEN.U)), regfile(rs1_addr), 0.U(WORD_LEN.W))
    val rs2_data = Mux((rs2_addr =/= 0.U(WORD_LEN.U)), regfile(rs2_addr), 0.U(WORD_LEN.W))

    val imm_i = inst(31, 20)
    val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i) // 31bit目の値を20回繰り返し、ついでimm_iの値を連結する（合計32bit)

    val imm_s = Cat(inst(31, 25), inst(11, 7))
    val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s) // 31bit目の値を20回繰り返し、ついでimm_sの値を連結する（合計32bit)

    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (inst === LW) -> (rs1_data + imm_i_sext),
        (inst === SW) -> (rs1_data + imm_s_sext)
    ))

    io.dmem.addr := alu_out
    io.dmem.wen := (inst === SW) // SW命令の時に有効にする
    io.dmem.wdata := rs2_data // rs2レジスタの値（アドレス）に書き込む

    val wb_data = io.dmem.rdata
    when (inst === LW) {
        regfile(wb_addr) := wb_data
    }

    //

    io.exit := (inst === 0x22222222.U(WORD_LEN.W))

    printf(p"pc_reg : 0x${Hexadecimal(pc_reg)}\n")
    printf(p"inst : 0x${Hexadecimal(inst)}\n")
    printf(p"rs1_addr : $rs1_addr\n")
    printf(p"rs2_addr : $rs2_addr\n")
    printf(p"wb_addr : $wb_addr\n")
    printf(p"rs1_data : 0x${Hexadecimal(rs1_data)}\n")
    printf(p"rs2_data : 0x${Hexadecimal(rs2_data)}\n")
    printf(p"wb_data : 0x${Hexadecimal(wb_data)}\n")
    printf(p"dmem.addr : ${io.dmem.addr}\n")
    printf(p"dmem.wen : ${io.dmem.wen}\n")
    printf(p"dmem.wdata : 0x${Hexadecimal(io.dmem.wdata)}\n")    
    printf("----------\n")
}
