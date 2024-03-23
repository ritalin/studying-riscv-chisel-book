# RISC-VとChiselで学ぶはじめてのCPU自作の写経

* 書籍案内
  * https://direct.gihyo.jp/view/item/000000001593?category_page_id=programming

* 書籍公式レポジトリ
  * https://github.com/chadyuu/riscv-chisel-book

## 環境

* MacOS: Ventura
* JDK: 21.0.2
* sbt: 1.9.9
* scala: 2.13.12

環境構築は、`Home brew`を経由して、`sbt`をインストール

## chisel

* version: 6.2.0
* chseltest: 6.0.0

## 変更点

いくつかは書籍のままではビルドが通らなくなっている。

### Ch.4

* 書籍用に提供された`build.sbt`が古い。
  * [chiselの公式レポジトリ]()から提供されているテンプレートを使用する必要がある。
  * `chseltest`は最新バージョンを使用するよう変更。

### Ch.5

* `loadMemoryFromFile`はビルドは通るが、警告がでる。

> [WARNING] Unsupported annotation: LoadMemoryAnnotation

* 加えて、ファイルを適切に読み込んでもくれない。
  * `loadMemoryFromFileInline`にしたら解決した。

### Ch.6

* `org.scalatest.FlatSpec`が廃止されているためコンパイルエラーになる
  * `org.scalatest.flatspec.AnyFlatSpec`を使用することで解決

* 0x34333231でexitするよう実装されているがこれだと、2ステップ読んで終了となった
  * 書籍では3ステップ出力されるって書かれているけど
  * とりま、41~44を追加し、0x44434241でexitするようにしておいた。

### Ch.8

* `Top.scala`で`core`の`dmem`と`memory`の`dmem`を接続
  * 本文中には記載なし
  * https://www.rm48.net/post/risc-vとchiselで学ぶ-はじめての自作cpu-メモ

* ch.6同様、終了アドレスを`0x14131211`にすると2つ目の命令を読み込まないため、`0x22222222`に変更した。

### Ch.9

* ch.8同様、終了アドレスを`0x22222222`に変更した。
* 書籍では、hexファイルは12行目まで入力している。しかし2つめの命令がアドレス0x16を指すが存在しないため、一つ前の結果(0x22222222)となる。
  * 追加で4行分、0で埋めた (0x16のデータ)

### Ch.12

書籍では、一気に実装してからテストを回していたが、失敗するのが目に見えているので一歩ずつ進める。

`riscv-tests`を回す上で、`_start`のエントリーポイントをコールしているため、`Ch.16`のジャンプ命令から・・・
といきたかったが、デコードの共通化が行われた後なので、先に`Ch.12`を消化する。

* 警備ではあるが、`Consts.scala`の`RSXXXXX`の定義値を変更している。
  * パターンマッチの判定用のため、さしたる影響はない。

### Ch,17

> * `riscv-tests`は、Nightly版の[zig言語](https://ziglang.org/)を介して`hex`ファイルに変換。
>   * テストケースは、[cgshep/riscv-tests-prebuilt-binaries](https://github.com/cgshep/riscv-tests-prebuilt-binaries)を使用した。
> * `od`コマンドで`HEX`化
>   * MacOS版の`od`は、`w`パラメータが存在しないため、`sed`で加工した。
>
> `hex`ファイルの作成は、`tools/test-conv`フォルダ下で`zig build hex`とタイプすることで作成できる。

`jalr`命令は`auipc`で即値をレジスタに入れ、`addi`で上位アドレスを計算している。
そのため、即値命令を先行して実装する。

### Ch.10

`addi`は`Ch,17`の試験結果を踏まえるようにする。

### Ch.16

`_start`のエントリーポイントのコールを処理するためジャンプ命令を先行して実装する。
`jalr`命令は、`Ch.17`, `Ch.10`の結果を踏まえそうな箇所を選択した。

* JALRの実行でLSBに０を付与する際、&の結果はBoolになるため、UIntへの変換が必要
  * https://www.rm48.net/post/risc-vとchiselで学ぶ-はじめての自作cpu-メモ

### Ch.15

* 比較命令のルックアップが、ほとんど同じなため、mapで一括生成して連結した。

### Ch.11

* `Ch.15`同様ルックアップの共通化を行なっている。

### Ch.13

* `Ch.15`同様ルックアップの共通化を行なっている。

### Ch.14

* `Ch.15`同様ルックアップの共通化を行なっている。

### Ch.18

* I命令用の`imm_z_sext`の読み出しが本文に記載されていなかった（公式レポジトリのコードには記載あり）
* `cgshep/riscv-tests-prebuilt-binaries`には`csr`用のテストが含まれていなかったため、書籍公式レポジトリより持ってきた。
  * `chadyuu/riscv-chisel-book/target/share/riscv-tests/isa/rv32mi-p-csr`

### Ch.19

* 書籍公式レポジトリよりテストケースを持ってきた
  * `chadyuu/riscv-chisel-book/target/share/riscv-tests/isa/rv32mi-p-illegal`

### Ch.20

* ここまでの実装では、すべての`riscv-tests`をパスできない。
  * `LH`, `LHU`, `LBU`, `LB`が未定義のため。
    * ライトバック用に`OffsetMem`型を用意し、マスクと符号拡張を行うようにした。
  * `SB`, `SH`の定義も追加した
    * 出力のためのマスクは、`OffsetMem`型を使用した。

  * `rv32mi-p-illegal`の通過を試みたが、`test_vectored_interrupts`で無限ループするため諦めた。
    * `0x1cc`で決定される値と`0x1d0`で決定される値の`and`がどうしても`0`にならない。
    * `RiscvTest_all.scala`からも除外している

## 疑問点

### Ch.6

* `rs1_addr`と`rs2_addr`の取り出しで警告が出る。
  * サイズを推論させたら警告出なくなった

> [W002] Dynamic index with width 6 is too large for extractee of width 1

