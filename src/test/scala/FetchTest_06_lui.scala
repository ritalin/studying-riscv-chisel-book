package lui

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val lui_config = () => {
            val cpu = new Top(
                exit_addr = 0xec.U(Consts.WORD_LEN.W), 
                start_addr = 0xe8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
            cpu
        }

        test(lui_config()) { c => 
            c.clock.step(1)
        }

        val auipc_config = () => {
            val cpu = new Top(
                exit_addr = 0x40.U(Consts.WORD_LEN.W), 
                start_addr = 0x3c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-jalr.hex")
            cpu
        }

        test(auipc_config()) { c => 
            c.clock.step(1)
        }
    }
}