package common

import chisel3._
import chisel3.util._

case class OffsetMem(raw_data: UInt, size_order: UInt, zero: Bool, max_size: Int) {
    // printf(p">>>> raw_data: 0x${Hexadecimal(raw_data)}, size_order: $size_order, sext? ${zero === 0.U}\n")
    private val size = Wire(UInt(max_size.W))
    size := size_order

    private def pick(min_order: Int): UInt = {
        var res = raw_data

        // Fullサイズが0だとHardware例外が送出される
        // そのためハーフワードからスタートする必要がある
        for (i <- (max_size >> 4)-1 to min_order by -1) {
            val pos = 8 << i
            val fil = Mux(zero, 0.U, res(pos-1))

            res = Mux(size === i.U, Cat(Fill(max_size-pos, fil), res(pos-1, 0)), res)
            // printf(p">>>> [$i] res: ${Hexadecimal(res)}, size: $size\n")
        }

        res
    }

    val data = pick(0)
}
