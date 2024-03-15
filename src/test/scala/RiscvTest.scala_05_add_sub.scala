package add_sub

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val addi_config = () => {
            val cpu = new Top(
                exit_addr = 0xf0.U(Consts.WORD_LEN.W), 
                start_addr = 0xe8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
            cpu
        }

        test(addi_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val add_config = () => {
            val cpu = new Top(
                exit_addr = 0x264.U(Consts.WORD_LEN.W), 
                start_addr = 0x248.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-add.hex")
            cpu
        }

        test(add_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}
