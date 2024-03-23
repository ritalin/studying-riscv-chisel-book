package run_test_all

import chisel3._

import org.scalatest._
import chiseltest._
import common.Consts
import java.nio.file.{Files, Paths}

class RiscvTest extends flatspec.AnyFlatSpec with ChiselScalatestTester {
        Files.newDirectoryStream(Paths.get("tools/test-conv/zig-out/bin/"), f => { 
            val path = f.toString()
            path.endsWith(".hex") && path.contains("ui-p-")
        }).forEach { f => 
    s"mycpu" should s"works through hex of `$f`" in {
            println(s"<<<< ${f} >>>>") 
            
            val csrrw_config = () => {
                val cpu = new run_test.Top(
                    quiet = true,
                )
                cpu.memory.loadFrom(f.toString())
                cpu
            }

            test(csrrw_config()) { c => 
                while (!c.io.exit.peek().litToBoolean) {
                    c.clock.step(1)
                }
            }
        }
    }
}