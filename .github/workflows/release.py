#!/usr/bin/env python3
import subprocess

with open("build.gradle.kts", "r+") as f:
    lines = ""

    for line in f.readlines():
        if line.startswith("version = "):
            version = line[11:-2]
            version_new = (
                f'{version.split(".")[0]}.{str(int(version.split(".")[1]) + 1)}'
            )
            line = f'version = "{version_new}"\n'

        lines += line

    f.seek(0)
    f.write(lines)

with open("example/settings.gradle.kts", "r+") as f:
    lines = f.read().replace(f"/v{version}/", f"/v{version_new}/")
    f.seek(0)
    f.write(lines)

subprocess.run(["git", "tag", f"v{version}"])
subprocess.run(["git", "add", "build.gradle.kts", "example/settings.gradle.kts"])
subprocess.run(["git", "commit", "-m", "Bump version"])
