"""Stage public client packages from a fresh Ant client ZIP, then build Inno Setup."""
from pathlib import Path
import argparse, hashlib, json, shutil, subprocess, zipfile

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--build',type=Path,required=True)
    parser.add_argument('--work',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--version',default='8.0')
    args=parser.parse_args()
    assets=Path(__file__).resolve().parent
    root=assets.parents[1]
    stage=args.work.resolve()/'stage'
    stage.mkdir(parents=True,exist_ok=False)
    args.output.mkdir(parents=True,exist_ok=True)
    win=stage/'windows'/'Faumonia-{}'.format(args.version)
    mac=stage/'macos'/'Faumonia-{}'.format(args.version)
    win.mkdir(parents=True)
    with zipfile.ZipFile(args.build/'FaumoniaOnline-FULL-{}.zip'.format(args.version)) as archive:
        assert archive.testzip() is None
        for info in archive.infolist():
            n=info.filename
            if info.is_dir(): continue
            if not (n=='FaumoniaOnline.jar' or n.startswith('lib/') or n in ['doc/AUTHORS.txt','doc/LICENSE.txt']): continue
            dest=(win/n).resolve()
            assert win.resolve() in dest.parents
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(archive.read(n))
    for filename in ['COPYING.txt','SEPARATE_LICENCE_GRAPHICS.txt','SEPARATE_LICENCE_MUSIC.txt']:
        if (root/filename).exists(): shutil.copy2(root/filename,win/'doc'/filename)
    shutil.copy2(assets/'logo.ico',win/'lib/logo.ico')
    (win/'README.txt').write_text((assets/'README-Windows.txt').read_text(encoding='utf-8'),encoding='utf-8-sig',newline='\r\n')
    (win/'Uruchom-Faumonie.bat').write_text((assets/'Uruchom-Faumonie.bat').read_text(encoding='ascii'),encoding='ascii',newline='\r\n')
    shutil.copytree(win,mac)
    (mac/'Uruchom-Faumonie.bat').unlink()
    (mac/'README.txt').write_text((assets/'README-Mac.txt').read_text(encoding='utf-8'),encoding='utf-8',newline='\n')
    (mac/'Start.command').write_text((assets/'Start.command').read_text(encoding='utf-8'),encoding='utf-8',newline='\n')
    zip_paths=[]
    for platform,folder in [('JAR',win),('macOS',mac)]:
        target=args.output/'Faumonia-{}-{}.zip'.format(args.version,platform)
        if target.exists(): raise FileExistsError(target)
        with zipfile.ZipFile(target,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
            for file in sorted(folder.rglob('*')):
                if not file.is_file(): continue
                entry=zipfile.ZipInfo.from_file(file,(Path(folder.name)/file.relative_to(folder)).as_posix())
                entry.create_system=3
                entry.external_attr=(0o100755 if file.name=='Start.command' else 0o100644)<<16
                archive.writestr(entry,file.read_bytes(),compress_type=zipfile.ZIP_DEFLATED,compresslevel=9)
        zip_paths.append(target)
    cmd=[r'C:\Program Files (x86)\Inno Setup 6\ISCC.exe',
         '/DClientDir='+str(win),'/DOutputDir='+str(args.output.resolve()),
         '/DAppVersion='+args.version,str(assets/'windows.iss')]
    result=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=180)
    (args.work/'inno-build.log').write_bytes(result.stdout)
    if result.returncode:
        print(result.stdout.decode('utf-8',errors='replace'))
        raise SystemExit(result.returncode)
    exe=args.output/'Faumonia-{}-Windows-Setup.exe'.format(args.version)
    assert exe.is_file()
    records=[{'name':p.name,'size':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in [exe]+zip_paths]
    (args.work/'packages.json').write_text(json.dumps({'windows_stage':str(win),'mac_stage':str(mac),'packages':records},indent=2),encoding='utf-8')
    print(json.dumps(records,indent=2))

if __name__=='__main__': main()
