#!/usr/bin/perl -w
# -------------------------------------------------------------------------------------------------
# Program: bldmfg.pl
# Purpose: To build and package the Cobra API Platform product.
# Authors: Enterprise R&D Release Engineering
# History: Refer to source control for complete change history.
# License: Copyright Nuance Communications, Inc.
# -------------------------------------------------------------------------------------------------

# Please note that the following file observes the following formatting rules:
# - Lines of code do not exceed 120 characters in length.
# - Indentations are represented by 4 space characters (no tab character).
# - Braces wrap statements. For instance: "if {", and "} else {".

#
# ----- Preprocessor-level Stuff ----- #
use strict;                                     # never leave home without it
use warnings;                                   # nor this
use constant ME         => "bldmfg.pl";         # hardwired name reference
use constant WINDOWS    => $^O =~ /^MS/;        # for code readability

#
# ----- Required Libraries ----- #
use Cwd;                                        # to obtain the cwd()
use Getopt::Long;                               # parsing command-line options
use rpm;                                        # home-brew RPM library

#
# ----- Required Global Declarations ----- #
# Note -- all globals are defined within Init()
my $g_sVersion;                                 # engine version required by language
my $g_sProductCode;                             # Windows product code GUID
my $g_sTimestamp;                               # time stamp value to use
my $g_sScmRef;                                  # SCM system reference to use
my $g_sBldRef;                                  # builder reference to use
my $g_bVerbose;                                 # to talk or not to talk
my $g_bSkipTests;                               # skip tests?
my $g_sBaseDir;                                 # base directory to use
my $g_sWinBaseDrive;                            # base directory drive letter (MAX_PATH workaround)
my ($PD,$PS);                                   # path delimiter and seperator

#
# ----- Forward-prototypes ----- #
# Note -- except Main(), prototypes and implementations are sorted
sub Main();
sub _genguid();
sub _mapvirtualdrive($);
sub _system($);
sub BuildProduct();
sub Init();
sub PackageProduct();
sub PackageProductLinux();
sub PackageProductWindows();

# -------------------------------------------------------------------------------------------------
# Routine:  Main Routine
# Purpose:  Controls high-level script operations.
# Accepts:  Nothing.
# Returns:  0 on success, non-zero on failure.
# -------------------------------------------------------------------------------------------------
sub Main() {
    # Required declarations:
    my $nExit = 0;

    # The script will exit if Init() encounters any unrecoverable errors:
    Init();

    # Build our product, assuming Init() didn't exit, then package our product (if we built):
    $nExit = BuildProduct();
    $nExit = PackageProduct() unless ($nExit);

    # Return with our exit code:
    printf("%s: [Main]: Leaving with exit code %d\n",ME,$nExit) if ($g_bVerbose);
    return($nExit);
}

# -------------------------------------------------------------------------------------------------
# Routine:  _genguid()
# Purpose:  To return a Windows installer GUID, generated at random.
# Accepts:  Nothing.
# Returns:  Windows installer GUID string.
# -------------------------------------------------------------------------------------------------
sub _genguid() {
    # Required declarations:
    my @sSet = ('0'..'9','A'..'F');
    my $sGUID = sprintf(
        # Establish our GUID format:
        "{%s-%s-%s-%s-%s}",
        # For each %s placeholder, randomly select one of the characters in
        # @sSet, and generate them 8, 4, 4, 4, then 12 times, joining them
        # together in a single string:
        (join '' => map($sSet[rand(@sSet)],1..8)),
        (join '' => map($sSet[rand(@sSet)],1..4)),
        (join '' => map($sSet[rand(@sSet)],1..4)),
        (join '' => map($sSet[rand(@sSet)],1..4)),
        (join '' => map($sSet[rand(@sSet)],1..12))
    );

    # Return our GUID string:
    printf("%s: [_genguid]: Leaving with guid=%s\n",ME,$sGUID) if ($g_bVerbose);
    return($sGUID);
}

