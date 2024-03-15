package jump

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val jal_config = () => {
            val cpu = new Top(
                exit_addr = 0x4c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
            cpu
        }

        test(jal_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val jalr_config = () => {
            val cpu = new Top(
                exit_addr = 0x190.U(Consts.WORD_LEN.W),
                start_addr = 0x17c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
            cpu
        }

        test(jalr_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}