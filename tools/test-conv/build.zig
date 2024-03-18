const std = @import("std");

pub fn build(b: *std.Build) void {
    const bin_step = b.step("hex", "Convert to raw binary");

    const test_dirs = [_][]const u8{ "riscv-tests/isa/rv32ui", "riscv-tests-mi"};

    for (test_dirs) |test_dir_path| {
        var test_dir = std.fs.cwd().openDir(test_dir_path, .{}) catch @panic("Test folder not found");
        defer test_dir.close();

        var test_files_iter = test_dir.walk(b.allocator) catch @panic("Can not open Test folder");
        defer test_files_iter.deinit();

        while (test_files_iter.next() catch @panic("Can not iterate Test folder")) |e| {
            if (std.mem.endsWith(u8, e.basename, ".dump")) { continue; }

            const elf_file_path = b.pathJoin(&[_][]const u8{ test_dir_path, e.basename });

            const basename = e.basename;
            const bin_name = std.fmt.allocPrint(b.allocator, "{s}.bin", .{basename}) catch @panic("OOM");
            const hex_name = std.fmt.allocPrint(b.allocator, "{s}.hex", .{basename}) catch @panic("OOM");

            const bin = b.addObjCopy(
                .{ .cwd_relative = b.allocator.dupe(u8, elf_file_path) catch @panic("OOM") },
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
}
