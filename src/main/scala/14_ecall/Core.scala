package ecall

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._
import chisel3.probe.RWProbe

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

    val br_flg = Wire(Bool())
    val br_target = Wire(UInt(WORD_LEN.W))

    val csr_regfile = Mem(4096, UInt(WORD_LEN.W))

    pc_reg := MuxCase(pc_plus4, Seq(
        jmp_flg -> alu_out, // 1サイクル前のジャンプ命令の結果を割り当てる
        br_flg -> br_target, // EXステージでジャンプ先が確定した場合
        (inst === ECALL) -> csr_regfile(CSR_MTVEC),
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

    // [B format] - branch
    // +-------------------------------------------------------------------------------------------------+
    // | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    // +---+-----------------+--------------+--------------+--------+-----------+--+---------------------+
    // | imm_b(12 + 10:5)    | rs2          | rs1          | funct3 | imm_b(4:1+11)| opcode              |
    // +---+-----------------+--------------+--------------+--------+-----------+--+---------------------+

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

    val imm_b = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8))
    val imm_b_sext = Cat(Fill(19, imm_b(11)), imm_b, 0.U)

    val imm_z = inst(19, 15)
    val imm_z_sext = Cat(Fill(27, 0.U), imm_z)

    // 命令のルックアップテーブルを構築する
    // ストアの可否もここで判定してしまう
    val csignals = ListLookup(inst, 
        (
            List(ALU_X, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X, CSR_X) // default
        ), 
        Array( // mappings
            LW -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X), // rs1_data + imm_i_sext
            SW -> List(ALU_ADD, OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X, CSR_X),  // rs1_data + imm_s_sext
            ADD -> List(ALU_ADD, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X),
            ADDI -> List(ALU_ADD, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X),
            LUI -> List(ALU_ADD, OP1_X, OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X),
            AUIPC -> List(ALU_ADD, OP1_PC, OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X),            
            JAL -> List(ALU_ADD, OP1_PC, OP2_IMJ, MEN_X, REN_S, WB_PC, CSR_X), // ジャンプ先の早退アドレスが計算済みのため加算命令を流用できる
            JALR -> List(ALU_JALR, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_PC, CSR_X),
        )
        // 分岐命令
        ++ Array((BEQ, ALU_BR_BEQ), (BNE, ALU_BR_BNE), (BLT, ALU_BR_BLT), (BGE, ALU_BR_BGE), (BLTU, ALU_BR_BLTU), (BGEU, ALU_BR_BGEU)).map { 
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X, CSR_X) 
        }
        // 論理演算命令
        ++ Array((AND, ALU_AND), (OR, ALU_OR), (XOR, ALU_XOR)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        ++ Array((ANDI, ALU_AND), (ORI, ALU_OR), (XORI, ALU_XOR)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        // シフト演算命令
        ++ Array((SLL, ALU_SLL), (SRL, ALU_SRL), (SRA, ALU_SRA)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        ++ Array((SLLI, ALU_SLL), (SRLI, ALU_SRL), (SRAI, ALU_SRA)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        // 比較命令
        ++ Array((SLT, ALU_SLT), (SLTU, ALU_SLTU)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        ++ Array((SLTI, ALU_SLT), (SLTIU, ALU_SLTU)).map {
            case (pat, alu) => pat -> List(alu, OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X)
        }
        // CSR命令
        ++ Array(
            CSRRW -> List(ALU_COPY1, OP1_RS1, OP2_X, MEN_X, REN_S, WB_CSR, CSR_W),
            CSRRWI -> List(ALU_COPY1, OP1_IMZ, OP2_X, MEN_X, REN_S, WB_CSR, CSR_W),
            CSRRS -> List(ALU_COPY1, OP1_RS1, OP2_X, MEN_X, REN_S, WB_CSR, CSR_S),
            CSRRSI -> List(ALU_COPY1, OP1_IMZ, OP2_X, MEN_X, REN_S, WB_CSR, CSR_S),
            CSRRC -> List(ALU_COPY1, OP1_RS1, OP2_X, MEN_X, REN_S, WB_CSR, CSR_C),
            CSRRCI -> List(ALU_COPY1, OP1_IMZ, OP2_X, MEN_X, REN_S, WB_CSR, CSR_C),
        )
        ++ Array(
            ECALL -> List(ALU_ECALL, OP1_X, OP2_X, MEN_X, REN_X, WB_X, CSR_E),
        )
    )
    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: mem_ren :: wb_sel :: csr_cmd :: Nil = csignals

    val op1_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op1_sel === OP1_RS1) -> rs1_data,
        (op1_sel === OP1_PC) -> pc_reg, 
        (op1_sel === OP1_IMZ) -> imm_z_sext,
    ))
    val op2_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op2_sel === OP2_RS2) -> rs2_data,
        (op2_sel === OP2_IMI) -> imm_i_sext,
        (op2_sel === OP2_IMS) -> imm_s_sext, 
        (op2_sel === OP2_IMU) -> imm_u_shifted,
        (op2_sel === OP2_IMJ) -> imm_j_sext, 
    ))


    br_flg := MuxCase(false.B, Seq(
        (exe_fun === ALU_BR_BEQ) -> (op1_data === op2_data),
        (exe_fun === ALU_BR_BNE) -> (op1_data =/= op2_data),
        (exe_fun === ALU_BR_BLT) -> (op1_data.asSInt < op2_data.asSInt),
        (exe_fun === ALU_BR_BGE) -> (op1_data.asSInt >= op2_data.asSInt),
        (exe_fun === ALU_BR_BLTU) -> (op1_data < op2_data),
        (exe_fun === ALU_BR_BGEU) -> (op1_data >= op2_data),
    ))
    br_target := pc_reg + imm_b_sext

    alu_out := MuxCase(0.U(WORD_LEN.W), Seq(
        (exe_fun === ALU_ADD) -> (op1_data + op2_data),
        (exe_fun === ALU_JALR) -> ((op1_data + op2_data) & ~1.U(WORD_LEN.W)).asUInt, // &の結果はBoolになるため、UIntへの変換が必要
        (exe_fun === ALU_AND) -> (op1_data & op2_data),
        (exe_fun === ALU_OR) -> (op1_data | op2_data),
        (exe_fun === ALU_XOR) -> (op1_data ^ op2_data),
        (exe_fun === ALU_SLL) -> (op1_data << op2_data(4, 0))(31, 0),
        (exe_fun === ALU_SRL) -> (op1_data >> op2_data(4, 0)).asUInt,
        (exe_fun === ALU_SRA) -> (op1_data.asSInt >> op2_data(4, 0)).asUInt,
        (exe_fun === ALU_SLT) -> (op1_data.asSInt < op2_data.asSInt).asUInt,
        (exe_fun === ALU_SLTU) -> (op1_data < op2_data).asUInt,
        (exe_fun === ALU_COPY1) -> op1_data,
    ))

    io.dmem.addr := alu_out
    io.dmem.wen := mem_wen
    io.dmem.wdata := rs2_data // rs2レジスタの値（アドレス）に書き込む

    val csr_addr = MuxCase(inst(31, 20), Seq(
        (csr_cmd === CSR_E) -> CSR_MCAUSE
    ))

    val csr_rdata = csr_regfile(csr_addr)

    val csr_wdata = MuxCase(0.U(WORD_LEN.W), Seq(
        (csr_cmd === CSR_W) -> op1_data,
        (csr_cmd === CSR_S) -> (op1_data | csr_rdata),
        (csr_cmd === CSR_C) -> (~op1_data & csr_rdata), // op1_data反転して消さない状態のフラグを立て、元の状態と論理積をとる
        (csr_cmd === CSR_E) -> PRIV_MODE_M,
    ))

    when (csr_cmd > 0.U(WORD_LEN.W)) {
        csr_regfile(csr_addr) := csr_wdata
    }

    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> io.dmem.rdata,
        (wb_sel === WB_PC) -> pc_plus4,
        (wb_sel === WB_CSR) -> csr_rdata,
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