# -------------------------------------------------------------------------------------------------
# Routine:  _mapvirtualdrive($)
# Purpose:  To map a path to a virtual drive.
# Accepts:  Path to map to a virtual drive.
# Returns:  Drive letter assigned, or '' on error.
# -------------------------------------------------------------------------------------------------
sub _mapvirtualdrive($) {
    # Required declarations:
    my $sPath = shift;
    my @sDrives = ('G'..'Z'); # A..F assumed in use by user
    my $sDrive = "";
    my @sOutput = `subst`;
    my $bAvailable = 0;
    my $nIndex = 0;
    my $nRC = 0;

    # We can't try to map a path that does not exist:
    if (-d $sPath) {
        # Cycle through each of our @sDrives, checking for availability:
        for ($nIndex = 0; $nIndex < @sDrives; $nIndex++) {
            $sDrive = $sDrives[$nIndex];

            # Assume available until found in use:
            $bAvailable = 1;

            # Scan through subst output, seeing if this drive is in use:
            foreach (@sOutput) {
                if (/^$sDrive:\\/) {
                    # This drive is in use; stop scanning subst output:
                    $bAvailable = 0;
                    last;
                }
            }

            # Attempt to use this drive if it is available, otherwise keep going:
            if ($bAvailable) {
                $nRC = _system("subst ${sDrive}: ${sPath}");

                # If there is an error, we should try again on the next drive.
                # It could be that some other utility (net use?) is taking it.
                # If there are no errors, stop trying:
                last unless ($nRC);
            }
        }
    }
    else {
        $sDrive = "";
        printf("%s: [_mapvirtualdrive]: ERROR: supplied path '%s' does not exist\n",ME,$sPath);
    }

    # Return our drive letter:
    printf("%s: [_mapvirtualdrive]: Leaving with assigned drive letter: %s\n",ME,$sDrive) if ($g_bVerbose);
    return($sDrive);
}

# -------------------------------------------------------------------------------------------------
# Routine:  _system($)
# Purpose:  Simple Perl system() wrapper that includes console messaging.
# Accepts:  Command string to invoke (as system()).
# Returns:  Process exit code (or error level if on Windows).
# -------------------------------------------------------------------------------------------------
sub _system($) {
    # Required declarations:
    my $sCmd = shift;
    my $nRC = 0;
    my $nResult = 0;

    # Run our command and display the results, if verbose:
    $nRC = system($sCmd);
    $nResult = $nRC >> 8;
    printf("%s: [_system]: Finished '%s' and got result code %d (returning %d)\n",ME,$sCmd,$nRC,$nResult) if ($g_bVerbose);

    # Return with our result:
    return($nResult);
}

# -------------------------------------------------------------------------------------------------
# Routine:  BuildProduct
# Purpose:  'Build' the product, by obtaining required node dependencies and minifying it.
# Accepts:  Nothing.
# Returns:  0 on success, non-zero on failure.
# -------------------------------------------------------------------------------------------------
sub BuildProduct() {
    # Required declarations:
    my $sMfgDir = WINDOWS ? "${g_sWinBaseDrive}:\\mfg\\windows" : "${g_sBaseDir}/mfg/linux";
    my $sSrcDir = WINDOWS ? "${g_sWinBaseDrive}" : "${g_sBaseDir}";
    my $sTargetDir = WINDOWS ? "${sSrcDir}\\target\\package" : "${sSrcDir}/target/package";
    my $sWorkDir = WINDOWS ? "${g_sWinBaseDrive}\\work" : "${g_sBaseDir}/work";
    my $sBuildDir = WINDOWS ? "${sWorkDir}\\build" : "${sWorkDir}/build";
    my $sCP = WINDOWS ? "copy /v/y" : "/bin/cp -v";
    my $sCPR = WINDOWS ? "xcopy /e/f/i/v/y" : "/bin/cp -rv";
    my $sCmd = "mvn -B clean install";
    if ($g_bSkipTests) { 
        $sCmd = "$sCmd -Dmaven.test.skip=true"
    }
    my $sPwd = cwd();
    my $nResult = 0;

    # Create required directories:
    mkdir("$sWorkDir");
    mkdir("$sBuildDir");

    # Build the product via maven:
    chdir($sSrcDir);
    $nResult = _system($sCmd);
    chdir($sPwd);

    # Populate our build directory (if we were successful):
    if ($nResult == 0) {
        $nResult += _system("${sCPR} ${sTargetDir}${PS}* ${sBuildDir}${PS}");
    }

    # Return our result:
    printf("%s: [BuildProduct]: Leaving with result %d\n",ME,$nResult) if ($g_bVerbose);
    return($nResult);
}

