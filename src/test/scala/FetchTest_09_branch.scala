package branch

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val bne_config = () => {
            val cpu = new Top(
                exit_addr = 0x650.U(Consts.WORD_LEN.W),
                start_addr = 0x648.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-add.hex")
            cpu
        }

        test(bne_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val beq_config = () => {
            val cpu = new Top(
                exit_addr = 0x430.U(Consts.WORD_LEN.W),
                start_addr = 0x408.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-beq.hex")
            cpu
        }

        test(beq_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val blt_config = () => {
            val cpu = new Top(
                exit_addr = 0x258.U(Consts.WORD_LEN.W),
                start_addr = 0x24c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-blt.hex")
            cpu
        }

        test(blt_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val bge_config = () => {
            val cpu = new Top(
                exit_addr = 0x2b8.U(Consts.WORD_LEN.W),
                start_addr = 0x2ac.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-bge.hex")
            cpu
        }

        test(bge_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
        val bltu_config = () => {
            val cpu = new Top(
                exit_addr = 0x260.U(Consts.WORD_LEN.W),
                start_addr = 0x250.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-bltu.hex")
            cpu
        }

        test(bltu_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val bgeu_config = () => {
            val cpu = new Top(
                exit_addr = 0x2c0.U(Consts.WORD_LEN.W),
                start_addr = 0x2b0.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-bgeu.hex")
            cpu
        }

        test(bgeu_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        // val jalr_config = () => {
        //     val cpu = new Top(
        //         exit_addr = 0x190.U(Consts.WORD_LEN.W),
        //         start_addr = 0x17c.U(Consts.WORD_LEN.W)
        //     )
        //     cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
        //     cpu
        // }

        // test(jalr_config()) { c => 
        //     while (!c.io.exit.peek().litToBoolean) {
        //         c.clock.step(1)
        //     }
        // }
    }
}