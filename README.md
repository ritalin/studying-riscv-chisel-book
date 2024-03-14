# RISC-VとChiselで学ぶはじめてのCPU自作の写経

https://direct.gihyo.jp/view/item/000000001593?category_page_id=programming

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
* 書籍では、hexファイルは12行目まで入力している。しかし2つめの命令がアドレス0x16を指すが存在しないため、一つ前の結果(0x22222222)となる
  * 追加で4行分、0で埋めた (0x16のデータ)

### Ch.12

書籍では、一気に実装してからテストを回していたが、失敗するのが目に見えているので一歩ずつ進める。

`riscv-tests`を回す上で、`_start`のエントリーポイントをコールしているため、`Ch.16`のジャンプ命令から・・・
といきたかったが、デコードの共通化が行われた後なので、先に`Ch.12`を消化する。

* 警備ではあるが、`Consts.scala`の`RSXXXXX`の定義値を変更している
  * パターンマッチの判定用のため、さしたる影響はない。

## 疑問点

### Ch.6

* `rs1_addr`と`rs2_addr`の取り出しで警告が出る。

> [W002] Dynamic index with width 6 is too large for extractee of width 1

