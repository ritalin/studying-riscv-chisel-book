package shift

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val sll_config = () => {
            val cpu = new Top(
                exit_addr = 0x1bc.U(Consts.WORD_LEN.W), 
                start_addr = 0x1a8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-sll.hex")
            cpu
        }

        test(sll_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val srl_config = () => {
            val cpu = new Top(
                exit_addr = 0x1bc.U(Consts.WORD_LEN.W), 
                start_addr = 0x1a8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-srl.hex")
            cpu
        }

        test(srl_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val sra_config = () => {
            val cpu = new Top(
                exit_addr = 0x1bc.U(Consts.WORD_LEN.W), 
                start_addr = 0x1a8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-sra.hex")
            cpu
        }

        test(sra_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val slli_config = () => {
            val cpu = new Top(
                exit_addr = 0x214.U(Consts.WORD_LEN.W), 
                start_addr = 0x204.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-slli.hex")
            cpu
        }

        test(slli_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }    

        val srli_config = () => {
            val cpu = new Top(
                exit_addr = 0x220.U(Consts.WORD_LEN.W), 
                start_addr = 0x20c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-srli.hex")
            cpu
        }

        test(srli_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val srai_config = () => {
            val cpu = new Top(
                exit_addr = 0x1b0.U(Consts.WORD_LEN.W), 
                start_addr = 0x1a0.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-srai.hex")
            cpu
        }

        test(srai_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}