# -------------------------------------------------------------------------------------------------
# Routine:  Init()
# Purpose:  Sets global values, handles script arguments and shows usage.
#           *** Will abort script operation if an unrecoverable error ***
#           ***         occurs or the -h switch is supplied.          ***
# Accepts:  Nothing.
# Returns:  Always 0 (if it doesn't exit...).
# -------------------------------------------------------------------------------------------------
sub Init() {
    # Required declarations:
    my ($optHelp,$optTimestamp,$optScmRef,$optBldRef,$optShowGUID,$optVerbose,$optSkipTests,$optVersion,$optUseGUID);
    my $sDefVersion = "0.0.0";
    my $sProdCodeVer = "";
    my $sProdCodeTail = "D682623";

    # Obtain command-line arguments (don't complain about invalid args):
    GetOptions(
        "h|help|usage"  => \$optHelp,
        "showguid"      => \$optShowGUID,
        "version=s"     => \$optVersion,
        "useguid=s"     => \$optUseGUID,
        "timestamp=s"   => \$optTimestamp,
        "scmref=s"      => \$optScmRef,
        "bldref=s"      => \$optBldRef,
        "verbose"       => \$optVerbose,
        "skipTests"     => \$optSkipTests
    );

    # Handle usage/help by displaying a message and **EXIT with the result of 1**:
    if (defined($optHelp)) {
        #rintf("1234567890123456789012345678901234567890123456789012345678901234567890123456789\n");
        printf("%s -- Platform-independent Cobra API Platform packaging script.\n",ME);
        printf("(c) Nuance Communications, Inc.\n");
        printf("USAGE: <perl -w %s> [OPTIONS]\n",ME);
        printf("OPTIONS:\n");
        printf("  -version VER        Version to build as.  Default value: %s\n",$sDefVersion);
        printf("  -timestamp TS       Use time stamp TS (for Linux packaging).\n");
        printf("                      Default value: 000000000000\n");
        printf("  -scmref SCM         String representing the source code reference.\n");
        printf("                      Used exclusively for RPM packaging.\n");
        printf("  -bldref BLD         String representing the builder reference.\n");
        printf("                      Used exclusively for RPM packaging.\n");
        printf("  -verbose            Useful when developing script changes.\n");
        printf("  -skipTests          Skip Unit Tests.\n");
        printf("  -showguid           Generate a product-specific Windows GUID and display it.\n");
        printf("  -useguid GUID       Use the supplied GUID as the Windows product code.\n");
        printf("  -h|-help|-usage     Display this usage information.\n");
        exit(1);
    }

    # Set our global variables:
    if (WINDOWS) {
        $PD = ";";
        $PS = "\\";
    }
    else {
        $PD = ":";
        $PS = "/";
    }

    # Process options and assign global values:
    $g_bVerbose = defined($optVerbose) ? 1 : 0;
    $g_bSkipTests = defined($optSkipTests) ? 1 : 0;
    $g_sVersion = defined($optVersion) ? $optVersion : $sDefVersion;
    $g_sTimestamp = defined($optTimestamp) ? $optTimestamp : "000000000000";
    $g_sScmRef = defined($optScmRef) ? $optScmRef : "0";
    $g_sBldRef = defined($optBldRef) ? $optBldRef : "0";

    # Define our g_sProductCode:
    if (defined($optUseGUID)) {
        $g_sProductCode = $optUseGUID;
    } else {
        # Our product code is actually
        $sProdCodeVer = $g_sVersion;
        $sProdCodeVer =~ s/\./D/g;
        $sProdCodeTail = substr($sProdCodeTail,0,length($sProdCodeTail)-(length($g_sVersion)-5));
        $g_sProductCode = sprintf("{68262681-0683-BB70-0000-%s%s}",$sProdCodeVer,$sProdCodeTail);
    }

    # Determine our g_sBaseDir:
    $g_sBaseDir = cwd();
    $g_sBaseDir =~ s|/common$||;
    $g_sBaseDir =~ s|/mfg$||;
    $g_sBaseDir =~ s|/|\\|g if (WINDOWS);

    # Determine our g_sWinBaseDrive, if Windows:
    $g_sWinBaseDrive = _mapvirtualdrive($g_sBaseDir) if (WINDOWS);

    # If asked to -showguid, then we need to ** WRITE IT AND EXIT **:
    if (defined($optShowGUID)) {
        printf("%s\n",$g_sProductCode);
        exit(0);
    }

    # Write a message before returning:
    if ($g_bVerbose) {
        printf(
            "%s: [Init]: Leaving with:\n".
                "\ttimestamp='%s'\n\tscmref='%s'\n".
                "\tbldref='%s'\n\tbasedir='%s'\n".
                "\tversion='%s'\n\tproductcode='%s'\n",
            ME,
            $g_sTimestamp,$g_sScmRef,
            $g_sBldRef,$g_sBaseDir,
            $g_sVersion,$g_sProductCode
        );
    }
}

