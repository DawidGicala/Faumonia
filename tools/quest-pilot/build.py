"""Isolated Java 8 build. Does not run Ant init, edit defaults or open a database."""
import argparse
import os
from pathlib import Path
import subprocess
import re


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, required=True)
    parser.add_argument('--test', action='store_true')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    out = args.output.resolve()
    out.mkdir(parents=True, exist_ok=True)
    source = root / 'src'
    generated = out / 'generated/games/stendhal/client/StendhalBuild.java'
    generated.parent.mkdir(parents=True, exist_ok=True)
    generated.write_text('package games.stendhal.client; public class StendhalBuild {'
                         'public static String getBuildNumber() { return "quest-pilot-local"; }}',
                         encoding='utf-8')
    common = list((source / 'games/stendhal/common').rglob('*.java'))
    server = [p for p in (source / 'games/stendhal/server').rglob('*.java')
              if 'script' not in p.relative_to(source / 'games/stendhal/server').parts]
    client = list((source / 'games/stendhal/client').rglob('*.java'))
    libs = root / 'libs'
    server_cp = os.pathsep.join(str(p) for p in sorted(libs.glob('*.jar')))
    client_cp = os.pathsep.join(str(libs / p) for p in ('marauroa.jar', 'log4j.jar', 'jorbis.jar'))

    def compile_sources(label, files, cp):
        dest = out / label
        dest.mkdir(exist_ok=True)
        options = ['-encoding', 'UTF-8', '-source', '1.8', '-target', '1.8',
                   '-d', str(dest), '-classpath', cp, '-sourcepath', str(source)]
        arguments = out / (label + '.args')
        # javac reads @files with the Windows default encoding (the sources remain UTF-8).
        arguments.write_text('\n'.join('"' + str(p).replace('\\', '/') + '"'
                                       for p in options + sorted(map(str, files))), encoding='mbcs' if os.name == 'nt' else 'utf-8')
        print('Compiling ' + label, flush=True)
        result = subprocess.run([str(args.java_home / 'bin/javac'), '@' + str(arguments)],
                                cwd=out, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        (out / (label + '-compile.log')).write_bytes(result.stdout)
        if result.returncode:
            print(result.stdout.decode(errors='replace'))
            raise SystemExit(result.returncode)
        print(label + ': OK', flush=True)
        return dest

    server_dest = compile_sources('server', common + server, server_cp)
    client_dest = compile_sources('client', common + client + [generated], client_cp)
    if args.test:
        tests = list((root / 'tests/quest-pilot').rglob('*.java'))
        if not tests:
            raise SystemExit('No quest pilot tests found')
        properties = (root / 'build.ant.properties').read_text(encoding='utf-8-sig')
        version_match = re.search(r'^version\s*=\s*(\S+)', properties, re.MULTILINE)
        if not version_match:
            raise SystemExit('Missing version in build.ant.properties')
        resources = sorted((root / 'build/lib').glob('*-data-' + version_match.group(1) + '.jar'))
        cp = os.pathsep.join(map(str, [server_dest, client_dest, server_cp, source, root] + resources))
        test_dest = compile_sources('tests', tests, cp)
        test_cp = os.pathsep.join([str(out), str(test_dest), cp])
        logging = out / 'log4j.properties'
        logging.write_text('log4j.rootLogger=INFO,pilot\n'
                           'log4j.appender.pilot=org.apache.log4j.FileAppender\n'
                           'log4j.appender.pilot.File=' + (out / 'runtime.log').as_posix() + '\n'
                           'log4j.appender.pilot.Append=false\n'
                           'log4j.appender.pilot.layout=org.apache.log4j.PatternLayout\n'
                           'log4j.appender.pilot.layout.ConversionPattern=%p %c - %m%n\n', encoding='utf-8')
        names = [str(p.relative_to(root / 'tests/quest-pilot')).replace('\\', '.')[:-5]
                 for p in tests if p.name.endswith('Test.java')]
        result = subprocess.run([str(args.java_home / 'bin/java'), '-Djava.awt.headless=false', '-Dfile.encoding=UTF-8',
                                 '-Dlog4j.configuration=' + logging.as_uri(),
                                 '-Dquest.pilot.output=' + str(out), '-cp', test_cp,
                                 'org.junit.runner.JUnitCore'] + sorted(names), cwd=out,
                                stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        (out / 'tests.log').write_bytes(result.stdout)
        report = result.stdout.decode('utf-8', errors='replace')
        summary = re.search(r'OK \(\d+ tests\)', report)
        print(summary.group(0) if result.returncode == 0 and summary else report[-12000:])
        print('Full test log: ' + str(out / 'tests.log'))
        raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
