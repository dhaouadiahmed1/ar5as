#!/usr/bin/env python3
"""Decode product photos for Ar5as v0.2.

Why this exists: the photos are stored in the repo as base64 TEXT
(app/images-b64/*.b64) because the GitHub App snapshot push used by Muse
only accepts UTF-8 text files — binary JPGs cannot be pushed that way.
This script decodes them back to JPGs in app/src/main/res/drawable/
before the resources are compiled.

Run automatically:
  - Gradle: the `decodeImages` task in app/build.gradle.kts (preBuild).
  - Manual build: build-manual.sh calls this script first.

Also safe to run by hand: it is idempotent (re-decodes every file).
"""
import base64
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC_DIR = ROOT / "app" / "images-b64"
DST_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable"


def main() -> int:
    if not SRC_DIR.is_dir():
        print(f"images-b64 dir not found: {SRC_DIR}", file=sys.stderr)
        return 1
    b64_files = sorted(SRC_DIR.glob("*.b64"))
    if not b64_files:
        print(f"no .b64 files in {SRC_DIR}", file=sys.stderr)
        return 1
    DST_DIR.mkdir(parents=True, exist_ok=True)
    decoded = 0
    for src in b64_files:
        dst = DST_DIR / (src.stem + ".jpg")
        try:
            text = "".join(src.read_text(encoding="utf-8").split())
            data = base64.b64decode(text, validate=True)
        except Exception as e:
            print(f"ERROR decoding {src.name}: {e}", file=sys.stderr)
            return 1
        if not data.startswith(b"\xff\xd8\xff"):
            print(f"ERROR {src.name}: decoded data is not a JPEG", file=sys.stderr)
            return 1
        dst.write_bytes(data)
        decoded += 1
    print(f"decode_images: decoded {decoded} images -> {DST_DIR}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