# -------------------------------------------------------------------------------------------------
# Routine:  PackageProduct
# Purpose:  Package the prepared language pack.
# Accepts:  Nothing.
# Returns:  0 on success, non-zero on failure.
# -------------------------------------------------------------------------------------------------
sub PackageProduct() {
    # Requried declarations:
    my $nResult = 0;

    # Invoke our OS-dependent package routine:
    if (WINDOWS) {
        $nResult = PackageProductWindows();
    }
    else {
        $nResult = PackageProductLinux();
    }

    # Return our result:
    printf("%s: [PackageProduct]: Leaving with result %d\n",ME,$nResult) if ($g_bVerbose);
    return($nResult);
}

# -------------------------------------------------------------------------------------------------
# Routine:  PackageProductLinux
# Purpose:  Package the prepared language pack for Linux, on Linux.
# Accepts:  Nothing.
# Returns:  0 on success, non-zero on failure.
# -------------------------------------------------------------------------------------------------
sub PackageProductLinux() {
    # Requried declarations:
    my $sArtifactsDir = "${g_sBaseDir}/artifacts";
    my $sBuildDir = "${g_sBaseDir}/work/build";
    my $sTopDir = "${g_sBaseDir}/work/rpm-topdir";
    my $sUserName = "njetty";
    my $sUserGroup = "njetty";
    my $sUnzipCmd = "unzip -o -d \"${sBuildDir}\"";
    my $oRPM = new rpm();
    my $sRPMName = "nuance-webapi-platform";
    my $sRPMDistro = "el7";
    my $sRPMRel = "${g_sTimestamp}.${sRPMDistro}";
    my $sRPMArch = "noarch";
    my $sRPMDesc = "";
    my $sRPMFile = "";
    my $sScriptlet = "${g_sBaseDir}/mfg/linux/${sRPMName}_scriptlets.sh";
    my $nLogLevel = $g_bVerbose ? 3 : 0;
    my $nResult = 0;

    # Determine our RPM description:
    $sRPMDesc = "NuanceInfo: Nuance_WebAPI_Platform ".
                "${sRPMName}-${g_sVersion}-${sRPMRel}.${sRPMArch} ${g_sScmRef} ${g_sBldRef}\n".
                "This RPM installs the Nuance WebAPI ${g_sVersion} on your computer.\n";

    # Construct our RPM object:
    $oRPM->autoreqprov("no");
    $oRPM->requires("nina-webapi-jetty");
    $oRPM->files($sBuildDir);
    $oRPM->topdir($sTopDir);
    $oRPM->username($sUserName);
    $oRPM->usergroup($sUserGroup);
    $oRPM->ver($g_sVersion);
    $oRPM->rel($sRPMRel);
    $oRPM->arch($sRPMArch);
    $oRPM->name($sRPMName);
    $oRPM->loglevel($nLogLevel);
    $oRPM->desc($sRPMDesc);
    $oRPM->prefix("/usr/local");
    $oRPM->postprefix("/Nuance/webapi");
    $oRPM->scriptlets($sScriptlet);

    # Build our RPM:
    $sRPMFile = $oRPM->Build();
    printf("%s: [PackageProductLinux]: Got RPM '%s' from builder\n",ME,$sRPMFile) if ($g_bVerbose);

    # Post it to the artifacts directory if all went well:
    if (-f $sRPMFile) {
        _system("mkdir -p \"${sArtifactsDir}\"");
        $nResult = _system("mv \"${sRPMFile}\" \"${sArtifactsDir}\"");
    }
    else {
        $nResult = 1;
        printf("%s: [PackageProductLinux]: ERROR: Unable to build RPM!\n",ME);
    }

    # Return our result:
    printf("%s: [PackageProductLinux]: Leaving with result %d\n",ME,$nResult) if ($g_bVerbose);
    return($nResult);
}

