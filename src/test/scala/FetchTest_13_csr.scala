package csr

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val csrrw_config = () => {
            val cpu = new Top(
                exit_addr = 0x224.U(Consts.WORD_LEN.W), 
                start_addr = 0x0.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32mi-p-csr.hex")
            cpu
        }

        test(csrrw_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}