package jump

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class Core(pc_reg_exit: UInt, pc_reg_start: UInt = 0.U(WORD_LEN.W)) extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPortIo())
        val dmem = Flipped(new DmemPortIo())
        val exit = Output(Bool())
    })

    val regfile = Mem(32, UInt(WORD_LEN.W)) // Riscvは32本のレジスタを持つ

    val pc_reg = RegInit(START_ADDR + pc_reg_start)
    val alu_out = Wire(UInt(WORD_LEN.W))

    io.imem.addr := pc_reg
    val inst = io.imem.inst

    val pc_plus4 = pc_reg + 4.U(WORD_LEN.W)
    val jmp_flg = (inst === JAL | inst === JALR)

    pc_reg := MuxCase(pc_plus4, Seq(
        jmp_flg -> alu_out // 1サイクル前のジャンプ命令の結果を割り当てる
    ))  

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

    // [U format] - lui, auipc
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +------------------------------------------------------------+--------------+---------------------+
    // | imm_u(31:12)                                               | rd           | opcode              |
    // +------------------------------------------------------------+--------------+---------------------+

    // [J format] - jump etc
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +---+-----------------------------+--+-----------------------+--------------+---------------------+
    // | imm_j(20 ++ 10:1 ++ 11 ++ 19:12)                           | rd           | opcode              |
    // +---+-----------------------------+--+-----------------------+--------------+---------------------+

    val rs1_data = Mux((rs1_addr =/= 0.U(WORD_LEN.U)), regfile(rs1_addr), 0.U(WORD_LEN.W))
    val rs2_data = Mux((rs2_addr =/= 0.U(WORD_LEN.U)), regfile(rs2_addr), 0.U(WORD_LEN.W))

    val imm_i = inst(31, 20)
    val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i) // 31bit目の値を20回繰り返し、ついでimm_iの値を連結する（合計32bit)

    val imm_s = Cat(inst(31, 25), inst(11, 7))
    val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s) // 31bit目の値を20回繰り返し、ついでimm_sの値を連結する（合計32bit)
    
    val imm_u = inst(31, 12)
    val imm_u_shifted = Cat(imm_u, Fill(12, 0.U)) // 1bit幅で12bit分の下位を埋める

    val imm_j = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21))
    val imm_j_sext = Cat(Fill(11, imm_j(19)), imm_j, 0.U)

    // 命令のルックアップテーブルを構築する
    // ストアの可否もここで判定してしまう
    val csignals = ListLookup(inst, 
        (
            List(ALU_X, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X) // default
        ), 
        Array( // mappings
            LW -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM), // rs1_data + imm_i_sext
            SW -> List(ALU_ADD, OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X),  // rs1_data + imm_s_sext
            ADD -> List(ALU_ADD, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            ADDI -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU),
            SUB -> List(ALU_SUB, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            LUI -> List(ALU_ADD, OP1_X, OP2_IMU, MEN_X, REN_S, WB_ALU),
            AUIPC -> List(ALU_ADD, OP1_PC, OP2_IMU, MEN_X, REN_S, WB_ALU),            
            JAL -> List(ALU_ADD, OP1_PC, OP2_IMJ, MEN_X, REN_S, WB_PC), // ジャンプ先の早退アドレスが計算済みのため加算命令を流用できる
            JALR -> List(ALU_JALR, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_PC)
        )
    )
    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: mem_ren :: wb_sel :: Nil = csignals

    val op1_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op1_sel === OP1_RS1) -> rs1_data,
        (op1_sel === OP1_PC) -> pc_reg, 
    ))
    val op2_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op2_sel === OP2_RS2) -> rs2_data,
        (op2_sel === OP2_IMI) -> imm_i_sext,
        (op2_sel === OP2_IMS) -> imm_s_sext, 
        (op2_sel === OP2_IMU) -> imm_u_shifted,
        (op2_sel === OP2_IMJ) -> imm_j_sext, 
    ))

    alu_out := MuxCase(0.U(WORD_LEN.W), Seq(
        (exe_fun === ALU_ADD) -> (op1_data + op2_data),
        (exe_fun === ALU_SUB) -> (op1_data - op2_data),
        (exe_fun === ALU_JALR) -> ((op1_data + op2_data) & ~1.U(WORD_LEN.W)).asUInt, // &の結果はBoolになるため、UIntへの変換が必要
    ))

    io.dmem.addr := alu_out
    io.dmem.wen := mem_wen
    io.dmem.wdata := rs2_data // rs2レジスタの値（アドレス）に書き込む

    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> io.dmem.rdata,
        (wb_sel === WB_PC) -> pc_plus4,
    ))
    when (mem_ren === REN_S) {
        regfile(wb_addr) := wb_data
    }

    //

    io.exit := (pc_reg === pc_reg_exit)

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
    printf(p"exe_fun : ${exe_fun}\n")
    printf(p"op1_data : 0x${Hexadecimal(op1_data)}\n")
    printf(p"op2_data : 0x${Hexadecimal(op2_data)}\n")
    printf(p"alu_out : 0x${Hexadecimal(alu_out)}\n")
printf(p"exit req : 0x${Hexadecimal(pc_reg_exit)} (${io.exit})\n")   
    printf("----------\n")
}
