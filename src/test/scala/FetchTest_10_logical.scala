package logical

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts

class HexTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
    "mycpu" should "work through hex" in {
        val and_config = () => {
            val cpu = new Top(
                exit_addr = 0x334.U(Consts.WORD_LEN.W), 
                start_addr = 0x320.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-and.hex")
            cpu
        }

        test(and_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val or_config = () => {
            val cpu = new Top(
                exit_addr = 0x18c.U(Consts.WORD_LEN.W), 
                start_addr = 0x178.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-or.hex")
            cpu
        }

        test(or_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val xor_config = () => {
            val cpu = new Top(
                exit_addr = 0x1b0.U(Consts.WORD_LEN.W), 
                start_addr = 0x19c.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-xor.hex")
            cpu
        }

        test(xor_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val andi_config = () => {
            val cpu = new Top(
                exit_addr = 0x1c0.U(Consts.WORD_LEN.W), 
                start_addr = 0x1ac.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-andi.hex")
            cpu
        }

        test(andi_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }    

        val ori_config = () => {
            val cpu = new Top(
                exit_addr = 0x1e0.U(Consts.WORD_LEN.W), 
                start_addr = 0x1c8.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-ori.hex")
            cpu
        }

        test(ori_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }

        val xori_config = () => {
            val cpu = new Top(
                exit_addr = 0x190.U(Consts.WORD_LEN.W), 
                start_addr = 0x178.U(Consts.WORD_LEN.W)
            )
            cpu.memory.loadFrom("tools/test-conv/zig-out/bin/rv32ui-p-xori.hex")
            cpu
        }

        test(xori_config()) { c => 
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}