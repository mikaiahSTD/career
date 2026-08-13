package com.rmm.std.file.hash;

import com.rmm.std.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