# -------------------------------------------------------------------------------------------------
# Routine:  PackageProductWindows
# Purpose:  Package the prepared language pack for Windows, on Windows.
# Accepts:  Nothing.
# Returns:  0 on success, non-zero on failure.
# -------------------------------------------------------------------------------------------------
sub PackageProductWindows() {
    # Requried declarations:
    my $sArtifactsDir = "${g_sWinBaseDrive}:\\artifacts";
    my $sPackageDir = "${g_sWinBaseDrive}:\\work\\build";
    my $sISBldCmd = "C:\\Program Files (x86)\\InstallShield\\2010 StandaloneBuild\\System\\IsCmdBld.exe";
    my $sProjDrive = _mapvirtualdrive("${g_sWinBaseDrive}:\\mfg\\windows\\installer");
    my $sProjBuiltDir = "${sProjDrive}:\\PC\\distro\\DiskImages\\DISK1";
    my $sProjTL = "${sProjDrive}:\\nte_webclient_tmp.ism";
    my $sProj = "${sProjDrive}:\\nte_webclient.ism";
    my @sProjContents = ();
    my $sProductName = "NTE Webclient ${g_sVersion}";
    my $sUpgradeCode = "{68262681-068E-9EBC-0000-0D0D0D682623}";
    my $sPackageCode = _genguid();
    my $sPackageDrive = "";
    my $nResult = 0;

    # Create our package drive substitution:
    $sPackageDrive = _mapvirtualdrive($sPackageDir);

    # Attempt to open our installer project for reading:
    if (open(TL,$sProjTL)) {
        @sProjContents = <TL>;
        close(TL);

        # Replace all template tokens with language-specific values:
        foreach (@sProjContents) {
            s/__EXE_NAME__/$sProductName/ if (/__EXE_NAME__/);
            s/0\.0\.0/$g_sVersion/ if (/0\.0\.0/);
            s/{00000000-0000-0000-0000-000000000000}/$g_sProductCode/ if (/{00000000-0000-0000-0000-000000000000}/);
            s/{11111111-1111-1111-1111-111111111111}/$sUpgradeCode/ if (/{11111111-1111-1111-1111-111111111111}/);
            s/{22222222-2222-2222-2222-222222222222}/$sPackageCode/ if (/{22222222-2222-2222-2222-222222222222}/);
            s/__DRIVE__/$sPackageDrive:/ if (/__DRIVE__/);
            s/__PRODUCT_CONFIGURATION__/PC/ if (/__PRODUCT_CONFIGURATION__/);
            s/__RELEASE__/distro/ if (/__RELEASE__/);
        }

        # Attempt to save our adjusted project file:
        if (open(LP, "> $sProj")) {
            print LP @sProjContents;
            close(LP);

            # Build our language pack installer:
            $nResult = _system("\"${sISBldCmd}\" -p \"${sProj}\"");
            _system("subst /d ${sPackageDrive}:");

            # Post it to the artifacts location, if successful:
            if ($nResult) {
                # We were not successful:
                printf(
                    "%s: [PackageProductWindows]: ERROR: The installer did not build successfully!\n",
                    ME
                );
            }
            else {
                # Post our media to the artifacts directory:
                _system("mkdir \"${sArtifactsDir}\"");
                $nResult = _system("move /y \"${sProjBuiltDir}\\*\" \"${sArtifactsDir}\"");
            }

            # We are done with the built media, so we can remove the $sProjDrive substitution:
            _system("subst /d ${sProjDrive}:");

            # We must also remove our $g_sWinBaseDrive substitution, established in Init():
            _system("subst /d ${g_sWinBaseDrive}:");
        }
        else {
            # We were unable to save our adjusted project file:
            $nResult = 1;
            printf(
                "%s: [PackageProductWindows]: ERROR: Unable to save project %s: %s\n",
                ME,$sProj,$!
            );
        }
    }
    else {
        # We were unable to open our project template:
        $nResult = 1;
        printf(
            "%s: [PackageProductWindows]: ERROR: Unable to open project template %s: %s\n",
            ME,$sProjTL,$!
        );
    }

    # Return our result:
    printf("%s: [PackageProductWindows]: Leaving with result %d\n",ME,$nResult) if ($g_bVerbose);
    return($nResult);
}

# -------------------------------------------------------------------------------------------------
# Routine:  STARTUP CODE BLOCK
# Purpose:  Call the Main() routine and exit() with the result.
# -------------------------------------------------------------------------------------------------
exit(Main());
