# RISC-VとChiselで学ぶはじめてのCPU自作の写経

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

## 疑問点

### Ch.7

* `rs1_addr`と`rs2_addr`の取り出しで警告が出る。

> [W002] Dynamic index with width 6 is too large for extractee of width 1

