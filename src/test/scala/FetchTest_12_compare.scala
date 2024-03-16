package compare

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val slt_config = () => {
            val cpu = new Top(
                exit_addr = 0x610.U(Consts.WORD_LEN.W), 
                start_addr = 0x600.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-slt.hex")
            cpu
        }

        test(slt_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val sltu_config = () => {
            val cpu = new Top(
                exit_addr = 0x610.U(Consts.WORD_LEN.W), 
                start_addr = 0x600.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-sltu.hex")
            cpu
        }

        test(sltu_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val slti_config = () => {
            val cpu = new Top(
                exit_addr = 0x3e4.U(Consts.WORD_LEN.W), 
                start_addr = 0x3d0.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-slti.hex")
            cpu
        }

        test(slti_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val sltiu_config = () => {
            val cpu = new Top(
                exit_addr = 0x3e4.U(Consts.WORD_LEN.W), 
                start_addr = 0x3d0.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-sltiu.hex")
            cpu
        }

        test(sltiu_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}
