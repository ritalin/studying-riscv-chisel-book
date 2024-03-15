const std = @import("std");

pub fn build(b: *std.Build) void {
    const bin_step = b.step("hex", "Convert to raw binary");

    const test_files = [_][]const u8{
        "./riscv-tests/isa/rv32ui/rv32ui-p-add",
        "./riscv-tests/isa/rv32ui/rv32ui-p-jalr",
    };

    for (test_files) |elf_file_path| {
        const basename = std.fs.path.stem(elf_file_path);
        const bin_name = std.fmt.allocPrint(b.allocator, "{s}.bin", .{basename}) catch @panic("OOM");
        const hex_name = std.fmt.allocPrint(b.allocator, "{s}.hex", .{basename}) catch @panic("OOM");

        const bin = b.addObjCopy(
            .{ .cwd_relative = elf_file_path },
            .{
                .basename = bin_name,
                .format = .bin
            }
        );

        const to_hex = b.addSystemCommand(&[_][]const u8{"od", "-An", "-tx1", "-v"});
        to_hex.addFileArg(bin.getOutput());
        const cap_hex = to_hex.captureStdOut();
        to_hex.step.dependOn(&bin.step);

        const to_chisel_hex = b.addSystemCommand(&[_][]const u8{"sed", "-e", "s/^[ \t]*//", "-e", "s/  /\\n/g"});
        to_chisel_hex.addFileArg(cap_hex);
        const cap_chisel_hex = to_chisel_hex.captureStdOut();
        to_chisel_hex.step.dependOn(&to_hex.step);

        const copy_bin = b.addInstallBinFile(cap_chisel_hex, hex_name);
        copy_bin.step.dependOn(&to_chisel_hex.step);

        bin_step.dependOn(&copy_bin.step);
    }
}
