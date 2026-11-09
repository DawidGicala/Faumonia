"""Package a verified isolated build from this source tree, then check the real ZIP.

Does not edit server.ini, existing distributions or user databases. Output must be new.
"""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, textwrap, zipfile
import xml.etree.ElementTree as ET


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--build',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--java-home',type=Path,required=True)
    args=parser.parse_args()
    root=Path(__file__).resolve().parents[2]
    build=args.build.resolve();out=args.output.resolve();jdk=args.java_home
    assert not out.exists(),'Use a fresh packaging directory'
    assert 'OK (' in (build/'tests.log').read_text(encoding='utf-8'),'Tests must pass before packaging'
    sources=list((root/'src').rglob('*.java'))+list((root/'tests/quest-pilot').rglob('*.java'))
    assert all(p.stat().st_mtime<=(build/'tests.log').stat().st_mtime for p in sources),'Java changed after tests'
    out.mkdir(parents=True);lib=out/'lib';lib.mkdir();client=out/'client';(client/'lib').mkdir(parents=True)
    env=dict(os.environ,JAVA_TOOL_OPTIONS='-XX:TieredStopAtLevel=1')
    def run(label,cmd,cwd=out,extra=None):
        result=subprocess.run(cmd,cwd=cwd,env=dict(env,**(extra or {})),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=240)
        (out/(label+'.log')).write_bytes(result.stdout)
        if result.returncode:raise RuntimeError(label+'\n'+result.stdout.decode('utf-8',errors='replace')[-10000:])
        print(label+': OK',flush=True);return result.stdout.decode('utf-8',errors='replace')
    def manifest(main=None,cp=None):
        lines=['Manifest-Version: 1.0']
        for key,value in [('Main-Class',main),('Class-Path',cp)]:
            if value:lines+=textwrap.wrap(key+': '+value,70,subsequent_indent=' ',break_long_words=True,break_on_hyphens=False,replace_whitespace=False,drop_whitespace=False)
        return ('\r\n'.join(lines)+'\r\n\r\n').encode('ascii')
    def jar(path,entries,metadata=None):
        with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED)as z:
            z.writestr('META-INF/MANIFEST.MF',metadata or manifest())
            for name,p in sorted(entries.items()):z.write(p,name)
    def trees(directory,prefix='',predicate=lambda p:True):
        return {prefix+p.relative_to(directory).as_posix():p for p in directory.rglob('*') if p.is_file() and predicate(p)}
    def resources(directory):
        return trees(directory,predicate=lambda p:p.suffix not in ('.java','.class') and '.svn' not in p.parts)
    server_name='FaumoniaOnline-server-7.0.jar';maps_name='FaumoniaOnline-maps-7.0.jar';xml_name='FaumoniaOnline-xmlconf-7.0.jar'
    server=trees(build/'server',predicate=lambda p:p.suffix=='.class' and 'maps' not in p.relative_to(build/'server').parts)
    for part in ['games/stendhal/common','games/stendhal/server']:
        # Only common text and the server SQL are runtime resources in the server JAR.
        for p in (root/'src'/part).rglob('*.txt'):
            server[p.relative_to(root/'src').as_posix()]=p
    server['games/stendhal/server/stendhal_init.sql']=root/'src/games/stendhal/server/stendhal_init.sql'
    server['data/conf/stendhalcreateaccount.properties']=root/'data/conf/stendhalcreateaccount.properties'
    jar(lib/server_name,server,manifest('games.stendhal.server.StendhalServer',maps_name+' '+xml_name+' marauroa.jar commons-lang.jar log4j.jar groovy.jar simple.jar mysql-connector.jar h2.jar data/script/ data/conf/ ./'))
    maps=trees(build/'server/games/stendhal/server/maps','games/stendhal/server/maps/',lambda p:p.suffix=='.class')
    maps.update(trees(root/'tiled','data/maps/',lambda p:p.suffix=='.tmx'));jar(lib/maps_name,maps)
    config=trees(root/'data/conf','data/conf/',lambda p:p.suffix=='.xml')
    config.update(trees(root/'data/languages','data/languages/'));jar(lib/xml_name,config)
    classes=trees(build/'client',predicate=lambda p:p.suffix=='.class')
    for part in ['games/stendhal/common','games/stendhal/client']:
        classes.update({part+'/'+k:v for k,v in resources(root/'src'/part).items()})
    cp='lib/log4j.jar lib/marauroa.jar lib/jorbis.jar lib/FaumoniaOnline-data-7.0.jar lib/FaumoniaOnline-sound-data-7.0.jar lib/FaumoniaOnline-music-data-7.0.jar'
    jar(client/'FaumoniaOnline.jar',classes,manifest('games.stendhal.client.update.Starter',cp))
    assets={}
    for directory,prefix in [('data/sprites','data/sprites/'),('tiled/tileset','data/tileset/'),('data/gui','data/gui/')]:
        assets.update(trees(root/directory,prefix,lambda p:p.suffix.lower() not in ('.bz2','.gz','.psd','.xcf') and '.xcf.' not in p.name))
    assets['data/conf/log4j.properties']=root/'data/conf/log4j.properties'
    jar(client/'lib/FaumoniaOnline-data-7.0.jar',assets)
    for kind in ['sounds','music']:
        filename='FaumoniaOnline-'+('sound' if kind=='sounds' else 'music')+'-data-7.0.jar'
        jar(client/'lib'/filename,trees(root/'data'/kind,'data/'+kind+'/'))
    for name in ['marauroa.jar','log4j.jar','jorbis.jar']:shutil.copy2(root/'libs'/name,client/'lib'/name)
    shutil.copy2(root/'runclient-local.bat',client)
    ant_build=out/'ant-build';scripts=ant_build/'build_server_script';scripts.mkdir(parents=True)
    script_sources=sorted((root/'src/games/stendhal/server/script').glob('*.java'))
    options=['-encoding','UTF-8','-source','1.8','-target','1.8','-d',str(scripts),'-classpath',';'.join(map(str,[lib/server_name,lib/maps_name]+sorted((root/'libs').glob('*.jar'))))]
    argfile=out/'scripts.args';argfile.write_text('\n'.join('"'+str(v).replace('\\','/')+'"' for v in options+script_sources),encoding='mbcs')
    run('scripts-compile',[str(jdk/'bin/javac.exe'),'@'+str(argfile)])
    tree=ET.parse(root/'build.xml');project=tree.getroot();project.set('basedir',str(root))
    project.find("target[@name='dist_server_binary']").attrib.pop('depends')
    # The distribution target deletes only its generated intermediate directory.
    generated=(ant_build/'FaumoniaOnline-server-7.0').resolve()
    assert generated.parent==ant_build.resolve() and out in generated.parents
    antfile=out/'distribution.xml';tree.write(antfile,encoding='utf-8',xml_declaration=True)
    run('ant-distribution',[str(jdk/'bin/java.exe'),'-cp','C:/Program Files/apache-ant-1.9.16/lib/*;'+str(jdk/'lib/tools.jar'),'org.apache.tools.ant.Main','-f',str(antfile),'-Dbuildroot='+str(ant_build),'-Dlib='+str(lib),'dist_server_binary'])
    archive=ant_build/'FaumoniaOnline-server-7.0.zip';fresh=out/'server';fresh.mkdir()
    with zipfile.ZipFile(archive)as z:
        assert 'server.ini' not in z.namelist() and 'mysql-connector.jar' in z.namelist() and 'h2.jar' in z.namelist()
        for name in z.namelist():assert fresh.resolve() in (fresh/name).resolve().parents
        z.extractall(fresh)
    run('package-check',['cmd.exe','/d','/c',str(fresh/'runserver.bat'),'--check'],cwd=fresh,extra={'FAUMONIA_JAVA':str(jdk/'bin/java.exe'),'FAUMONIA_NO_PAUSE':'1'})
    with zipfile.ZipFile(lib/server_name)as s,zipfile.ZipFile(client/'FaumoniaOnline.jar')as c:
        for n in c.namelist():
            if n.startswith('games/stendhal/common/') and n.endswith('.class'):assert c.read(n)==s.read(n),n
    tests=sorted('.'.join(p.relative_to(root/'tests/quest-pilot').with_suffix('').parts) for p in (root/'tests/quest-pilot').rglob('*Test.java'))
    # No source paths or loose application classes: this suite uses only shipped JARs.
    tests_cp=';'.join(map(str,[build/'tests',client/'FaumoniaOnline.jar',fresh/'*',client/'lib/*',root/'libs/junit-4.4.jar',root/'libs/hamcrest-all-1.1.jar']))
    (out/'packaged-tests').mkdir()
    run('packaged-tests',[str(jdk/'bin/java.exe'),'-Dfile.encoding=UTF-8','-Djava.awt.headless=false','-Dquest.pilot.output='+str(out/'packaged-tests'),'-cp',tests_cp,'org.junit.runner.JUnitCore']+tests)
    shutil.copy2(archive,out/archive.name)
    hashes={str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
    (out/'source-hashes.json').write_text(json.dumps(hashes,ensure_ascii=False,indent=2),encoding='utf-8')
    (out/'validation.json').write_text(json.dumps({'source':str(root),'build':str(build),'source_tests':(build/'tests.log').read_text(encoding='utf-8')[-150:],'fresh_zip_check':True,'packaged_tests':(out/'packaged-tests.log').read_text(encoding='utf-8')[-150:],'database_started':False,'installed':False},ensure_ascii=False,indent=2),encoding='utf-8')
    print('Verified server ZIP and complete client: '+str(out),flush=True)


if __name__=='__main__':main()
