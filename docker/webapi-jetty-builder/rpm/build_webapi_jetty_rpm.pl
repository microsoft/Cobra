#!/usr/bin/perl -w

use strict;
use FindBin 1.51 qw( $RealBin );
use lib $RealBin;

use guidelines;
use rpm_set_owner;
use Getopt::Long;


my $oRPM 		= new rpm();
my $szName 		= "jetty";
my $szLongname 		= "Nuance Jetty";
my $szTopdir 		= "/tmp/rpm_builds";
my $szFiles 		= "/tmp/rpm_build_files";
my $szVer 		= "1.0.0";
my $szRel 		= GenShortdate();
my $szArch 		= "noarch";
#my $szArch 		= "i386"; # force 32-bit target
my $szScriptDir		= "/tmp/scripts";
my $szScriptletFile 	= "/tmp/Nuance_jetty_scriptlets.sh";
#my $szDependsOn	= "Nuance-Common >= 10.1.2, Nuance-Meaning-Extraction, jdk >= 1.7.0_17";
my $szRPMFile 		= "";
my $szDescFile          = "";
my $szOwner             = "njetty";

GetOptions(
            "ShortName=s"       	=> \$szName,
            "LongName=s"     		=> \$szLongname,
            "BuildDir=s"    		=> \$szTopdir,
            "FilesDir=s"    		=> \$szFiles,
            "Version=s"    		=> \$szVer,
            "BuildTime=s"		=> \$szRel,
            "Arch=s"    		=> \$szArch,
            "ScriptDir=s"    		=> \$szScriptDir,
            "DescFile=s" 		=> \$szDescFile,
            "ScriptletFile=s"    	=> \$szScriptletFile);


$oRPM->topdir($szTopdir);
$oRPM->files($szFiles);
$oRPM->formattedname($szLongname);
$oRPM->name($szName);
$oRPM->ver($szVer);
$oRPM->rel($szRel);
$oRPM->postprefix("/Nuance/webapi");
$oRPM->arch($szArch);
$oRPM->scriptlets($szScriptletFile);
$oRPM->descfile($szDescFile);
$oRPM->owner($szOwner);
$oRPM->loglevel(2);

#$oRPM->customdeps(1);
#$oRPM->scriptsource($szScriptDir);
$szRPMFile = $oRPM->Build();

if ($szRPMFile eq "")
{
    print "The RPM failed to build.\n";
}
else
{
    print "The RPM file is here: $szRPMFile\n";
}
