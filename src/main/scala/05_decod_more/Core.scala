package decoder

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

    // [R format] - call instruction etc
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +---------------------|--------------+--------------+--------+--------------+---------------------+
    // | funct7              | rs2          | rs1          | funct3 | rd           | opcode              |
    // +---------------------|--------------+--------------+--------+--------------+---------------------+

    // [I format] load memory etc
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +------------------------------------+--------------+--------+--------------+---------------------+
    // | imm_i                              | rs1          | funct3 | rd           | opcode              |
    // +------------------------------------+--------------+--------+--------------+---------------------+

    // [S format] - store memory etc
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
    
    // 命令のルックアップテーブルを構築する
    // ストアの可否もここで判定してしまう
    val csignals = ListLookup(inst, 
        (
            List(ALU_X, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X) // default
        ), 
        Array( // mappings
            LW -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM), // rs1_data + imm_i_sext
            SW -> List(ALU_ADD, OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X)  // rs1_data + imm_s_sext
        )
    )
    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: mem_ren :: wb_sel :: Nil = csignals

    val op1_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op1_sel === OP1_RS1) -> rs1_data
    ))
    val op2_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op2_sel === OP2_RS2) -> rs2_data,
        (op2_sel === OP2_IMI) -> imm_i_sext,
        (op2_sel === OP2_IMS) -> imm_s_sext
    ))

    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (exe_fun === ALU_ADD) -> (op1_data + op2_data)
    ))

    io.dmem.addr := alu_out
    io.dmem.wen := mem_wen
    io.dmem.wdata := rs2_data // rs2レジスタの値（アドレス）に書き込む

    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> io.dmem.rdata
    ))
    when (mem_ren === REN_S) {
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
